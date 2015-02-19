//
//  TSCVideoSurface.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCVideoSurface.h"
#include "TSCoreError.h"
#include "TSCLogger.h"
#include "TSCVideoTrackRender.h"
#include "TSCVideoSurfaceObserver.h"
#include "TSCMediaTrackInfo.h"
#include "TSCVideoTrackEventData.h"

namespace twiliosdk {
    
TSCVideoSurface::TSCVideoSurface(const TSCVideoSurfaceObserverObjectRef& observer)
{
    m_observer = observer;
}
    
TSCVideoSurface::~TSCVideoSurface()
{
    m_observer = nullptr;
}

#pragma mark-

void
TSCVideoSurface::onAddTrack(const TSCVideoTrackInfoObjectRef& trackInfo, webrtc::VideoTrackInterface* videoTrack)
{
    TS_CORE_LOG_DEBUG("onAddTrack[%s]", trackInfo->getTrackId().c_str());
    
    talk_base::scoped_refptr<TSCVideoTrackRenderDelegate<TSCVideoSurface>>
    observer(new talk_base::RefCountedObject<TSCVideoTrackRenderDelegate<TSCVideoSurface>>(this));
    
    TSCVideoTrackRenderObjectRef render = new TSCVideoTrackRenderObject(trackInfo, videoTrack, observer);
    m_renders.insert(std::make_pair(trackInfo->getTrackId(), render));
    
    if(m_observer.get() != nullptr)
       m_observer->onVideoTrackAdded(trackInfo.get());
}

void
TSCVideoSurface::onRemoveTrack(const TSCVideoTrackInfoObjectRef& trackInfo)
{
    TS_CORE_LOG_DEBUG("onRemoveTrack[%s]", trackInfo->getTrackId().c_str());
    
    if(hasRenderForTrackId(trackInfo->getTrackId()))
    {
        m_renders.erase(trackInfo->getTrackId());
        if(m_observer.get() != nullptr)
           m_observer->onVideoTrackRemoved(trackInfo.get());
    }
}

#pragma mark-
    
bool
TSCVideoSurface::hasRenderForTrackId(const std::string& trackId) const
{
    TSVideoTrackRenderCollectionConstIterator pos = m_renders.find(trackId);
    return (pos != m_renders.end());
}
    
const TSCVideoTrackRenderObjectRef&
TSCVideoSurface::renderForTrackId(const std::string& trackId) const
{
    TSVideoTrackRenderCollectionConstIterator pos = m_renders.find(trackId);
    return pos->second;
}
    
#pragma mark-

void
TSCVideoSurface::onVideoTrackEvent(const TSCVideoTrackInfoObjectRef& trackInfo,
                                   const TSCVideoTrackEventDataObjectRef& data)
{
    // TS_CORE_LOG_DEBUG("VideoTrackEvent[%s]: type [%d]", trackInfo->getTrackId().c_str(), data->getType());
    if(m_observer.get() != nullptr)
       m_observer->onVideoTrackEvent(trackInfo.get(), data.get());
}
    
} // namespace twiliosdk
