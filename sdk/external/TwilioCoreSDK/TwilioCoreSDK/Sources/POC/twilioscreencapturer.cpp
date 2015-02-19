#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "webrtc/modules/desktop_capture/desktop_capture_options.h"
#include "webrtc/modules/desktop_capture/desktop_frame.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twilioscreencapturer.h"

using namespace twiliosdk;


ScreenCapturerSetupCallback *TwilioScreenCapturer::defaultSetupCallback_ = defaultScreenCapturerSetupCallback;
ScreenCapturerSetupCallback *TwilioScreenCapturer::setupCallback_ = defaultScreenCapturerSetupCallback;

void twiliosdk::defaultScreenCapturerSetupCallback(const unsigned char *trialBuffer,
                                                     unsigned int bitsPerPixel,
                                                     unsigned int &fromC,
                                                     unsigned int &fromR,
                                                     unsigned int &width,
                                                     unsigned int &height,
                                                     unsigned int &fourcc,
                                                     unsigned int &fps,
                                                     bool detectFirstScreen) {
    const unsigned char *row = trialBuffer;
    size_t i;
    size_t rowSz = (width * bitsPerPixel / 8);
    size_t bufSz = (rowSz * height);
    if (32 == bitsPerPixel) {//else - detect later
        for (i = 0; i < bufSz; ++i) {
            if (trialBuffer[i] != trialBuffer[(i + 3)]) {
                if (255 == trialBuffer[i]) {
                    fourcc = cricket::FOURCC_RGBA;
                    break;
                }
                if (255 == trialBuffer[(i + 3)]) {
                    fourcc = cricket::FOURCC_ARGB;
                    break;
                }
            }
        }
    }
    if (detectFirstScreen) {
        if (32 == bitsPerPixel) {
            //It is kind of unreliable, for demo only
            unsigned int current = (height - 1);
            const unsigned int *line = (const unsigned int *)(trialBuffer + (rowSz * current));
            unsigned int suspected = width;
            //find first
            for (i = 0; i < width; ++i) {
                if (0 != line[i]) {
                    suspected = i;
                    break;
                }
            }
            if (0 != suspected) {
                while (current > 0) {
                    --current;
                    line = (const unsigned int *)(trialBuffer + (rowSz * current));
                    bool done  = false;
                    for (i = 0; i < width; ++i) {
                        if (0 != line[i]) {
                            done = (i != suspected);
                            break;
                        }
                    }
                    if (done) break;
                }
                if (current != (height - 2)) {
                    width = suspected;
                    height = (current + 1);
                }
            }
        }
    }


}


TwilioScreenCapturer::TwilioScreenCapturer() {

    init_ = true;
    fromC_ = fromR_ = 0;
    width_ = 640;
    height_ = 480;
    fps_ = FPS_;
    fourcc_ = cricket::FOURCC_ARGB;
    webrtc::DesktopCaptureOptions opt = webrtc::DesktopCaptureOptions::CreateDefault();
    opt.set_use_update_notifications(true);
    capturer_ = webrtc::ScreenCapturer::Create(opt);
    webrtc::DesktopRect dr = webrtc::DesktopRect::MakeXYWH(0, 0, 0, 0);
    capturer_->Start(this);
    region_.SetRect(dr);
    capturer_->Capture(region_);

    while (init_) {
        talk_base::Thread::SleepMs(50);
    }


    size_t sz = (4 * width_ * height_);
    buffer_ = (unsigned char *)malloc(sz);
    if (0 != buffer_) {
        memset(buffer_, 0, sz);
    }
    std::vector<cricket::VideoFormat> formats;
    formats.push_back(cricket::VideoFormat(width_, height_,
                                           cricket::VideoFormat::FpsToInterval(fps_), fourcc_));
    ResetSupportedFormats(formats);
    interval_ = (1. / (double)fps_);
    stop_ = false;
    running_ = false;

}

TwilioScreenCapturer::~TwilioScreenCapturer() {
    if (running_) Stop();
    if (0 != buffer_) free(buffer_);
    if (0 != capturer_) delete capturer_;
}

void TwilioScreenCapturer::ResetSupportedFormats(const std::vector<cricket::VideoFormat>& formats) {
    SetSupportedFormats(formats);
}

