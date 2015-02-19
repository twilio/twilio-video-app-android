#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "webrtc/common_video/libyuv/include/webrtc_libyuv.h"
#include "webrtc/modules/video_capture/include/video_capture_factory.h"
#include "talk/media/base/mediachannel.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twilioDeviceManager.h"
#include "twilioVideoCapturer.h"
#include "twilioLogger.h"
#include "twilioConstants.h"

#ifdef WIN32
#include <Objbase.h>
#endif


namespace twiliosdk {

#ifdef WIN32
bool TwilioVideoCapturer::isComInitialized_ = false;
#endif

// Helper typer for data conversion
struct VideoCaptureCapabilityAdapter : public webrtc::VideoCaptureCapability {

    explicit VideoCaptureCapabilityAdapter(const cricket::VideoFormat& format) {
        width = format.width;
        height = format.height;
        maxFPS = cricket::VideoFormat::IntervalToFps(format.interval);
        expectedCaptureDelay = 120;
        rawType = webrtc::kVideoI420;
        codecType = webrtc::kVideoCodecUnknown;
        interlaced = false;
    }
};

struct CapturedFrameAdapter : public cricket::CapturedFrame {

    CapturedFrameAdapter(const webrtc::I420VideoFrame& frame,
                        void* buffer, int length) {
        width = frame.width();
        height = frame.height();
        fourcc = cricket::FOURCC_I420;
        pixel_width = 1;
        pixel_height = 1;
        // Convert units from VideoFrame RenderTimeMs to CapturedFrame (nanoseconds).
        elapsed_time = frame.render_time_ms() * talk_base::kNumNanosecsPerMillisec;
        time_stamp = elapsed_time;
        data_size = length;
        data = buffer;
    }
};

//////////////////////////////////////////////////////////////////////////////////
// TwilioVideoCapturer implementation
//////////////////////////////////////////////////////////////////////////////////

TwilioVideoCapturer::TwilioVideoCapturer() : cricket::VideoCapturer(),
                                             isScreencast_(false), 
                                             last_captured_frame_rate_(0), 
                                             last_feedback_report_time_(webrtc::TickTime::Now()),
                                             capture_feedback_interval_(TwilioDefaultCaptureFeedbackInterval) {
    // Default supported formats. Use ResetSupportedFormats to over write.
    std::vector<cricket::VideoFormat> formats;
    formats.push_back(cricket::VideoFormat(1280, 720,
                                           cricket::VideoFormat::FpsToInterval(30), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(640, 480,
                                           cricket::VideoFormat::FpsToInterval(30), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(352, 288,
                                           cricket::VideoFormat::FpsToInterval(30), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(320, 240,
                                           cricket::VideoFormat::FpsToInterval(30), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(160, 120,
                                           cricket::VideoFormat::FpsToInterval(30), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(1280, 720,
                                           cricket::VideoFormat::FpsToInterval(15), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(640, 480,
                                           cricket::VideoFormat::FpsToInterval(15), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(352, 288,
                                           cricket::VideoFormat::FpsToInterval(15), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(320, 240,
                                           cricket::VideoFormat::FpsToInterval(15), cricket::FOURCC_I420));
    formats.push_back(cricket::VideoFormat(160, 120,
                                           cricket::VideoFormat::FpsToInterval(15), cricket::FOURCC_I420));
    ResetSupportedFormats(formats);
    SignalVideoFrame.connect(this, &TwilioVideoCapturer::OnFrameAdapted);
}

TwilioVideoCapturer::~TwilioVideoCapturer() {
#ifdef WIN32
    if (!isComInitialized_)
    {
        CoUninitialize();
    }
#endif
}

bool TwilioVideoCapturer::Init() {
    if (module_.get()) {
        // Already initialized
        return false;
    }

#ifdef WIN32
    if (!isComInitialized_)
    {
        CoInitialize(NULL);
        isComInitialized_ = true;
    }
#endif

    std::string camera_name, camera_id;
    TwilioDeviceManager::instance().getCurrentDevice(TYPE_VIDEO_INPUT,
                                                     camera_name);

    // Camera device was not set or not found
    if (!TwilioDeviceManager::instance().getDeviceId(TYPE_VIDEO_INPUT,
                                                     camera_name, camera_id)) {
        return false;
    }

    module_ = webrtc::VideoCaptureFactory::Create(0, camera_id.c_str());
    
    process_module_ = webrtc::ProcessThread::CreateProcessThread();
    process_module_->Start();
    process_module_->RegisterModule(module_);

    if (!module_.get()) {
        return false;
    }
    return true;
}

void TwilioVideoCapturer::setCaptureFeedbackInterval(int64_t timeInMsec) {
    capture_feedback_interval_ = timeInMsec;
    LOG_INFO_STREAM << "Setting capture feedback interval to: " << timeInMsec << " msec" << std::endl;
}

void TwilioVideoCapturer::ResetSupportedFormats(const std::vector<cricket::VideoFormat>& formats) {
    SetSupportedFormats(formats);
}

cricket::CaptureState TwilioVideoCapturer::Start(const cricket::VideoFormat& format) {
    cricket::VideoFormat supported;
    if (GetBestCaptureFormat(format, &supported)) {
        SetCaptureFormat(&supported);
    }

    if (IsRunning()) {
        return cricket::CS_RUNNING;
    }

    if (!module_.get()) {
        Init();
    }
    if (!module_.get()) {
        return cricket::CS_NO_DEVICE;
    }
    VideoCaptureCapabilityAdapter cap(format);
    module_->RegisterCaptureDataCallback(*this);
    module_->RegisterCaptureCallback(*this);
    module_->EnableFrameRateCallback(true);
    module_->EnableNoPictureAlarm(false);
    if (module_->StartCapture(cap) != 0) {
        return cricket::CS_FAILED;
    }

    SetCaptureState(cricket::CS_RUNNING);
    return cricket::CS_RUNNING;
}

void TwilioVideoCapturer::Stop() {
    if (IsRunning()) {
        module_->StopCapture();
        module_->DeRegisterCaptureDataCallback();
        module_->DeRegisterCaptureCallback();
    }
    process_module_->DeRegisterModule(module_);
    process_module_->Stop();
    webrtc::ProcessThread::DestroyProcessThread(process_module_);

    module_.release();
    SetCaptureFormat(NULL);
    if (cricket::CS_STOPPED != capture_state()) {
        SetCaptureState(cricket::CS_STOPPED);
    }
}

bool TwilioVideoCapturer::IsRunning() {
    return (module_.get() != NULL) && (module_->CaptureStarted());
}

bool TwilioVideoCapturer::GetPreferredFourccs(std::vector<uint32>* fourccs) {
    fourccs->push_back(cricket::FOURCC_I420);
    fourccs->push_back(cricket::FOURCC_MJPG);
    return true;
}

void TwilioVideoCapturer::SetScreencast(bool is_screencast) {
    isScreencast_ = is_screencast;
}

bool TwilioVideoCapturer::IsScreencast() const {
    return isScreencast_;
}

void TwilioVideoCapturer::OnIncomingCapturedFrame(const int32_t id,
                                                  webrtc::I420VideoFrame& sample) {
    const int length = webrtc::CalcBufferSize(webrtc::kI420,
                                              sample.width(), sample.height());
    capture_buffer_.resize(length);
    webrtc::ExtractBuffer(sample, length, &capture_buffer_[0]);
    CapturedFrameAdapter frame(sample, &capture_buffer_[0], length);
    // Signal down stream components on captured frame.
    SignalFrameCaptured(this, &frame);

}

void TwilioVideoCapturer::OnCaptureDelayChanged(const int32_t id,
                                                const int32_t delay) {
}

void TwilioVideoCapturer::OnCaptureFrameRate(const int32_t id,
                        const uint32_t frameRate) {
    last_captured_frame_rate_ = frameRate;
}

void TwilioVideoCapturer::OnNoPictureAlarm(const int32_t id,
                      const webrtc::VideoCaptureAlarm alarm) {
    LOG_WARN("No frame captured"); 
}

void TwilioVideoCapturer::OnFrameAdapted(cricket::VideoCapturer*, const cricket::VideoFrame* video_frame) {

    const webrtc::TickTime now = webrtc::TickTime::Now();
    if ((now - last_feedback_report_time_).Milliseconds() > capture_feedback_interval_) {
        SignalCaptureFeedbackAvailable(video_frame->GetWidth(), video_frame->GetHeight(), last_captured_frame_rate_);
        last_feedback_report_time_ = now;
   }
}

}  // namespace twiliosdk 
