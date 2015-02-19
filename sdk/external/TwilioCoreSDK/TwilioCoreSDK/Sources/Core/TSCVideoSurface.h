//
//  TSCVideoSurface.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_SURFACE_H
#define TSC_VIDEO_SURFACE_H

#include "TSCoreSDKTypes.h"

#include "talk/app/webrtc/mediastreaminterface.h"

namespace twiliosdk {

class TSCVideoSurface
{
public:
    
    TSCVideoSurface(const TSCVideoSurfaceObserverObjectRef& observer);
    virtual ~TSCVideoSurface();

    void onAddTrack(const TSCVideoTrackInfoObjectRef& trackInfo, webrtc::VideoTrackInterface* videoTrack);
    void onRemoveTrack(const TSCVideoTrackInfoObjectRef& trackInfo);
    
    void onVideoTrackEvent(const TSCVideoTrackInfoObjectRef& trackInfo,
                           const TSCVideoTrackEventDataObjectRef& data);
    
private:
    TSCVideoSurface(const TSCVideoSurface&);
    TSCVideoSurface& operator=(TSCVideoSurface&);

    bool hasRenderForTrackId(const std::string& trackId) const;
    const TSCVideoTrackRenderObjectRef& renderForTrackId(const std::string& trackId) const;
    
    TSVideoTrackRenderCollection m_renders;
    TSCVideoSurfaceObserverObjectRef m_observer;
};
    
}  // namespace twiliosdk

#endif  // TSC_VIDEO_SURFACE_H
