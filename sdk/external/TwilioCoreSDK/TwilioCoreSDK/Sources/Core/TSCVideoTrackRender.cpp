//
//  TSCVideoTrackRender.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCVideoTrackRender.h"
#include "TSCoreError.h"
#include "TSCLogger.h"
#include "TSCMediaTrackInfo.h"
#include "TSCVideoTrackEventData.h"

namespace twiliosdk {
    
TSCVideoTrackRender::TSCVideoTrackRender(const TSCVideoTrackInfoObjectRef& trackInfo,
                                         webrtc::VideoTrackInterface* videoTrack,
                                         ITSCVideoTrackRenderDelegate* delegate)
{
    m_track_info = trackInfo;
    m_delegate = delegate;
    m_video_track = videoTrack;
    
    m_video_track->AddRenderer(this);
}
    
TSCVideoTrackRender::~TSCVideoTrackRender()
{
    m_video_track->RemoveRenderer(this);
}

#pragma mark-

void
TSCVideoTrackRender::SetSize(int width, int height)
{
    TSCVideoTrackEventDataObjectRef data = new TSCVideoTrackEventDataObject(kTSCVideoTrackEventDataFrameSize,
                                                                            width, height);
    m_delegate->onVideoTrackEvent(m_track_info, data);
}
    
void
TSCVideoTrackRender::RenderFrame(const cricket::VideoFrame* frame)
{
    TSCVideoTrackEventDataObjectRef data = new TSCVideoTrackEventDataObject(kTSCVideoTrackEventDataFrameBuffer,
                                                                            frame);
    m_delegate->onVideoTrackEvent(m_track_info, data);
}
    
} // namespace twiliosdk
