#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/base/common.h"
#include "talk/media/devices/devicemanager.h"
#include "webrtc/modules/video_capture/include/video_capture.h"
#include "webrtc/modules/video_capture/include/video_capture_factory.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twilioDeviceManager.h"

namespace twiliosdk {

TwilioDeviceManager::TwilioDeviceManager() {
    dm_.reset((cricket::DeviceManager*)cricket::DeviceManagerFactory::Create());
    // Fill inintial current devices
    std::vector<std::string> devices;
    listDevices(TYPE_VIDEO_INPUT, devices);
    if (devices.size() > 0) {
        current_devices_[TYPE_VIDEO_INPUT] = devices[0];
    }
    listDevices(TYPE_AUDIO_INPUT, devices);
    if (devices.size() > 0) {
        current_devices_[TYPE_AUDIO_INPUT] = devices[0];
    }
    listDevices(TYPE_AUDIO_OUTPUT, devices);
    if (devices.size() > 0) {
        current_devices_[TYPE_AUDIO_OUTPUT] = devices[0];
    }
}

TwilioDeviceManager::~TwilioDeviceManager() {

}

void TwilioDeviceManager::init(webrtc::PeerConnectionFactory* pcf) {
    peer_connection_factory_ = pcf;
}

TwilioDeviceManager& TwilioDeviceManager::instance() {
    static TwilioDeviceManager manager;
    return manager;
}

void TwilioDeviceManager::listDevices(TwilioSdkMediaType type,
                                      std::vector<std::string>& devices) {
    std::vector<cricket::Device> dcs;
    switch (type) {
        case TYPE_VIDEO_INPUT:
            dm_->GetVideoCaptureDevices(&dcs);
            break;
        case TYPE_AUDIO_INPUT:
            dm_->GetAudioInputDevices(&dcs);
            break;
        case TYPE_AUDIO_OUTPUT:
            dm_->GetAudioOutputDevices(&dcs);
            break;
        case TYPE_UNKNOWN_MEDIA:
        case TYPE_VIDEO_OUTPUT:
        case TYPE_DATA:
        default:
            break;
    }
    devices.clear();
    for (size_t i = 0; i < dcs.size(); ++i) {
        devices.push_back(dcs[i].name);
    }
}

bool TwilioDeviceManager::setCurrentDevice(TwilioSdkMediaType type,
                                           const std::string& device) {
    std::string old = current_devices_[type];
    current_devices_[type] = device;
    if (TYPE_AUDIO_INPUT  == type ||
        TYPE_AUDIO_OUTPUT == type) {
        // Need to set both devices here
        cricket::AudioOptions audio_options;
        peer_connection_factory_->channel_manager()->GetAudioOptions(NULL, NULL, &audio_options);
        peer_connection_factory_->channel_manager()->SetAudioOptions(current_devices_[TYPE_AUDIO_INPUT],
                                                                     current_devices_[TYPE_AUDIO_OUTPUT],
                                                                     audio_options);
    }
    return old != device;
}

void TwilioDeviceManager::getCurrentDevice(TwilioSdkMediaType type,
                                           std::string& device) {
    // Return empty if no current set
    device.clear();
    if (current_devices_.count(type) > 0) {
        device = current_devices_[type];
    }
}

bool TwilioDeviceManager::getDevice(TwilioSdkMediaType type,
                                    std::string& name,
                                    cricket::Device& device) {
    switch (type) {
        case TYPE_VIDEO_INPUT:
            return dm_->GetVideoCaptureDevice(name, &device);
            break;
        case TYPE_AUDIO_INPUT:
            return dm_->GetAudioInputDevice(name, &device);
            break;
        case TYPE_AUDIO_OUTPUT:
            return dm_->GetAudioOutputDevice(name, &device);
            break;
        case TYPE_UNKNOWN_MEDIA:
        case TYPE_VIDEO_OUTPUT:
        case TYPE_DATA:
        default:
            break;
    }
    return false;
}

bool TwilioDeviceManager::getDeviceId(TwilioSdkMediaType type,
                                      std::string& name,
                                      std::string& id) {
    cricket::Device device;
    bool result = getDevice(type, name, device);
    id = device.id;
    return result;
}

}  // namespace twiliosdk
