//
//  TSCVideoSurfaceObserver.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_SURFACE_OBSERVER_H
#define TSC_VIDEO_SURFACE_OBSERVER_H

#include "TSCoreSDKTypes.h"

#include "talk/base/asyncinvoker.h"
#include "talk/base/thread.h"

namespace twiliosdk {

class TSCVideoSurfaceObserver
{
public:
    TSCVideoSurfaceObserver();
    virtual ~TSCVideoSurfaceObserver();

    void onVideoTrackAdded(TSCVideoTrackInfoObject* trackInfo);
    void onVideoTrackRemoved(TSCVideoTrackInfoObject* trackInfo);
    
    void onVideoTrackEvent(TSCVideoTrackInfoObject* trackInfo,
                           TSCVideoTrackEventDataObject* data);

protected:
    virtual void onDidAddVideoTrack(const TSCVideoTrackInfoObjectRef& trackInfo);
    virtual void onDidRemoveVideoTrack(const TSCVideoTrackInfoObjectRef& trackInfo);
    
    virtual void onDidReceiveVideoTrackEvent(const TSCVideoTrackInfoObjectRef& trackInfo,
                                             const TSCVideoTrackEventDataObjectRef& data);
    
private:
    
    void onVideoTrackAddedPriv(TSCVideoTrackInfoObject* trackInfo);
    void onVideoTrackRemovedPriv(TSCVideoTrackInfoObject* trackInfo);
    
    void onVideoTrackEventPriv(TSCVideoTrackInfoObject* trackInfo,
                               TSCVideoTrackEventDataObject* data);
    
private:
    TSCVideoSurfaceObserver(const TSCVideoSurfaceObserver&);
    TSCVideoSurfaceObserver& operator=(TSCVideoSurfaceObserver&);

    talk_base::AsyncInvoker m_invoker;
    talk_base::scoped_ptr<talk_base::Thread> m_thread;
};
    
}  // namespace twiliosdk

#endif  // TSC_VIDEO_SURFACE_OBSERVER_H
