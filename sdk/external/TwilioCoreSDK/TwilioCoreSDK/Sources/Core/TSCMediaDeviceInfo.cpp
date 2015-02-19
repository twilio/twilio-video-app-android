//
//  TSCMediaDeviceInfo.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCMediaDeviceInfo.h"


namespace twiliosdk {

TSCMediaDeviceInfo::TSCMediaDeviceInfo(const std::string& deviceName,
                                       const std::string& deviceId)
{
    m_device_name = deviceName;
    m_device_id = deviceId;
}

TSCMediaDeviceInfo::~TSCMediaDeviceInfo()
{
}

#pragma mark-
    
TSCMediaDeviceInfo::TSCMediaDeviceInfo(const TSCMediaDeviceInfo& object)
{
    m_device_name = object.getDeviceName();
    m_device_id = object.getDeviceId();
}

TSCMediaDeviceInfo&
TSCMediaDeviceInfo::operator=(const TSCMediaDeviceInfo& object)
{
    if (this != &object)
    {
        m_device_name = object.getDeviceName();
        m_device_id = object.getDeviceId();
    }
    return *this;
}
    
#pragma mark-
    
const bool
TSCMediaDeviceInfo::isValid() const
{
    return !getDeviceId().empty();
}

const std::string&
TSCMediaDeviceInfo::getDeviceName() const
{
    return m_device_name;
}

const std::string&
TSCMediaDeviceInfo::getDeviceId() const
{
    return m_device_id;
}

#pragma mark-

TSCVideoCaptureDeviceInfo::TSCVideoCaptureDeviceInfo(const std::string& deviceName,
                                                     const std::string& deviceId,
                                                     const TSCVideoCaptureDeviceType deviceType) :
    TSCMediaDeviceInfo(deviceName, deviceId), m_device_type(deviceType)
{
}

TSCVideoCaptureDeviceInfo::~TSCVideoCaptureDeviceInfo()
{
}

#pragma mark-
    
TSCVideoCaptureDeviceInfo::TSCVideoCaptureDeviceInfo(const TSCVideoCaptureDeviceInfo& object):
    TSCMediaDeviceInfo(object)
{
    m_device_type = object.getDeviceType();
}

TSCVideoCaptureDeviceInfo&
TSCVideoCaptureDeviceInfo::operator=(const TSCVideoCaptureDeviceInfo& object)
{
    TSCMediaDeviceInfo::operator=(object);
    m_device_type = object.getDeviceType();
    return *this;
}
    
const TSCVideoCaptureDeviceType
TSCVideoCaptureDeviceInfo::getDeviceType() const
{
    return m_device_type;
}

#pragma mark-
    
TSCAudioInputDeviceInfo::TSCAudioInputDeviceInfo(const std::string& deviceName,
                                                 const std::string& deviceId):
    TSCMediaDeviceInfo(deviceName, deviceId)
{
}

TSCAudioInputDeviceInfo::~TSCAudioInputDeviceInfo()
{
}
    
#pragma mark-
    
TSCAudioInputDeviceInfo::TSCAudioInputDeviceInfo(const TSCAudioInputDeviceInfo& object):
    TSCMediaDeviceInfo(object)
{
}

TSCAudioInputDeviceInfo&
TSCAudioInputDeviceInfo::operator=(const TSCAudioInputDeviceInfo& object)
{
    TSCMediaDeviceInfo::operator=(object);
    return *this;
}
    
}