#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/media/base/videocommon.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twilioVideoTrackRenderer.h"

namespace twiliosdk {

//////////////////////////////////////////////////////////////////////////////////
// TwilioVideoTrackRenderer implementation
//////////////////////////////////////////////////////////////////////////////////

TwilioVideoTrackRenderer::TwilioVideoTrackRenderer(const std::string& id,
                                                   webrtc::VideoTrackInterface* videoTrack) :
                                                   ref_count_(0),
                                                   id_(id),
                                                   video_track_(videoTrack),
                                                   buffer_(NULL),
                                                   buffer_size_(0),
                                                   fourcc_type_(cricket::FOURCC_ANY),
                                                   width_(0),
                                                   height_(0),
                                                   video_start_signaled_(false),
                                                   video_started_(false) {
    video_track_->AddRenderer(this);
}

TwilioVideoTrackRenderer::~TwilioVideoTrackRenderer() {
    stop();
    video_track_->RemoveRenderer(this);
}

void TwilioVideoTrackRenderer::SetSize(int width, int height) {
    width_ = width;
    height_ = height;
    TwilioSdkSourceFormat fourcc;
    SignalVideoStarted(id_, width_, height_, &buffer_, &fourcc);
    // We should have buffer & fourcc variables filled.
    fill_fourcc(fourcc);
    video_start_signaled_ = true;
}

void TwilioVideoTrackRenderer::RenderFrame(const cricket::VideoFrame* frame) {
    // Treat unexpected frame size as error.
    if (!frame) {
        return;
    }
    if (!video_start_signaled_) {
        video_start_signaled_ = true;
        width_ = frame->GetWidth();
        height_ = frame->GetHeight();
        TwilioSdkSourceFormat fourcc;
        SignalVideoStarted(id_, width_, height_, &buffer_, &fourcc);
        // We should have buffer & fourcc variables filled.
        fill_fourcc(fourcc);
    }
    if (started()) {
        fill_buffer(frame);
        SignalDataAvailable(id_);
    }
}

void TwilioVideoTrackRenderer::start() {
    video_started_ = true;
}

void TwilioVideoTrackRenderer::stop() {
    video_started_ = false;
    video_start_signaled_ = false;
}

bool TwilioVideoTrackRenderer::started() const {
    return video_started_ && buffer_ && (fourcc_type_ != cricket::FOURCC_ANY);
}

void TwilioVideoTrackRenderer::fill_fourcc(TwilioSdkSourceFormat type) {
    switch (type) {
        case VIDEO_I420:
            fourcc_type_ = cricket::FOURCC_I420;
            buffer_size_ = cricket::VideoFrame::SizeOf(width_, height_);
            break;
        case VIDEO_RGBA:
            fourcc_type_ = cricket::FOURCC_RGBA;
            buffer_size_ = width_ * height_ * 4;
            break;
        case VIDEO_BGRA:
            fourcc_type_ = cricket::FOURCC_BGRA;
            buffer_size_ = width_ * height_ * 4;
            break;
        case VIDEO_ARGB:
            fourcc_type_ = cricket::FOURCC_ARGB;
            buffer_size_ = width_ * height_ * 4;
            break;
        case VIDEO_RBGA:
            fourcc_type_ = cricket::FOURCC_RGBA;
            buffer_size_ = width_ * height_ * 4;
            break;
        case VIDEO_24BG:
            fourcc_type_ = cricket::FOURCC_24BG;
            buffer_size_ = width_ * height_ * 3;
            break;
        default:
            // not supported yet
            break;
    }
}

void TwilioVideoTrackRenderer::fill_buffer(const cricket::VideoFrame* frame) {
    switch (fourcc_type_) {
        case cricket::FOURCC_I420:
            frame->CopyToBuffer(buffer_, buffer_size_);
            break;
        case cricket::FOURCC_BGRA:
        case cricket::FOURCC_ARGB:
        case cricket::FOURCC_RGBA:
            frame->ConvertToRgbBuffer(fourcc_type_,
                                      buffer_,
                                      buffer_size_,
                                      width_ * 4);
            break;
        case cricket::FOURCC_24BG:
            frame->ConvertToRgbBuffer(fourcc_type_,
                                      buffer_,
                                      buffer_size_,
                                      width_ * 3);
            break;
    }
}

const std::string& TwilioVideoTrackRenderer::id() const {
    return id_;
}

int TwilioVideoTrackRenderer::Release() {
    if (--ref_count_ == 0) {
        delete this;
    }
    return ref_count_;
}

int TwilioVideoTrackRenderer::AddRef() {
    return ++ref_count_;
}

}  // namespace twiliosdk
