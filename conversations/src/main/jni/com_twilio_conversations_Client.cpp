#include "com_twilio_conversations_Client.h"
#include "webrtc/api/java/jni/jni_helpers.h"

using namespace webrtc_jni;


JNIEXPORT void JNICALL Java_com_twilio_conversations_Client_nativeSetCoreLogLevel
        (JNIEnv *, jobject, jint) {
    // TODO: implement me
}

JNIEXPORT void JNICALL Java_com_twilio_conversations_Client_nativeSetModuleLevel
        (JNIEnv *, jobject, jint, jint) {
    // TODO: implement me
}

JNIEXPORT jint JNICALL Java_com_twilio_conversations_Client_nativeGetCoreLogLevel
        (JNIEnv *, jobject) {
    return 0;
}
