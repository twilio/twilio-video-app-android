/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "test_audio_device.h"

#include <webrtc/api/array_view.h>
#include <webrtc/common_audio/audio_ring_buffer.h>
#include <webrtc/common_audio/channel_buffer.h>
#include <webrtc/common_audio/wav_file.h>
#include <webrtc/modules/audio_device/include/audio_device_default.h>
#include <webrtc/rtc_base/checks.h>
#include <webrtc/rtc_base/criticalsection.h>
#include <webrtc/rtc_base/numerics/safe_conversions.h>
#include <webrtc/rtc_base/platform_thread.h>
#include <webrtc/rtc_base/ptr_util.h>
#include <webrtc/rtc_base/random.h>
#include <webrtc/rtc_base/refcountedobject.h>
#include <webrtc/system_wrappers/include/event_wrapper.h>

namespace twilio_video_jni {

constexpr int kFrameLengthMs = 10;
constexpr int kFramesPerSecond = 1000 / kFrameLengthMs;

// TestAudioDeviceModule implements an AudioDevice module that can act both as a
// capturer and a renderer. It will use 10ms audio frames.
class TestAudioDeviceModuleImpl
        : public webrtc::webrtc_impl::AudioDeviceModuleDefault<TestAudioDeviceModule> {
public:
    // Creates a new TestAudioDeviceModule. When capturing or playing, 10 ms audio
    // frames will be processed every 10ms / |speed|.
    // |capturer| is an object that produces audio data. Can be nullptr if this
    // device is never used for recording.
    // |renderer| is an object that receives audio data that would have been
    // played out. Can be nullptr if this device is never used for playing.
    // Use one of the Create... functions to get these instances.
    TestAudioDeviceModuleImpl(std::unique_ptr<Capturer> capturer,
                              std::unique_ptr<Renderer> renderer,
                              float speed = 1)
            : capturer_(std::move(capturer)),
              renderer_(std::move(renderer)),
              speed_(speed),
              audio_callback_(nullptr),
              audio_recording_sink_(nullptr),
              rendering_(false),
              capturing_(false),
              done_rendering_(true, true),
              done_capturing_(true, true),
              tick_(webrtc::EventTimerWrapper::Create()),
              thread_(TestAudioDeviceModuleImpl::Run,
                      this,
                      "TestAudioDeviceModuleImpl") {
        auto good_sample_rate = [](int sr) {
            return sr == 8000 || sr == 16000 || sr == 32000 || sr == 44100 || sr == 48000;
        };

        if (renderer_) {
            const int sample_rate = renderer_->SamplingFrequency();
            playout_buffer_.resize(SamplesPerFrame(sample_rate) * renderer_->NumChannels(), 0);
            RTC_CHECK(good_sample_rate(sample_rate));
        }
        if (capturer_) {
            RTC_CHECK(good_sample_rate(capturer_->SamplingFrequency()));
        }
    }

    ~TestAudioDeviceModuleImpl() {
        // This class may be destroyed outside of the worker thread.
        thread_checker_.DetachFromThread();
        StopPlayout();
        StopRecording();
        thread_.Stop();
    }

    int32_t Init() {
        RTC_CHECK(tick_->StartTimer(true, kFrameLengthMs / speed_));
        // This method is called during MediaEngine creation, latch on to it as the first worker callback.
        thread_checker_.DetachFromThread();
        thread_.Start();
        thread_.SetPriority(rtc::kHighPriority);
        return 0;
    }

    int32_t RegisterAudioCallback(webrtc::AudioTransport *callback) {
        RTC_CHECK(thread_checker_.CalledOnValidThread());
        rtc::CritScope cs(&lock_);
        RTC_DCHECK(callback || audio_callback_);
        audio_callback_ = callback;
        return 0;
    }

    int32_t RegisterRecordingSink(webrtc::AudioTrackSinkInterface *sink) {
        RTC_CHECK(thread_checker_.CalledOnValidThread());
        rtc::CritScope cs(&lock_);
        if (audio_recording_sink_ == nullptr) {
            audio_recording_sink_ = sink;
            return 0;
        }

        return -1;
    }

    int32_t StartPlayout() {
        RTC_CHECK(thread_checker_.CalledOnValidThread());
        rtc::CritScope cs(&lock_);
        RTC_CHECK(renderer_);
        rendering_ = true;
        done_rendering_.Reset();
        return 0;
    }

    int32_t StopPlayout() {
        RTC_CHECK(thread_checker_.CalledOnValidThread());
        rtc::CritScope cs(&lock_);
        rendering_ = false;
        done_rendering_.Set();
        return 0;
    }

    int32_t StartRecording() {
        RTC_CHECK(thread_checker_.CalledOnValidThread());
        rtc::CritScope cs(&lock_);
        RTC_CHECK(capturer_);
        capturing_ = true;
        done_capturing_.Reset();
        return 0;
    }

    int32_t StopRecording() {
        RTC_CHECK(thread_checker_.CalledOnValidThread());
        rtc::CritScope cs(&lock_);
        capturing_ = false;
        done_capturing_.Set();
        return 0;
    }

    bool Playing() const {
        rtc::CritScope cs(&lock_);
        return rendering_;
    }

    bool Recording() const {
        rtc::CritScope cs(&lock_);
        return capturing_;
    }

    // Blocks until the Renderer refuses to receive data. Returns false if |timeout_ms| passes before that happens.
    bool WaitForPlayoutEnd(int timeout_ms = rtc::Event::kForever) {
        return done_rendering_.Wait(timeout_ms);
    }

    // Blocks until the Recorder stops producing data.Returns false if |timeout_ms| passes before that happens.
    bool WaitForRecordingEnd(int timeout_ms = rtc::Event::kForever) {
        return done_capturing_.Wait(timeout_ms);
    }

private:
    void ProcessAudio() {
        {
            rtc::CritScope cs(&lock_);
            if (capturing_) {
                // Capture 10ms of audio. 2 bytes per sample.
                const bool keep_capturing = capturer_->Capture(&recording_buffer_);
                uint32_t new_mic_level = 0;
                if (recording_buffer_.size() > 0) {
                    const size_t samples = recording_buffer_.size() / capturer_->NumChannels();
                    const size_t bytes_per_sample = capturer_->NumChannels() * sizeof(int16_t);

                    audio_callback_->RecordedDataIsAvailable(
                            recording_buffer_.data(), samples, bytes_per_sample,
                            capturer_->NumChannels(), capturer_->SamplingFrequency(), 0, 0, 0,
                            false, new_mic_level);

                    if (audio_recording_sink_) {
                        // AudioTrackSinkInterface refers to "samples" as "frames".
                        audio_recording_sink_->OnData((const void *)recording_buffer_.data(),
                                                      16,
                                                      capturer_->SamplingFrequency(),
                                                      capturer_->NumChannels(),
                                                      samples);
                    }
                }
                if (!keep_capturing) {
                    capturing_ = false;
                    done_capturing_.Set();
                }
            }
            if (rendering_) {
                size_t samples_out = 0;
                int64_t elapsed_time_ms = -1;
                int64_t ntp_time_ms = -1;
                const int sampling_frequency = renderer_->SamplingFrequency();
                const size_t samples = SamplesPerFrame(sampling_frequency);
                const size_t bytes_per_sample = renderer_->NumChannels() * sizeof(int16_t);

                audio_callback_->NeedMorePlayData(samples, bytes_per_sample, renderer_->NumChannels(),
                                                  sampling_frequency, playout_buffer_.data(), samples_out,
                                                  &elapsed_time_ms, &ntp_time_ms);
                const bool keep_rendering =
                        renderer_->Render(rtc::ArrayView<const int16_t>(playout_buffer_.data(), samples_out));
                if (!keep_rendering) {
                    rendering_ = false;
                    done_rendering_.Set();
                }
            }
        }
        tick_->Wait(WEBRTC_EVENT_INFINITE);
    }

    static bool Run(void *obj) {
        static_cast<TestAudioDeviceModuleImpl *>(obj)->ProcessAudio();
        return true;
    }

    const std::unique_ptr<Capturer> capturer_ RTC_GUARDED_BY(lock_);
    const std::unique_ptr<Renderer> renderer_ RTC_GUARDED_BY(lock_);
    const float speed_;

    rtc::CriticalSection lock_;
    webrtc::AudioTransport *audio_callback_ RTC_GUARDED_BY(lock_);
    webrtc::AudioTrackSinkInterface *audio_recording_sink_ RTC_GUARDED_BY(lock_);
    bool rendering_ RTC_GUARDED_BY(lock_);
    bool capturing_ RTC_GUARDED_BY(lock_);
    rtc::Event done_rendering_;
    rtc::Event done_capturing_;

    std::vector<int16_t> playout_buffer_ RTC_GUARDED_BY(lock_);
    rtc::BufferT<int16_t> recording_buffer_ RTC_GUARDED_BY(lock_);

    std::unique_ptr<webrtc::EventTimerWrapper> tick_;
    rtc::PlatformThread thread_;
    // Ensures that AudioDeviceModule methods are called from the same thread after object creation.
    rtc::ThreadChecker thread_checker_;
};

class BufferedWavFileReader final: public TestAudioDeviceModule::Capturer {
public:
    BufferedWavFileReader(std::string filename,
                          int sampling_frequency_in_hz,
                          int num_channels)
            : BufferedWavFileReader(std::make_unique<webrtc::WavReader>(filename),
                                    sampling_frequency_in_hz,
                                    num_channels) {}

