//
//  TSCVideoSurfaceObserver.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCVideoSurfaceObserver.h"
#include "TSCLogger.h"
#include "TSCMediaTrackInfo.h"
#include "TSCVideoTrackEventData.h"
#include "TSCThreadMonitor.h"

namespace twiliosdk {
    
TSCVideoSurfaceObserver::TSCVideoSurfaceObserver()
{
    m_thread.reset(new talk_base::Thread());
    m_thread->Start(new TSCThreadMonitor("TSCVideoSurfaceObserver", this));
}
    
TSCVideoSurfaceObserver::~TSCVideoSurfaceObserver()
{
    TS_CORE_LOG_DEBUG("TSCVideoSurfaceObserver::~TSCVideoSurfaceObserver()");
    m_thread->Stop();
}

#pragma mark-

void
TSCVideoSurfaceObserver::onVideoTrackEvent(TSCVideoTrackInfoObject* trackInfo,
                                           TSCVideoTrackEventDataObject* data)
{
    trackInfo->AddRef();
    data->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(),
                                talk_base::Bind(&TSCVideoSurfaceObserver::onVideoTrackEventPriv,
                                                this, trackInfo, data));
}

void
TSCVideoSurfaceObserver::onVideoTrackEventPriv(TSCVideoTrackInfoObject* trackInfo,
                                               TSCVideoTrackEventDataObject* data)
{
    TSCVideoTrackInfoObjectRef trackInfoHolder = trackInfo;
    trackInfo->Release();
    TSCVideoTrackEventDataObjectRef dataHolder = data;
    data->Release();
    onDidReceiveVideoTrackEvent(trackInfoHolder, data);
}

void
TSCVideoSurfaceObserver::onDidReceiveVideoTrackEvent(const TSCVideoTrackInfoObjectRef& trackInfo,
                                                     const TSCVideoTrackEventDataObjectRef& data)
{
}

#pragma mark-
    
void
TSCVideoSurfaceObserver::onVideoTrackAdded(TSCVideoTrackInfoObject* trackInfo)
{
    trackInfo->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(),
                                talk_base::Bind(&TSCVideoSurfaceObserver::onVideoTrackAddedPriv,
                                                this, trackInfo));
}

void
TSCVideoSurfaceObserver::onVideoTrackAddedPriv(TSCVideoTrackInfoObject* trackInfo)
{
    TSCVideoTrackInfoObjectRef trackInfoHolder = trackInfo;
    trackInfo->Release();
    onDidAddVideoTrack(trackInfoHolder);
}

void
TSCVideoSurfaceObserver::onDidAddVideoTrack(const TSCVideoTrackInfoObjectRef& trackInfo)
{
}
    
#pragma mark-
    
void
TSCVideoSurfaceObserver::onVideoTrackRemoved(TSCVideoTrackInfoObject* trackInfo)
{
    trackInfo->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(),
                                talk_base::Bind(&TSCVideoSurfaceObserver::onVideoTrackRemovedPriv,
                                                this, trackInfo));
}

void
TSCVideoSurfaceObserver::onVideoTrackRemovedPriv(TSCVideoTrackInfoObject* trackInfo)
{
    TSCVideoTrackInfoObjectRef trackInfoHolder = trackInfo;
    trackInfo->Release();
    onDidRemoveVideoTrack(trackInfoHolder);
}

void
TSCVideoSurfaceObserver::onDidRemoveVideoTrack(const TSCVideoTrackInfoObjectRef& trackInfo)
{
}
    
} // namespace twiliosdk
