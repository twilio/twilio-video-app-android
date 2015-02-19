#ifndef TWILIOWEBRTCCONSTRAINTS_H
#define TWILIOWEBRTCCONSTRAINTS_H

#include <string>
#include <vector>

#include "talk/base/stringencode.h"

#include "talk/app/webrtc/mediaconstraintsinterface.h"

#include "twilioLogger.h"

namespace twiliosdk {

class TwilioWebrtcConstraints : public webrtc::MediaConstraintsInterface {

 public:
    TwilioWebrtcConstraints() {
    }
    virtual ~TwilioWebrtcConstraints() {
    }

    virtual const Constraints& GetMandatory() const {
        return mandatory_;
    }
    virtual const Constraints& GetOptional() const {
        return optional_;
    }

    template<class T>
    void AddMandatory(const std::string& key, const T& value) {
        mandatory_.push_back(Constraint(key, talk_base::ToString<T>(value)));
    }

    template<class T>
    void SetMandatory(const std::string& key, const T& value) {
        std::string value_str;
        if (mandatory_.FindFirst(key, &value_str)) {
            for (Constraints::iterator iter = mandatory_.begin();
                    iter != mandatory_.end(); ++iter) {
                if (iter->key == key) {
                    LOG_DEBUG_STREAM << "key present: " << iter->value << std::endl;
                    mandatory_.erase(iter);
                    break;
                }
            }
        }
        mandatory_.push_back(Constraint(key, talk_base::ToString<T>(value)));
    }

    template<class T>
    void AddOptional(const std::string& key, const T& value) {
        optional_.push_back(Constraint(key, talk_base::ToString<T>(value)));
    }

    void SetMandatoryMinAspectRatio(double ratio) {
        SetMandatory(MediaConstraintsInterface::kMinAspectRatio, ratio);
        LOG_DEBUG_STREAM << "Setting mandatory min aspect ratio to " << ratio << std::endl;
    }
    
    void SetOptionalMinAspectRatio(double ratio) {
        AddOptional(MediaConstraintsInterface::kMinAspectRatio, ratio);
        LOG_DEBUG_STREAM << "Setting optional min aspect ratio to " << ratio << std::endl;
    }
    
    void SetMandatoryMaxAspectRatio(double ratio) {
        SetMandatory(MediaConstraintsInterface::kMaxAspectRatio, ratio);
        LOG_DEBUG_STREAM << "Setting mandatory max aspect ratio to " << ratio << std::endl;
    }
    
    void SetOptionalMaxAspectRatio(double ratio) {
        AddOptional(MediaConstraintsInterface::kMaxAspectRatio, ratio);
        LOG_DEBUG_STREAM << "Setting optional max aspect ratio to " << ratio << std::endl;
    }
    
    void SetMandatoryMinWidth(int width) {
        SetMandatory(MediaConstraintsInterface::kMinWidth, width);
        LOG_DEBUG_STREAM << "Setting mandatory min width to " << width << std::endl;
    }

    void SetOptionalMinWidth(int width) {
        AddOptional(MediaConstraintsInterface::kMinWidth, width);
        LOG_DEBUG_STREAM << "Setting optional min width to " << width << std::endl;
    }

    void SetMandatoryMaxWidth(int width) {
        SetMandatory(MediaConstraintsInterface::kMaxWidth, width);
        LOG_DEBUG_STREAM << "Setting mandatory max width to " << width << std::endl;
    }

    void SetOptionalMaxWidth(int width) {
        AddOptional(MediaConstraintsInterface::kMaxWidth, width);
        LOG_DEBUG_STREAM << "Setting optional max width to " << width << std::endl;
    }

    void SetMandatoryMinHeight(int height) {
        SetMandatory(MediaConstraintsInterface::kMinHeight, height);
        LOG_DEBUG_STREAM << "Setting mandatory min height to " << height << std::endl;
    }

    void SetOptionalMinHeight(int height) {
        AddOptional(MediaConstraintsInterface::kMinHeight, height);
        LOG_DEBUG_STREAM << "Setting optional min height to " << height << std::endl;
    }

    void SetMandatoryMaxHeight(int height) {
        SetMandatory(MediaConstraintsInterface::kMaxHeight, height);
        LOG_DEBUG_STREAM << "Setting mandatory max height to " << height << std::endl;
    }

    void SetOptionalMaxHeight(int height) {
        AddOptional(MediaConstraintsInterface::kMaxHeight, height);
        LOG_DEBUG_STREAM << "Setting optional max height to " << height << std::endl;
    }

    void SetMandatoryMinFrameRate(int framerate) {
        SetMandatory(MediaConstraintsInterface::kMinFrameRate, framerate);
        LOG_DEBUG_STREAM << "Setting mandatory min framerate to " << framerate << std::endl;
    }

    void SetOptionalMinFrameRate(int framerate) {
        AddOptional(MediaConstraintsInterface::kMinFrameRate, framerate);
        LOG_DEBUG_STREAM << "Setting optional min framerate to " << framerate << std::endl;
    }

    void SetMandatoryMaxFrameRate(int framerate) {
        SetMandatory(MediaConstraintsInterface::kMaxFrameRate, framerate);
        LOG_DEBUG_STREAM << "Setting mandatory max framerate to " << framerate << std::endl;
    }

    void SetOptionalMaxFrameRate(int framerate) {
        AddOptional(MediaConstraintsInterface::kMaxFrameRate, framerate);
        LOG_DEBUG_STREAM << "Setting optional max framerate to " << framerate << std::endl;
    }

    void SetMandatoryReceiveAudio(bool enable) {
        SetMandatory(MediaConstraintsInterface::kOfferToReceiveAudio, enable);
    }

    void SetMandatoryReceiveVideo(bool enable) {
        SetMandatory(MediaConstraintsInterface::kOfferToReceiveVideo, enable);
    }

    void SetMandatoryUseRtpMux(bool enable) {
        SetMandatory(MediaConstraintsInterface::kUseRtpMux, enable);
    }

    void SetMandatoryIceRestart(bool enable) {
        SetMandatory(MediaConstraintsInterface::kIceRestart, enable);
    }

    void SetAllowRtpDataChannels() {
        SetMandatory(MediaConstraintsInterface::kEnableRtpDataChannels, true);
    }

    void SetOptionalVAD(bool enable) {
        AddOptional(MediaConstraintsInterface::kVoiceActivityDetection, enable);
    }

    void SetAllowDtlsSctpDataChannels() {
        SetMandatory(MediaConstraintsInterface::kEnableDtlsSrtp, true);
    }

 private:
    Constraints mandatory_;
    Constraints optional_;
};

}  // namespace twiliosdk

#endif // TWILIOWEBRTCCONSTRAINTS_H