    BufferedWavFileReader(rtc::PlatformFile file,
                          int sampling_frequency_in_hz,
                          int num_channels)
            : BufferedWavFileReader(std::make_unique<webrtc::WavReader>(file),
                                    sampling_frequency_in_hz,
                                    num_channels) {}

    int SamplingFrequency() const override { return sampling_frequency_in_hz_; }

    int NumChannels() const override { return num_channels_; }

    bool Capture(rtc::BufferT<int16_t> *buffer) override {
        buffer->SetData(TestAudioDeviceModule::SamplesPerFrame(sampling_frequency_in_hz_) * num_channels_,
                        [&](rtc::ArrayView<int16_t> data) {
                            if (audio_ring_buffer_->ReadFramesAvailable() == 0) {
                                audio_ring_buffer_->MoveReadPositionBackward(read_total_);
                                read_total_ = 0;
                            }
                            audio_ring_buffer_->Read(conversion_buffer_->fbuf()->Slice(f_slice_.get(), 0),
                                                     static_cast<size_t>(num_channels_),
                                                     data.size());
                            auto tmp_buff_ptr = conversion_buffer_->ibuf_const()->Slice(i_slice_.get(), 0);
                            for (size_t it = 0; it < data.size(); ++it) {
                                data[it] = tmp_buff_ptr[0][it];
                            }
                            return data.size();
                        });
        read_total_ += buffer->size();
        return true;
    }
private:
    BufferedWavFileReader(std::unique_ptr<webrtc::WavReader> wav_reader,
                          int sampling_frequency_in_hz,
                          int num_channels)
            : sampling_frequency_in_hz_(sampling_frequency_in_hz),
              read_total_(0),
              num_channels_(num_channels),
              audio_ring_buffer_(std::make_unique<webrtc::AudioRingBuffer>(num_channels_, wav_reader->num_samples())),
              conversion_buffer_(std::make_unique<webrtc::IFChannelBuffer>(wav_reader->num_samples(),
                                                                           wav_reader->num_channels())),
              f_slice_(new float *[num_channels_]),
              i_slice_(new int16_t *[num_channels_]) {
        RTC_CHECK_EQ(wav_reader->sample_rate(), sampling_frequency_in_hz);
        RTC_CHECK_EQ(wav_reader->num_channels(), num_channels);
        RTC_CHECK_EQ(wav_reader->ReadSamples(wav_reader->num_samples(),
                                             *conversion_buffer_->fbuf()->Slice(f_slice_.get(), 0)),
                     wav_reader->num_samples());
        audio_ring_buffer_->Write(conversion_buffer_->fbuf_const()->Slice(f_slice_.get(), 0),
                                  wav_reader->num_channels(),
                                  wav_reader->num_samples());
        RTC_CHECK_EQ(audio_ring_buffer_->ReadFramesAvailable(), wav_reader->num_samples());
    }

