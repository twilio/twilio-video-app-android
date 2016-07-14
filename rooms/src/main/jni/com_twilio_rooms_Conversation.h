#include <jni.h>

#ifndef _Included_com_twilio_conversations_Conversation
#define _Included_com_twilio_conversations_Conversation

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_twilio_rooms_Conversation_nativeWrapOutgoingSession
  (JNIEnv *, jobject, jlong, jlong, jobjectArray);

JNIEXPORT void JNICALL Java_com_twilio_rooms_Conversation_nativeStart
  (JNIEnv *, jobject, jlong, jboolean, jboolean, jboolean, jboolean, jobject, jobjectArray, jobject);

JNIEXPORT void JNICALL Java_com_twilio_rooms_Conversation_nativeStop
  (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_rooms_Conversation_nativeSetExternalCapturer
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT void JNICALL Java_com_twilio_rooms_Conversation_nativeSetSessionObserver
  (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT void JNICALL Java_com_twilio_rooms_Conversation_nativeFreeHandle
  (JNIEnv *, jobject, jlong);

JNIEXPORT jboolean JNICALL Java_com_twilio_rooms_Conversation_nativeEnableVideo
  (JNIEnv *, jobject, jlong, jboolean, jboolean, jobject);

JNIEXPORT jboolean JNICALL Java_com_twilio_rooms_Conversation_nativeMute
  (JNIEnv *, jobject, jlong, jboolean);

JNIEXPORT jboolean JNICALL Java_com_twilio_rooms_Conversation_nativeIsMuted
  (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_rooms_Conversation_nativeInviteParticipants
  (JNIEnv *, jobject, jlong, jobjectArray);

JNIEXPORT jstring JNICALL Java_com_twilio_rooms_Conversation_nativeGetConversationSid
  (JNIEnv *, jobject, jlong);

JNIEXPORT jboolean JNICALL Java_com_twilio_rooms_Conversation_nativeEnableAudio
  (JNIEnv *, jobject, jlong, jboolean, jboolean);


#ifdef __cplusplus
}
#endif
#endif
