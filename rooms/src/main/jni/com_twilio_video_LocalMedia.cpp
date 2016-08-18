#include "com_twilio_video_LocalMedia.h"

namespace twilio {
namespace media {

JNIEXPORT void JNICALL Java_com_twilio_video_LocalMedia_nativeRelease(JNIEnv *jni,
                                                                      jobject j_local_media,
                                                                      jlong local_media_handle) {
    LocalMediaContext* local_media_context =
            reinterpret_cast<LocalMediaContext *>(local_media_handle);

    delete local_media_context;
}

}
}