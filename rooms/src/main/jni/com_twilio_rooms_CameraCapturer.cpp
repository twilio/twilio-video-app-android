#include "webrtc/api/java/jni/androidvideocapturer_jni.h"
#include "webrtc/api/java/jni/classreferenceholder.h"
#include "webrtc/api/androidvideocapturer.h"
#include "com_twilio_rooms_CameraCapturer.h"
#include "webrtc/api/java/jni/jni_helpers.h"
#include "TSCLogger.h"

using namespace twiliosdk;
using namespace webrtc_jni;

#define TAG  "TwilioSDK(native)"

JNIEXPORT void JNICALL Java_com_twilio_rooms_CameraCapturer_nativeStopVideoSource
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "stopVideoSource");
    // TODO: Implement stopping video source
    //TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    //session->get()->stopVideoSource();
}


JNIEXPORT void JNICALL Java_com_twilio_rooms_CameraCapturer_nativeRestartVideoSource
        (JNIEnv *env, jobject obj, jlong nativeSession)
{
    TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "stopVideoSource");
    // TODO: Implement restarting video source
    //TSCSessionPtr *session = reinterpret_cast<TSCSessionPtr *>(nativeSession);
    //session->get()->restartVideoSource();
}

JNIEXPORT jlong JNICALL
Java_com_twilio_rooms_CameraCapturer_nativeCreateNativeCapturer(JNIEnv *env,
                                                                        jobject instance,
                                                                        jobject j_video_capturer,
                                                                        jobject j_egl_context) {
    rtc::scoped_refptr<webrtc::AndroidVideoCapturerDelegate> delegate =
            new rtc::RefCountedObject<AndroidVideoCapturerJni>(env, j_video_capturer, j_egl_context);
    std::unique_ptr<cricket::VideoCapturer> capturer(new webrtc::AndroidVideoCapturer(delegate));
    return jlongFromPointer(capturer.release());
}

JNIEXPORT void JNICALL
Java_com_twilio_rooms_CameraCapturer_nativeDisposeCapturer(JNIEnv *env,
                                                                        jobject instance,
                                                                        jlong nativeVideoCapturerAndroid) {
    webrtc::AndroidVideoCapturer *capturer =
            reinterpret_cast<webrtc::AndroidVideoCapturer *>(nativeVideoCapturerAndroid);
    delete capturer;
}
