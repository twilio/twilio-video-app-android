//
//  TSCMediaDeviceInfo.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/21/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_MEDIA_DEVICE_INFO_H
#define TSC_MEDIA_DEVICE_INFO_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class TSCMediaDeviceInfo
{
public:

    TSCMediaDeviceInfo(const std::string& deviceName,
                       const std::string& deviceId);
    virtual ~TSCMediaDeviceInfo();
    
    TSCMediaDeviceInfo(const TSCMediaDeviceInfo&);
    TSCMediaDeviceInfo& operator=(const TSCMediaDeviceInfo&);
    
    const bool isValid() const;
    
    const std::string& getDeviceName() const;
    const std::string& getDeviceId() const;
    
private:
    
    TSCMediaDeviceInfo();
    
    std::string m_device_name;
    std::string m_device_id;
};

    
class TSCVideoCaptureDeviceInfo: public TSCMediaDeviceInfo
{
public:
    
    TSCVideoCaptureDeviceInfo(const std::string& deviceName,
                              const std::string& deviceId,
                              const TSCVideoCaptureDeviceType deviceType);
    virtual ~TSCVideoCaptureDeviceInfo();
    
    TSCVideoCaptureDeviceInfo(const TSCVideoCaptureDeviceInfo&);
    TSCVideoCaptureDeviceInfo& operator=(const TSCVideoCaptureDeviceInfo&);
    
    const TSCVideoCaptureDeviceType getDeviceType() const;
private:
    
    TSCVideoCaptureDeviceInfo();
    TSCVideoCaptureDeviceType m_device_type;
};
    

class TSCAudioInputDeviceInfo: public TSCMediaDeviceInfo
{
public:
    
    TSCAudioInputDeviceInfo(const std::string& deviceName,
                            const std::string& deviceId);
    virtual ~TSCAudioInputDeviceInfo();
    
    TSCAudioInputDeviceInfo(const TSCAudioInputDeviceInfo&);
    TSCAudioInputDeviceInfo& operator=(const TSCAudioInputDeviceInfo&);
    
private:
    
    TSCAudioInputDeviceInfo();
};
    
}

#endif // TSC_MEDIA_DEVICE_INFO_H