    int sampling_frequency_in_hz_;
    uint64_t read_total_;
    const int num_channels_;
    std::unique_ptr<webrtc::AudioRingBuffer> audio_ring_buffer_;
    std::unique_ptr<webrtc::IFChannelBuffer> conversion_buffer_;
    std::unique_ptr<float *[]> f_slice_;
    std::unique_ptr<int16_t *[]> i_slice_;
};

class WavFileReader final: public TestAudioDeviceModule::Capturer {
public:
    WavFileReader(std::string filename,
                  int sampling_frequency_in_hz,
                  int num_channels)
            : WavFileReader(std::make_unique<webrtc::WavReader>(filename),
                            sampling_frequency_in_hz,
                            num_channels) {}

    WavFileReader(rtc::PlatformFile file,
                  int sampling_frequency_in_hz,
                  int num_channels)
            : WavFileReader(std::make_unique<webrtc::WavReader>(file),
                            sampling_frequency_in_hz,
                            num_channels) {}

    int SamplingFrequency() const override { return sampling_frequency_in_hz_; }

    int NumChannels() const override { return num_channels_; }

    bool Capture(rtc::BufferT<int16_t> *buffer) override {
        buffer->SetData(TestAudioDeviceModule::SamplesPerFrame(sampling_frequency_in_hz_) * num_channels_,
                        [&](rtc::ArrayView<int16_t> data) {
                            return wav_reader_->ReadSamples(data.size(), data.data());
                        });
        return buffer->size() > 0;
    }
private:
    WavFileReader(std::unique_ptr<webrtc::WavReader> wav_reader,
                  int sampling_frequency_in_hz,
                  int num_channels)
            : sampling_frequency_in_hz_(sampling_frequency_in_hz),
              num_channels_(num_channels),
              wav_reader_(std::move(wav_reader)) {
        RTC_CHECK_EQ(wav_reader_->sample_rate(), sampling_frequency_in_hz);
        RTC_CHECK_EQ(wav_reader_->num_channels(), num_channels);
    }

