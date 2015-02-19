//
//  TSCVideoTrackRender.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_TRACK_RENDER_H
#define TSC_VIDEO_TRACK_RENDER_H

#include "TSCoreSDKTypes.h"
#include "TSCVideoTrackRenderDelegate.h"

#include "talk/app/webrtc/mediastreaminterface.h"
#include "talk/media/base/videoframe.h"

namespace twiliosdk {

class TSCVideoTrackRender: public webrtc::VideoRendererInterface
{
public:
    
    TSCVideoTrackRender(const TSCVideoTrackInfoObjectRef& trackInfo,
                        webrtc::VideoTrackInterface* videoTrack,
                        ITSCVideoTrackRenderDelegate* delegate);
    virtual ~TSCVideoTrackRender();
    
    virtual void SetSize(int width, int height);
    virtual void RenderFrame(const cricket::VideoFrame* frame);

private:
    TSCVideoTrackRender(const TSCVideoTrackRender&);
    TSCVideoTrackRender& operator=(TSCVideoTrackRender&);
    
    TSCVideoTrackInfoObjectRef m_track_info;
    talk_base::scoped_refptr<webrtc::VideoTrackInterface> m_video_track;
    talk_base::scoped_refptr<ITSCVideoTrackRenderDelegate> m_delegate;
};
    
}  // namespace twiliosdk

#endif  // TSC_VIDEO_TRACK_RENDER_H
