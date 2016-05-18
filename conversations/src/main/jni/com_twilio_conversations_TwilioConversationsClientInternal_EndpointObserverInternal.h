#include <jni.h>

#ifndef _Included_com_twilio_conversations_ConversationsClient_EndpointObserverInternal
#define _Included_com_twilio_conversations_ConversationsClient_EndpointObserverInternal
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_twilio_conversations_ConversationsClient_EndpointObserverInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/conversations/core/EndpointObserver;Lcom/twilio/conversations/Endpoint;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_conversations_TwilioConversationsClientInternal_00024EndpointObserverInternal_nativeWrapObserver
  (JNIEnv *, jobject, jobject, jobject);

/*
 * Class:     com_twilio_conversations_ConversationsClient_EndpointObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClientInternal_00024EndpointObserverInternal_nativeFreeObserver
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
