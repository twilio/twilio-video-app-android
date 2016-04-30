#include <webrtc/api/java/jni/androidvideocapturer_jni.h>
#include <webrtc/api/java/jni/classreferenceholder.h>
#include "webrtc/api/androidvideocapturer.h"
#include "com_twilio_conversations_impl_CameraCapturerImpl.h"
#include "webrtc/api/java/jni/jni_helpers.h"
#include "TSCoreSDKTypes.h"
#include "TSCoreError.h"
#include "TSCLogger.h"
#include "TSCEndpoint.h"
#include "TSCSession.h"

using namespace twiliosdk;
using namespace webrtc_jni;

#define TAG  "TwilioSDK(native)"


JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_CameraCapturerImpl_stopVideoSource
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "stopVideoSource");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    session->get()->stopVideoSource();
}


JNIEXPORT void JNICALL Java_com_twilio_conversations_impl_CameraCapturerImpl_restartVideoSource
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "stopVideoSource");
    TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    session->get()->restartVideoSource();
}

JNIEXPORT jlong JNICALL
Java_com_twilio_conversations_impl_CameraCapturerImpl_createNativeCapturer(JNIEnv *env,
                                                                           jobject instance,
                                                                           jobject j_video_capturer) {
    jobject j_surface_texture_helper =
            env->CallObjectMethod(j_video_capturer,
                                  GetMethodID(env,
                                              FindClass(env, "org/webrtc/VideoCapturer"),
                                              "getSurfaceTextureHelper",
                                              "()Lorg/webrtc/SurfaceTextureHelper;"));
    rtc::scoped_refptr<webrtc::AndroidVideoCapturerDelegate> delegate =
            new rtc::RefCountedObject<AndroidVideoCapturerJni>(env, j_video_capturer, j_surface_texture_helper);
    rtc::scoped_ptr<cricket::VideoCapturer> capturer(new webrtc::AndroidVideoCapturer(delegate));
    return jlongFromPointer(capturer.release());
}

JNIEXPORT void JNICALL
Java_com_twilio_conversations_impl_CameraCapturerImpl_disposeCapturer(JNIEnv *env,
                                                                      jobject instance,
                                                                      jlong nativeVideoCapturerAndroid) {
    webrtc::AndroidVideoCapturer *capturer =
            reinterpret_cast<webrtc::AndroidVideoCapturer *>(nativeVideoCapturerAndroid);
    // XXX: VideoCapturer is owned by video source; I don't think we should delete it ourselves
    //      delete capturer;
}
