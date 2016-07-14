#include <jni.h>

#ifndef _Included_com_twilio_conversations_ConversationsClient
#define _Included_com_twilio_conversations_ConversationsClient
#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT jlong JNICALL
Java_com_twilio_rooms_TwilioConversationsClientInternal_nativeCreateEndpoint
  (JNIEnv *, jobject, jobject, jobjectArray, jlong, jlong);

/*
 * Class:     com_twilio_conversations_ConversationsClient
 * Method:    listen
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_rooms_TwilioConversationsClientInternal_nativeListen
        (JNIEnv *, jobject, jlong);

/*
 * Class:     com_twilio_conversations_ConversationsClient
 * Method:    unlisten
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_rooms_TwilioConversationsClientInternal_nativeUnlisten
        (JNIEnv *, jobject, jlong);

/*
 * Class:     com_twilio_conversations_ConversationsClient
 * Method:    reject
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_twilio_rooms_TwilioConversationsClientInternal_nativeReject
        (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_twilio_conversations_ConversationsClient
 * Method:    freeNativeHandle
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_twilio_rooms_TwilioConversationsClientInternal_nativeFreeHandle
  (JNIEnv *, jobject, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