    int sampling_frequency_in_hz_;
    const int num_channels_;
    std::unique_ptr<webrtc::WavReader> wav_reader_;
};

class WavFileWriter final: public TestAudioDeviceModule::Renderer {
public:
    WavFileWriter(std::string filename,
                  int sampling_frequency_in_hz,
                  int num_channels)
            : WavFileWriter(std::make_unique<webrtc::WavWriter>(filename,
                                                                sampling_frequency_in_hz,
                                                                num_channels),
                            sampling_frequency_in_hz,
                            num_channels) {}

    WavFileWriter(rtc::PlatformFile file,
                  int sampling_frequency_in_hz,
                  int num_channels)
            : WavFileWriter(std::make_unique<webrtc::WavWriter>(file,
                                                                sampling_frequency_in_hz,
                                                                num_channels),
                            sampling_frequency_in_hz,
                            num_channels) {}

    int SamplingFrequency() const override { return sampling_frequency_in_hz_; }

    int NumChannels() const override { return num_channels_; }

    bool Render(rtc::ArrayView<const int16_t> data) override {
        wav_writer_->WriteSamples(data.data(), data.size());
        return true;
    }

private:
    WavFileWriter(std::unique_ptr<webrtc::WavWriter> wav_writer,
                  int sampling_frequency_in_hz,
                  int num_channels)
            : sampling_frequency_in_hz_(sampling_frequency_in_hz),
              wav_writer_(std::move(wav_writer)),
              num_channels_(num_channels) {}

