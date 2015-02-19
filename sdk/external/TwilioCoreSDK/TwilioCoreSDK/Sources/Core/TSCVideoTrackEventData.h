//
//  TSCVideoTrackEventData.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_TRACK_EVENT_DATA_H
#define TSC_VIDEO_TRACK_EVENT_DATA_H

#include "TSCoreSDKTypes.h"
#include "talk/media/base/videoframe.h"

namespace twiliosdk {

typedef enum _TSCVideoTrackEventDataType {
    kTSCVideoTrackEventDataFrameSize   = 1,
    kTSCVideoTrackEventDataFrameBuffer = 2
} TSCVideoTrackEventDataType;

class TSCVideoTrackEventData
{
public:
    TSCVideoTrackEventData(TSCVideoTrackEventDataType dataType,
                           const cricket::VideoFrame* frame = nullptr);
    TSCVideoTrackEventData(TSCVideoTrackEventDataType dataType,
                           int width, int height);
    
    virtual ~TSCVideoTrackEventData();
    
    TSCVideoTrackEventDataType getType() const;
    
    int getFrameWidth() const;
    int getFrameHeight() const;
    
    const cricket::VideoFrame* getFrame() const;
    
private:
    
    TSCVideoTrackEventData();
    TSCVideoTrackEventData(const TSCVideoTrackEventData&);
    TSCVideoTrackEventData& operator=(TSCVideoTrackEventData&);
    
    TSCVideoTrackEventDataType m_type;
    talk_base::scoped_ptr<cricket::VideoFrame> m_frame;
    
    int m_frame_width;
    int m_frame_height;
};
    
    
}

#endif // TSC_VIDEO_TRACK_EVENT_DATA_H
