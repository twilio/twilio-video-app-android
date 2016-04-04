//
// Created by Aaron Alaniz on 4/1/16.
//

#ifndef ANDROID_VIDEO_CODEC_MANAGER_H
#define ANDROID_VIDEO_CODEC_MANAGER_H

#include <jni.h>
#include <string.h>
#include "TSCoreSDKTypes.h"
#include "ITSCVideoCodec.h"

#include "talk/app/webrtc/java/jni/jni_helpers.h"

using namespace webrtc_jni;
using namespace twiliosdk;

class AndroidVideoCodecManager : public ITSCVideoCodec {

public:
    static const std::string videoCodecName;

    AndroidVideoCodecManager();

    virtual ~AndroidVideoCodecManager() {}
    virtual cricket::WebRtcVideoEncoderFactory* createVideoEncoderFactory();
    virtual cricket::WebRtcVideoDecoderFactory* createVideoDecoderFactory();
    virtual const std::string getName();

};

#endif //ANDROID_VIDEO_CODEC_MANAGER_H
