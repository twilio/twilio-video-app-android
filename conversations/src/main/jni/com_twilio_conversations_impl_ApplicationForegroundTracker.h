#include <jni.h>

#ifndef SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_IMPL_APPLICATIONFOREGROUNDTRACKER_H
#define SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_IMPL_APPLICATIONFOREGROUNDTRACKER_H
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_twilio_conversations_impl_TwilioConversationsImpl
 * Method:    onApplicationForeground
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ApplicationForegroundTracker_onApplicationForeground
        (JNIEnv *, jobject);



/*
 * Class:     com_twilio_conversations_impl_TwilioConversationsImpl
 * Method:    onApplicationBackground
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_ApplicationForegroundTracker_onApplicationBackground
        (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif //SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_IMPL_APPLICATIONFOREGROUNDTRACKER_H
