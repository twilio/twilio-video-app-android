#include <jni.h>

#ifndef _Included_com_twilio_conversations_Client
#define _Included_com_twilio_conversations_Client

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_twilio_rooms_RoomsClient_nativeSetCoreLogLevel
        (JNIEnv *, jobject, jint);

JNIEXPORT void JNICALL Java_com_twilio_rooms_RoomsClient_nativeSetModuleLevel
        (JNIEnv *, jobject, jint, jint);

JNIEXPORT jint JNICALL Java_com_twilio_rooms_RoomsClient_nativeGetCoreLogLevel
        (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
