#include "com_twilio_video_I420Frame.h"
#include "webrtc/media/base/videoframe.h"

namespace twilio_video_jni {

JNIEXPORT void JNICALL Java_com_twilio_video_I420Frame_nativeRelease(JNIEnv *jni,
                                                                     jobject j_i420_frame,
                                                                     jlong i420_frame_pointer) {
    delete reinterpret_cast<const cricket::VideoFrame*>(i420_frame_pointer);
}

}