    int sampling_frequency_in_hz_;
    std::unique_ptr<webrtc::WavWriter> wav_writer_;
    const int num_channels_;
};

class DiscardRenderer final: public TestAudioDeviceModule::Renderer {
public:
    explicit DiscardRenderer(int sampling_frequency_in_hz, int num_channels)
            : sampling_frequency_in_hz_(sampling_frequency_in_hz),
              num_channels_(num_channels) {}

    int SamplingFrequency() const override { return sampling_frequency_in_hz_; }

    int NumChannels() const override { return num_channels_; }

    bool Render(rtc::ArrayView<const int16_t> data) override { return true; }

private:
    int sampling_frequency_in_hz_;
    const int num_channels_;
};

size_t TestAudioDeviceModule::SamplesPerFrame(int sampling_frequency_in_hz) {
    return rtc::CheckedDivExact(sampling_frequency_in_hz, kFramesPerSecond);
}

rtc::scoped_refptr<TestAudioDeviceModule> TestAudioDeviceModule::CreateTestAudioDeviceModule(
        std::unique_ptr<Capturer> capturer,
        std::unique_ptr<Renderer> renderer,
        float speed) {
    return new rtc::RefCountedObject<TestAudioDeviceModuleImpl>(std::move(capturer), std::move(renderer), speed);
}

std::unique_ptr<TestAudioDeviceModule::Renderer>
TestAudioDeviceModule::CreateDiscardRenderer(int sampling_frequency_in_hz,
                                             int num_channels) {
    return std::make_unique<DiscardRenderer>(sampling_frequency_in_hz, num_channels);
}

std::unique_ptr<TestAudioDeviceModule::Capturer>
TestAudioDeviceModule::CreateBufferedWavFileReader(std::string filename,
                                                   int sampling_frequency_in_hz,
                                                   int num_channels) {
    return std::make_unique<BufferedWavFileReader>(filename, sampling_frequency_in_hz, num_channels);
}

std::unique_ptr<TestAudioDeviceModule::Capturer> TestAudioDeviceModule::CreateBufferedWavFileReader(std::string filename) {
    webrtc::WavReader reader(filename);
    int sampling_frequency_in_hz = reader.sample_rate();
    int num_channels = rtc::checked_cast<int>(reader.num_channels());
    return std::make_unique<BufferedWavFileReader>(filename, sampling_frequency_in_hz, num_channels);
}

std::unique_ptr<TestAudioDeviceModule::Capturer>
TestAudioDeviceModule::CreateWavFileReader(std::string filename,
                                           int sampling_frequency_in_hz,
                                           int num_channels) {
    return std::make_unique<WavFileReader>(filename, sampling_frequency_in_hz, num_channels);
}

std::unique_ptr<TestAudioDeviceModule::Capturer> TestAudioDeviceModule::CreateWavFileReader(std::string filename) {
    webrtc::WavReader reader(filename);
    int sampling_frequency_in_hz = reader.sample_rate();
    int num_channels = rtc::checked_cast<int>(reader.num_channels());
    return std::make_unique<WavFileReader>(filename, sampling_frequency_in_hz, num_channels);
}

std::unique_ptr<TestAudioDeviceModule::Renderer>
TestAudioDeviceModule::CreateWavFileWriter(std::string filename,
                                           int sampling_frequency_in_hz,
                                           int num_channels) {
    return std::make_unique<WavFileWriter>(filename, sampling_frequency_in_hz, num_channels);
}

}
