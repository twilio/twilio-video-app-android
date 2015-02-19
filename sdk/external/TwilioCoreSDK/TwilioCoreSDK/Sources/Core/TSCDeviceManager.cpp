//
//  TSCDeviceManager.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/24/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCDeviceManager.h"
#include "TSCMediaDeviceInfo.h"
#include "TSCScreenCaptureProvider.h"
#include "TSCoreConstants.h"


#include "talk/media/devices/devicemanager.h"

namespace twiliosdk {

class TSCDeviceManager::TImpl
{
public:
    
    static TImpl& instance()
    {
        static TImpl instance;
        return instance;
    }
    
    ~TImpl()
    {
    }
    
    std::vector<TSCVideoCaptureDeviceInfo> getVideoCaptureDevices() const;
    std::vector<TSCAudioInputDeviceInfo> getAudioCaptureDevices() const;
    
    void setDefaultVideoCaptureDevice(const TSCVideoCaptureDeviceInfo& device);
    const TSCVideoCaptureDeviceInfo getDefaultVideoCaptureDevice() const;
    
    void setDefaultAudioInputDevice(const TSCAudioInputDeviceInfo& device);
    const TSCAudioInputDeviceInfo getDefaultAudioInputDevice() const;
    
    void setScreenCaptureProvider(const TSCScreenCaptureProviderRef& provider);
    const TSCScreenCaptureProviderRef getScreenCaptureProvider() const;
    
private:
    TImpl() :
        m_default_video_capture_device("", "", kTSCVideoCaptureNone), m_default_audio_input_device("", "")
    {
        m_device_manager.reset((cricket::DeviceManager*)cricket::DeviceManagerFactory::Create());
    }
    
    TSCVideoCaptureDeviceInfo m_default_video_capture_device;
    TSCAudioInputDeviceInfo m_default_audio_input_device;
    talk_base::scoped_ptr<cricket::DeviceManager> m_device_manager;
    TSCScreenCaptureProviderRef m_screen_capture_provider;
};

#pragma mark-

std::vector<TSCVideoCaptureDeviceInfo>
TSCDeviceManager::TImpl::getVideoCaptureDevices() const
{
    std::vector<TSCVideoCaptureDeviceInfo> collection;
    
    std::vector<cricket::Device> devices;
    m_device_manager->GetVideoCaptureDevices(&devices);
    
    for(auto &device : devices)
    {
        TSCVideoCaptureDeviceInfo info(device.name, device.id, kTSCVideoCaptureNone);
        collection.push_back(info);
    }
    if (m_screen_capture_provider.get())
    {
        TSCVideoCaptureDeviceInfo info(kTSCScreenShareDeviceName,
                                       m_screen_capture_provider->getId(), kTSCVideoCaptureScreen);
        collection.push_back(info);
    }
    return collection;
}

std::vector<TSCAudioInputDeviceInfo>
TSCDeviceManager::TImpl::getAudioCaptureDevices() const
{
    std::vector<TSCAudioInputDeviceInfo> collection;
    
    std::vector<cricket::Device> devices;
    m_device_manager->GetAudioInputDevices(&devices);
    
    for(auto &device : devices)
    {
        TSCAudioInputDeviceInfo info(device.name, device.id);
        collection.push_back(info);
    }
    return collection;
}
    
#pragma mark-
    
void
TSCDeviceManager::TImpl::setDefaultVideoCaptureDevice(const TSCVideoCaptureDeviceInfo& device)
{
    m_default_video_capture_device = device;
}

const TSCVideoCaptureDeviceInfo
TSCDeviceManager::TImpl::getDefaultVideoCaptureDevice() const
{
    const std::vector<TSCVideoCaptureDeviceInfo> devices = getVideoCaptureDevices();
    
    TSCVideoCaptureDeviceInfo result = m_default_video_capture_device;
    if (result.isValid()) {
        // return user-set device if its still available
        for (auto &device: devices) {
            if (device.getDeviceId() == result.getDeviceId()) {
                return result;
            }
        }
    }
    // user-set device was not found in the system, try first available
    if (devices.size() > 0) {
        // for iOS devices select front camera, which is second
        result = devices.size() > 1 ? devices[1] : devices[0];
    }
    return result;
}

#pragma mark-

void
TSCDeviceManager::TImpl::setDefaultAudioInputDevice(const TSCAudioInputDeviceInfo& device)
{
    m_default_audio_input_device = device;
}

const TSCAudioInputDeviceInfo
TSCDeviceManager::TImpl::getDefaultAudioInputDevice() const
{
    const std::vector<TSCAudioInputDeviceInfo> devices = getAudioCaptureDevices();
    
    TSCAudioInputDeviceInfo result = m_default_audio_input_device;
    if (result.isValid()) {
        // return user-set device if its still available
        for (auto &device: devices) {
            if (device.getDeviceId() == result.getDeviceId()) {
                return result;
            }
        }
    }
    // user-set device was not found in the system, try first available
    if (devices.size() > 0) {
        result = devices[0];
    }
    return result;
}
    
#pragma mark-
    
void
TSCDeviceManager::TImpl::setScreenCaptureProvider(const TSCScreenCaptureProviderRef& provider)
{
    m_screen_capture_provider = provider;
}

const TSCScreenCaptureProviderRef
TSCDeviceManager::TImpl::getScreenCaptureProvider() const
{
    return m_screen_capture_provider;
}

#pragma mark-
    
TSCDeviceManager::TSCDeviceManager()
{
}

TSCDeviceManager::~TSCDeviceManager()
{
}
    
#pragma mark-

std::vector<TSCVideoCaptureDeviceInfo>
TSCDeviceManager::getVideoCaptureDevices() const
{
    return TImpl::instance().getVideoCaptureDevices();
}

std::vector<TSCAudioInputDeviceInfo>
TSCDeviceManager::getAudioCaptureDevices() const
{
    return TImpl::instance().getAudioCaptureDevices();
}

#pragma mark-
    
void
TSCDeviceManager::setDefaultVideoCaptureDevice(const TSCVideoCaptureDeviceInfo& device)
{
    TImpl::instance().setDefaultVideoCaptureDevice(device);
}

const TSCVideoCaptureDeviceInfo
TSCDeviceManager::getDefaultVideoCaptureDevice() const
{
    return TImpl::instance().getDefaultVideoCaptureDevice();
}
    
#pragma mark-
    
void
TSCDeviceManager::setDefaultAudioInputDevice(const TSCAudioInputDeviceInfo& device)
{
    TImpl::instance().setDefaultAudioInputDevice(device);
}

const TSCAudioInputDeviceInfo
TSCDeviceManager::getDefaultAudioInputDevice() const
{
    return TImpl::instance().getDefaultAudioInputDevice();
}
  
#pragma mark-

void
TSCDeviceManager::setScreenCaptureProvider(const TSCScreenCaptureProviderRef& provider)
{
    TImpl::instance().setScreenCaptureProvider(provider);
}
    
const TSCScreenCaptureProviderRef
TSCDeviceManager::getScreenCaptureProvider() const
{
    return TImpl::instance().getScreenCaptureProvider();
}
    
}