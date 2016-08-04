#ifndef ANDROID_VIDEO_CODEC_MANAGER_H
#define ANDROID_VIDEO_CODEC_MANAGER_H

#include <jni.h>
#include <string.h>
#include "ITSCVideoCodec.h"

#include "webrtc/api/java/jni/jni_helpers.h"

using namespace webrtc_jni;
using namespace twiliosdk;

class AndroidVideoCodecManager : public ITSCVideoCodec {

public:
    static const std::string videoCodecManagerName;

    AndroidVideoCodecManager();

    virtual ~AndroidVideoCodecManager() {}
    virtual cricket::WebRtcVideoEncoderFactory* createVideoEncoderFactory();
    virtual cricket::WebRtcVideoDecoderFactory* createVideoDecoderFactory();
    virtual const std::string getName();

};

#endif //ANDROID_VIDEO_CODEC_MANAGER_H
