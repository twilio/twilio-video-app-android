
#include <jni.h>
#ifndef SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_IMPL_WAKEUPRECEIVER_H
#define SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_IMPL_WAKEUPRECEIVER_H
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_twilio_conversations_impl_WakeUpReceiver
 * Method:    onApplicationWakeUp
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_WakeUpReceiver_nativeOnApplicationWakeUp
(JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif //SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_IMPL_WAKEUPRECEIVER_H
