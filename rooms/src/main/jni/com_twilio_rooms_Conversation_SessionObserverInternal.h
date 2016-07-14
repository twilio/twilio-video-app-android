#include <jni.h>

#ifndef _Included_com_twilio_conversations_Conversation_SessionObserverInternal
#define _Included_com_twilio_conversations_Conversation_SessionObserverInternal
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_twilio_conversations_Conversation_SessionObserverInternal
 * Method:    wrapNativeObserver
 * Signature: (Lcom/twilio/rooms/ConversationListener;Lcom/twilio/rooms/Endpoint;)J
 */
JNIEXPORT jlong JNICALL Java_com_twilio_rooms_Conversation_00024SessionObserverInternal_nativeWrapObserver
  (JNIEnv *, jobject, jobject, jobject);

/*
 * Class:     com_twilio_conversations_Conversation_SessionObserverInternal
 * Method:    freeNativeObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_rooms_Conversation_00024SessionObserverInternal_nativeFreeObserver
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_twilio_conversations_Conversation_SessionObserverInternal
 * Method:    enableStats
 * Signature: (JJZ)V
 */
JNIEXPORT void JNICALL Java_com_twilio_rooms_Conversation_00024SessionObserverInternal_nativeEnableStats
        (JNIEnv *, jobject, jlong, jlong, jboolean);

#ifdef __cplusplus
}
#endif
#endif
