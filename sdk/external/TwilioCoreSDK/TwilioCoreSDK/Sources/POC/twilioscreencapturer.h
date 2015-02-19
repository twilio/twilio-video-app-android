#ifndef TWILIOSCREENCAPTURER_H
#define TWILIOSCREENCAPTURER_H

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/base/timeutils.h"
#include "talk/media/base/videocapturer.h"
#include "talk/media/base/videocommon.h"
#include "talk/media/base/videoframe.h"
#include "webrtc/modules/video_capture/include/video_capture.h"
#include "webrtc/modules/desktop_capture/screen_capturer.h"
#include "talk/base/timing.h"
#include "webrtc/modules/desktop_capture/desktop_region.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

namespace twiliosdk {


//Move it to the SDK header
//Setups shape and detects FOURCC from trial capture
typedef void ScreenCapturerSetupCallback(const unsigned char *trialBuffer,
                                         unsigned int bitsPerPixel,
                                         unsigned int &fromC,
                                         unsigned int &fromR,
                                         unsigned int &width,
                                         unsigned int &height,
                                         unsigned int &fourcc,
                                         unsigned int &fps,
                                         bool detectFirstScreen);

void defaultScreenCapturerSetupCallback(const unsigned char *trialBuffer,
                                        unsigned int bitsPerPixel,
                                        unsigned int &fromC,
                                        unsigned int &fromR,
                                        unsigned int &width,
                                        unsigned int &height,
                                        unsigned int &fourcc,
                                        unsigned int &fps,
                                        bool detectFirstScreen);

// Twilio video capturer that allows the test to manually pump in frames.
class TwilioScreenCapturer : public cricket::VideoCapturer,
                             webrtc::DesktopCapturer::Callback {
public:
    class CaptureThread : public talk_base::Thread {
    public:
        CaptureThread(TwilioScreenCapturer *sc) : capturer_(sc) {

        }
        virtual ~CaptureThread() {}
        virtual void Run();
    private:
        TwilioScreenCapturer *capturer_;
        talk_base::Timing timing_;
    };

    enum {
        FPS_ = 2,
        MAX_FPS = 30,
        MIN_FPS = 1
    };
    TwilioScreenCapturer();
    virtual ~TwilioScreenCapturer();

    void init(double currentTime);
    //returns true, if not stopped. Sets the next capture time
    bool capture(double &currentTime);
    void ResetSupportedFormats(const std::vector<cricket::VideoFormat>& formats);
    virtual cricket::CaptureState Start(const cricket::VideoFormat& format);
    virtual void Stop();
    virtual bool IsRunning();
    virtual bool IsScreencast() const {return true;}
    virtual bool GetPreferredFourccs(std::vector<uint32>* fourccs);
    virtual void OnCaptureCompleted(webrtc::DesktopFrame* frame);
    virtual webrtc::SharedMemory* CreateSharedMemory(size_t size) {return 0;}
private:
    webrtc::DesktopRegion region_;
    webrtc::ScreenCapturer *capturer_;
    double interval_;
    double nextCapture_;
    unsigned char *buffer_;
    unsigned int fromC_;
    unsigned int fromR_;
    unsigned int width_;
    unsigned int height_;
    unsigned int fourcc_;
    unsigned int fps_;
    volatile bool stop_;
    volatile bool running_;
    volatile bool init_;

    static ScreenCapturerSetupCallback *defaultSetupCallback_;
    static ScreenCapturerSetupCallback *setupCallback_;
};


} //namespace twiliosdk

#endif // TWILIOSCREENCAPTURER_H