void TwilioScreenCapturer::CaptureThread::Run() {

    double currentTime = timing_.WallTimeNow();
    double now = currentTime;
    capturer_->init(currentTime);
    while(capturer_->capture(currentTime)) {
        double diff = (currentTime - now);
        if (diff > 0) {
            int ms = (int)(diff * 1000);
            ProcessMessages(ms);
        }
        currentTime = now = timing_.WallTimeNow();
    }
    delete this;
}



cricket::CaptureState TwilioScreenCapturer::Start(const cricket::VideoFormat& format) {
    if (running_) {
        if (stop_) {
            //TODO: do something later
            for (int i = 0; i < 1000; ++i) {
                if (!running_) break;
                talk_base::Thread::SleepMs(100);
            }
        }
    }
    running_ = true;
    webrtc::DesktopRect dr = webrtc::DesktopRect::MakeXYWH(fromC_, fromR_, width_, height_);
    region_.SetRect(dr);
    CaptureThread *thread = new CaptureThread(this);
    thread->Start();
    return cricket::CS_RUNNING;
}




void TwilioScreenCapturer::Stop() {
    if (running_) {
        if (!stop_) {
            stop_ = true;
            while (running_) {
                talk_base::Thread::SleepMs(100);
            }
        }
    }
}

bool TwilioScreenCapturer::IsRunning() {
    return running_;
}



bool TwilioScreenCapturer::GetPreferredFourccs(std::vector<uint32>* fourccs) {
    fourccs->push_back(fourcc_);
    return true;
}

void TwilioScreenCapturer::init(double currentTime) {
    nextCapture_ = (currentTime + interval_);
}

bool TwilioScreenCapturer::capture(double &currentTime) {
    if (stop_) {
        stop_ = false;
        running_ = false;
        return false;
    }
    nextCapture_ += interval_;
    currentTime = nextCapture_;
    capturer_->Capture(region_);
    return true;
}

void TwilioScreenCapturer::OnCaptureCompleted(webrtc::DesktopFrame* frame) {

    if (0 != frame) {
        if (!init_) {
            unsigned int cw = frame->size().width();
            unsigned int ch = frame->size().height();
            unsigned int bitsPerPixel = (frame->stride() * 8);
            bitsPerPixel /= cw;
            if (32 == bitsPerPixel) {
                cricket::CapturedFrame fr;
                fr.pixel_height = 1;
                fr.pixel_width = 1;
                fr.fourcc = fourcc_;
                fr.height = height_;
                fr.width = width_;
                fr.data_size = (height_ * width_ * 4);

                const unsigned char *src = frame->data();

                unsigned int w, h;

                unsigned int r_sz_s = frame->stride();
                unsigned int r_sz = (width_ * 4);

                unsigned char *dest = buffer_;
                if (fromC_ >= cw) {
                    w = 0;
                } else {
                    src += (fromC_ * 4);
                    w = (cw - fromC_);
                    if (w > width_) w = width_;
                }

                if (fromR_ >= ch) {
                    h = 0;
                } else {
                    src += (r_sz_s * fromR_);
                    h = (ch - fromR_);
                    if (h > height_) h = height_;
                }



                unsigned int i;
                for (i = 0; i < h; ++i) {
                    if (0 != r_sz) {
                        memmove(dest, src, r_sz);
                    }
                    dest += r_sz;
                    src += r_sz_s;
                }
                unsigned int diff = (width_ - w);
                if (diff > 0) {
                    diff *= 4;
                    dest = (buffer_ + (w * 4));
                    for (i = 0; i < h; ++i) {
                        memset(dest, 0, diff);
                        dest += r_sz;
                    }
                }
                dest = (buffer_ + (4 * width_ * h));
                for (i = h; i < height_; ++i) {
                    memset(dest, 0, (width_ * 4));
                    dest += r_sz;
                }


                fr.data = buffer_;
                SignalFrameCaptured(this, &fr);
            }
        } else {
            width_ = frame->size().width();
            height_ = frame->size().height();

            unsigned int bitsPerPixel = (frame->stride() * 8);
            bitsPerPixel /= width_;
            setupCallback_(frame->data(), bitsPerPixel, fromC_, fromR_,
                           width_, height_, fourcc_, fps_,
                           (setupCallback_ == defaultSetupCallback_));

            init_ = false;
        }
        delete frame;
    } else if (init_) init_ = false;
}

