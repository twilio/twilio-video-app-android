#include "android_video_codec_manager.h"
#include "talk/app/webrtc/java/jni/androidmediadecoder_jni.h"
#include "talk/app/webrtc/java/jni/androidmediaencoder_jni.h"

AndroidVideoCodecManager::AndroidVideoCodecManager() { }

cricket::WebRtcVideoEncoderFactory*AndroidVideoCodecManager::createVideoEncoderFactory() {
    return new MediaCodecVideoEncoderFactory();
}

cricket::WebRtcVideoDecoderFactory*AndroidVideoCodecManager::createVideoDecoderFactory() {
    return new MediaCodecVideoDecoderFactory();
}

const std::string AndroidVideoCodecManager::getName() {
    return "AndroidVideoCodecManager";
}
