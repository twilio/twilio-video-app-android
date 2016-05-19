#include <jni.h>

#ifndef COM_TWILIO_CONVERSATIONS_APPLICATIONFOREGROUNDTRACKER_H
#define COM_TWILIO_CONVERSATIONS_APPLICATIONFOREGROUNDTRACKER_H
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_twilio_conversations_ApplicationForegroundTracker
 * Method:    onApplicationForeground
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_ApplicationForegroundTracker_nativeOnApplicationForeground
        (JNIEnv *, jobject);

/*
 * Class:     com_twilio_conversations_ApplicationForegroundTracker
 * Method:    onApplicationBackground
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_ApplicationForegroundTracker_nativeOnApplicationBackground
        (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif //SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_APPLICATIONFOREGROUNDTRACKER_H
