//
//  ITSCVideoCaptureDataConsumer.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef ITSC_VIDEO_CAPTURE_DATA_RECIPIENT_H
#define ITSC_VIDEO_CAPTURE_DATA_RECIPIENT_H

#include "talk/media/base/videocapturer.h"
#include "webrtc/modules/video_capture/include/video_capture.h"

#include "TSCoreSDKTypes.h"

namespace twiliosdk {
    
class ITSCVideoCaptureDataConsumer
{
public:
    ITSCVideoCaptureDataConsumer() {}
    virtual void onVideoCaptureDataFrame(const cricket::CapturedFrame& frame) = 0;
protected:
    virtual ~ITSCVideoCaptureDataConsumer() {};
};
    
} // namespace twiliosdk

#endif // ITSC_VIDEO_CAPTURE_DATA_RECIPIENT_H
