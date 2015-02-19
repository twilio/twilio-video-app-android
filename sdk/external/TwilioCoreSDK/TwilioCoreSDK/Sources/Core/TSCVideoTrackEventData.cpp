//
//  TSCVideoTrackEventData.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCVideoTrackEventData.h"

namespace twiliosdk {

TSCVideoTrackEventData::TSCVideoTrackEventData(TSCVideoTrackEventDataType dataType,
                                               const cricket::VideoFrame* frame)
{
    m_type = dataType;
    m_frame.reset(frame->Copy());
    m_frame_width = 0;
    m_frame_height = 0;
}

TSCVideoTrackEventData::TSCVideoTrackEventData(TSCVideoTrackEventDataType dataType,
                                               int width, int height)
{
    m_type = dataType;
    m_frame_width = width;
    m_frame_height = height;
}
    
TSCVideoTrackEventData::~TSCVideoTrackEventData()
{
}

#pragma mark-

TSCVideoTrackEventDataType
TSCVideoTrackEventData::getType() const
{
    return m_type;
}

int
TSCVideoTrackEventData::getFrameWidth() const
{
    return m_frame_width;
}

int
TSCVideoTrackEventData::getFrameHeight() const
{
    return m_frame_height;
}

const cricket::VideoFrame*
TSCVideoTrackEventData::getFrame() const
{
    return m_frame.get();
}

}