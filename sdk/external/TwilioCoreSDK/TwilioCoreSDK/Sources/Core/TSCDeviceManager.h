//
//  TSCDeviceManager.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/24/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_DEVICE_MANAGER_H
#define TSC_DEVICE_MANAGER_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {
    
class TSCDeviceManager
{
public:
    TSCDeviceManager();
    virtual ~TSCDeviceManager();
    
    std::vector<TSCVideoCaptureDeviceInfo> getVideoCaptureDevices() const;
    std::vector<TSCAudioInputDeviceInfo> getAudioCaptureDevices() const;
    
    void setDefaultVideoCaptureDevice(const TSCVideoCaptureDeviceInfo& device);
    const TSCVideoCaptureDeviceInfo getDefaultVideoCaptureDevice() const;
    
    void setDefaultAudioInputDevice(const TSCAudioInputDeviceInfo& device);
    const TSCAudioInputDeviceInfo getDefaultAudioInputDevice() const;
    
    void setScreenCaptureProvider(const TSCScreenCaptureProviderRef& provider);
    const TSCScreenCaptureProviderRef getScreenCaptureProvider() const;
    
private:
    TSCDeviceManager(const TSCDeviceManager&);
    TSCDeviceManager& operator=(TSCDeviceManager&);
    
    class TImpl;
};
    
}

#endif //TSC_LOGGER_H
