#include "webrtc/video_frame.h"
#include "webrtc/modules/video_render/include/video_render.h"
#include "talk/media/base/videoframe.h"
#include "talk/app/webrtc/mediastreaminterface.h"

class VideoRenderCallbackConverter : public webrtc::VideoRendererInterface {

public:
    VideoRenderCallbackConverter(webrtc::VideoRenderCallback *callback, const uint32_t streamId) : callback_(callback), streamId_(streamId) {}

    
    virtual void SetSize(int width, int height) {
       // Do nothing
    };
	
    virtual void RenderFrame(const cricket::VideoFrame* frame) {
      // Convert this into an I420VideoFrame

      size_t width = frame->GetWidth();
      size_t height = frame->GetHeight();

      size_t y_plane_size = width * height;
      size_t uv_plane_size = frame->GetChromaSize();

      webrtc::I420VideoFrame *i420Frame = new webrtc::I420VideoFrame();
      i420Frame->CreateFrame(
        frame->GetYPlane(),
        frame->GetUPlane(),
        frame->GetVPlane(),
        width, height,
        frame->GetYPitch(), frame->GetUPitch(), frame->GetVPitch());

      i420Frame->set_render_time_ms(frame->GetTimeStamp() / 1000000);

      callback_->RenderFrame(streamId_, *i420Frame);
    
      delete i420Frame;
    }
    
private:
    webrtc::VideoRenderCallback *callback_;
    const uint32_t streamId_;

};
