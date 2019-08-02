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

#ifndef VIDEO_ANDROID_TEST_AUDIO_DEVICE_H_
#define VIDEO_ANDROID_TEST_AUDIO_DEVICE_H_

#include <memory>
#include <string>
#include <vector>
#include <algorithm>
#include <memory>
#include <string>
#include <utility>
#include <vector>

#include <webrtc/modules/audio_device/include/audio_device.h>
#include <webrtc/rtc_base/buffer.h>
#include <webrtc/rtc_base/event.h>
#include <webrtc/rtc_base/platform_file.h>
#include <webrtc/typedefs.h>

namespace twilio_video_jni {

// TestAudioDeviceModule implements an AudioDevice module that can act both as a
// capturer and a renderer. It will use 10ms audio frames.
class TestAudioDeviceModule: public webrtc::AudioDeviceModule {
public:
    // Returns the number of samples that Capturers and Renderers with this
    // sampling frequency will work with every time Capture or Render is called.
    static size_t SamplesPerFrame(int sampling_frequency_in_hz);

    class Capturer {
    public:
        virtual ~Capturer() {}
        // Returns the sampling frequency in Hz of the audio data that this
        // capturer produces.
        virtual int SamplingFrequency() const = 0;
        // Returns the number of channels of captured audio data.
        virtual int NumChannels() const = 0;
        // Replaces the contents of |buffer| with 10ms of captured audio data
        // (see TestAudioDeviceModule::SamplesPerFrame). Returns true if the
        // capturer can keep producing data, or false when the capture finishes.
        virtual bool Capture(rtc::BufferT<int16_t> *buffer) = 0;
    };

    class Renderer {
    public:
        virtual ~Renderer() {}
        // Returns the sampling frequency in Hz of the audio data that this
        // renderer receives.
        virtual int SamplingFrequency() const = 0;
        // Returns the number of channels of audio data to be required.
        virtual int NumChannels() const = 0;
        // Renders the passed audio data and returns true if the renderer wants
        // to keep receiving data, or false otherwise.
        virtual bool Render(rtc::ArrayView<const int16_t> data) = 0;
    };

    virtual ~TestAudioDeviceModule() {}

    // Creates a new TestAudioDeviceModule. When capturing or playing, 10 ms audio
    // frames will be processed every 10ms / |speed|.
    // |capturer| is an object that produces audio data. Can be nullptr if this
    // device is never used for recording.
    // |renderer| is an object that receives audio data that would have been
    // played out. Can be nullptr if this device is never used for playing.
    // Use one of the Create... functions to get these instances.
    static rtc::scoped_refptr<TestAudioDeviceModule> CreateTestAudioDeviceModule(std::unique_ptr<Capturer> capturer,
                                                                                 std::unique_ptr<Renderer> renderer,
                                                                                 float speed = 1);

    // Returns a Renderer instance that does nothing with the audio data.
    static std::unique_ptr<Renderer> CreateDiscardRenderer(
            int sampling_frequency_in_hz,
            int num_channels = 1);

    // WavReader and WavWriter creation based on file name.

    // Returns a Capturer instance that gets its data from a file and saves it into a ring buffer for loop play.
    // The sample rate and channels will be checked against the Wav file.
    static std::unique_ptr<Capturer> CreateBufferedWavFileReader(std::string filename,
                                                                 int sampling_frequency_in_hz,
                                                                 int num_channels = 1);

    // Returns a Capturer instance that gets its data from a file and saves it into a ring buffer for loop play.
    // Automatically detects sample rate and num of channels.
    static std::unique_ptr<Capturer> CreateBufferedWavFileReader(std::string filename);

    // Returns a Capturer instance that gets its data from a file. The sample rate
    // and channels will be checked against the Wav file.
    static std::unique_ptr<Capturer> CreateWavFileReader(std::string filename,
                                                         int sampling_frequency_in_hz,
                                                         int num_channels = 1);

    // Returns a Capturer instance that gets its data from a file.
    // Automatically detects sample rate and num of channels.
    static std::unique_ptr<Capturer> CreateWavFileReader(std::string filename);

    // Returns a Renderer instance that writes its data to a file.
    static std::unique_ptr<Renderer> CreateWavFileWriter(std::string filename,
                                                         int sampling_frequency_in_hz,
                                                         int num_channels = 1);

    virtual int32_t Init() = 0;
    virtual int32_t RegisterAudioCallback(webrtc::AudioTransport *callback) = 0;
    virtual int32_t RegisterRecordingSink(webrtc::AudioTrackSinkInterface *sink) = 0;

    virtual int32_t StartPlayout() = 0;
    virtual int32_t StopPlayout() = 0;
    virtual int32_t StartRecording() = 0;
    virtual int32_t StopRecording() = 0;

    virtual bool Playing() const = 0;
    virtual bool Recording() const = 0;

    // Blocks until the Renderer refuses to receive data.
    // Returns false if |timeout_ms| passes before that happens.
    virtual bool WaitForPlayoutEnd(int timeout_ms = rtc::Event::kForever) = 0;
    // Blocks until the Recorder stops producing data.
    // Returns false if |timeout_ms| passes before that happens.
    virtual bool WaitForRecordingEnd(int timeout_ms = rtc::Event::kForever) = 0;
};

}

#endif // VIDEO_ANDROID_TEST_AUDIO_DEVICE_H_
