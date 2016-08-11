#include "com_twilio_video_Participant.h"

JNIEXPORT jstring JNICALL
Java_com_twilio_video_Participant_nativeGetIdentity(JNIEnv *env, jobject instance,
                                                    jlong nativeHandle) {

    // TODO


    return env->NewStringUTF("");
}

JNIEXPORT jstring JNICALL
Java_com_twilio_video_Participant_nativeSid(JNIEnv *env, jobject instance, jlong nativeHandle) {

    // TODO


    return env->NewStringUTF("");
}

JNIEXPORT jboolean JNICALL
Java_com_twilio_video_Participant_nativeIsConnected(JNIEnv *env, jobject instance,
                                                    jlong nativeHandle) {

    // TODO
    return true;
}