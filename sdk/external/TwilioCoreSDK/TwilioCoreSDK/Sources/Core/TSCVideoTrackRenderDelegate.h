//
//  TSCVideoTrackRenderDelegate.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_TRACK_RENDER_DELEGATE_H
#define TSC_VIDEO_TRACK_RENDER_DELEGATE_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class ITSCVideoTrackRenderDelegate : public talk_base::RefCountInterface
{
public:

    virtual void onVideoTrackEvent(const TSCVideoTrackInfoObjectRef& trackInfo,
                                   const TSCVideoTrackEventDataObjectRef& data) = 0;
    
protected:
    ~ITSCVideoTrackRenderDelegate() {}
};
    
    
template <class T>
class TSCVideoTrackRenderDelegate: public ITSCVideoTrackRenderDelegate
{
public:
    
    TSCVideoTrackRenderDelegate(T* target)
    {
        m_target = target;
    }
    
    void onVideoTrackEvent(const TSCVideoTrackInfoObjectRef& trackInfo,
                           const TSCVideoTrackEventDataObjectRef& data)
    {
        m_target->onVideoTrackEvent(trackInfo, data);
    }
    
private:
    
    T* m_target;
};
    
}  // namespace twiliosdk

#endif  // TSC_VIDEO_TRACK_RENDER_DELEGATE_H
