#include <jni.h>

#ifndef SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_TWILIOCONVERSATIONSCLIENT_H
#define SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_TWILIOCONVERSATIONSCLIENT_H
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_twilio_conversations_TwilioConversationsClient
 * Method:    initCore
 * Signature: (Landroid/content/Context;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_twilio_conversations_TwilioConversationsClient_nativeInitCore
        (JNIEnv *, jobject, jobject);

/*
 * Class:     com_twilio_conversations_TwilioConversationsClient
 * Method:    destroyCore
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClient_nativeDestroyCore
        (JNIEnv *, jobject);

/*
 * Class:     com_twilio_conversations_TwilioConversationsClient
 * Method:    setCoreLogLevel 
 * Signature: (I)J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClient_nativeSetCoreLogLevel
        (JNIEnv *, jobject, jint);

/*
 * Class:     com_twilio_conversations_TwilioConversationsClient
 * Method:    setModuleLevel
 * Signature: (II)J
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClient_nativeSetModuleLevel
        (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_twilio_conversations_TwilioConversationsClient
 * Method:    getCoreLogLevel 
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_twilio_conversations_TwilioConversationsClient_nativeGetCoreLogLevel
        (JNIEnv *, jobject);

/*
 * Class:     com_twilio_conversations_TwilioConversationsClient
 * Method:    refreshRegistrations
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_twilio_conversations_TwilioConversationsClient_nativeRefreshRegistrations
        (JNIEnv *, jobject);


#ifdef __cplusplus
}
#endif
#endif //SIGNAL_SDK_ANDROID_COM_TWILIO_CONVERSATIONS_TWILIOCONVERSATIONSCLIENT_H
