#ifndef TWILIODEVICEMANAGER_H_
#define TWILIODEVICEMANAGER_H_

#include <string>
#include <vector>
#include <map>

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/app/webrtc/peerconnectionfactory.h"
#include "talk/media/devices/devicemanager.h"
#include "talk/session/media/channelmanager.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twiliosdk.h"

namespace twiliosdk {
//!  TwilioDeviceManager class.
/*!
  Device Manager for basic operations with media input/output devices.
 */
class TwilioDeviceManager {
    friend class TwilioVideoCapturer;
public:

    //! Device Manager single instance getter
    /*!
      \return single Device Manager instance
     */
    static TwilioDeviceManager& instance();

    void init(webrtc::PeerConnectionFactory* pcf);

    //! Lists available devices for provided media type
    /*!
          \param type Media type to list devices for.
          \param devices reference to devices vector to fill
     */
    void listDevices(TwilioSdkMediaType type, std::vector<std::string>& devices);

    //! Sets current device for provided media type.
    /*!
      \param type Media type to set device for.
      \param device name to set
      \return True if device was changed
     */
    bool setCurrentDevice(TwilioSdkMediaType type, const std::string& device);

    //! Gets current device for provided media type.
    /*!
      \param type Media type to get device for.
      \param device name reference to fill
      \return The test results
     */
    void getCurrentDevice(TwilioSdkMediaType type, std::string& device);

protected:
    TwilioDeviceManager();
    virtual ~TwilioDeviceManager();

    bool getDevice(TwilioSdkMediaType type, std::string& name, cricket::Device& device);
    bool getDeviceId(TwilioSdkMediaType type, std::string& name, std::string& id);

private:
    std::map<TwilioSdkMediaType, std::string> current_devices_;
    talk_base::scoped_ptr<cricket::DeviceManager> dm_;
    talk_base::scoped_refptr<webrtc::PeerConnectionFactory> peer_connection_factory_;
};

}

#endif /* TWILIODEVICEMANAGER_H_ */
