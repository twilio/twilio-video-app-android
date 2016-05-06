#include "android_video_codec_manager.h"
#include "webrtc/api/java/jni/androidmediadecoder_jni.h"
#include "webrtc/api/java/jni/androidmediaencoder_jni.h"

const std::string AndroidVideoCodecManager::videoCodecManagerName = "AndroidVideoCodec";

AndroidVideoCodecManager::AndroidVideoCodecManager() { }

cricket::WebRtcVideoEncoderFactory*AndroidVideoCodecManager::createVideoEncoderFactory() {
    return new MediaCodecVideoEncoderFactory();
}

cricket::WebRtcVideoDecoderFactory*AndroidVideoCodecManager::createVideoDecoderFactory() {
    return new MediaCodecVideoDecoderFactory();
}

const std::string AndroidVideoCodecManager::getName() {
    return videoCodecManagerName;
}
