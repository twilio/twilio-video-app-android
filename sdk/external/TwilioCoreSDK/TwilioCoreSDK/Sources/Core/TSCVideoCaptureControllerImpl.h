//
//  TSCVideoCaptureControllerImpl.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_CAPTURE_CONTROLLER_IMPL_H
#define TSC_VIDEO_CAPTURE_CONTROLLER_IMPL_H

#include "TSCoreSDKTypes.h"
#include "TSCSessionMediaControllers.h"

#include "talk/app/webrtc/mediastreaminterface.h"
#include "talk/media/base/videocommon.h"

namespace twiliosdk {

class TSCVideoCapturer;
class TSCVideoCaptureControllerImpl: public IVideoCaptureControllerInterface
{
public:
    TSCVideoCaptureControllerImpl(webrtc::VideoTrackInterface* videoTrack,
                                  TSCVideoCapturer* videoCapturer);
    virtual ~TSCVideoCaptureControllerImpl();
    
    virtual void setObserver(IVideoCaptureControllerObserverInterface* observer);
    
    virtual bool isPaused() const;
    virtual void setPaused(bool paused);
    virtual void setVideoCaptureDevice(const std::string& deviceId);
    virtual const std::string getVideoCaptureDevice() const;
    
    virtual bool isValid() const;

private:
    void updateCaptureFormat();
    
private:
    TSCVideoCaptureControllerImpl();
    TSCVideoCaptureControllerImpl(const TSCVideoCaptureControllerImpl&);
    TSCVideoCaptureControllerImpl& operator=(TSCVideoCaptureControllerImpl&);
    
    bool isPausedPriv() const;
    void setState(bool paused);
    
    talk_base::scoped_refptr<webrtc::VideoTrackInterface> m_video_track;
    talk_base::scoped_refptr<IVideoCaptureControllerObserverInterface> m_observer;
    
    TSCVideoCapturer* m_video_capturer;

    bool m_paused;
};
    
}

#endif // TSC_VIDEO_CAPTURE_CONTROLLER_IMPL_H
