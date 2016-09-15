#ifndef VIDEO_ANDROID_INCLUDE_ANDROID_MEDIA_OBSERVER_H_
#define VIDEO_ANDROID_INCLUDE_ANDROID_MEDIA_OBSERVER_H_

#include "webrtc/api/java/jni/jni_helpers.h"

#include "video/logger.h"
#include "media/media_observer.h"
#include "media/media.h"


class AndroidMediaObserver: public twilio::media::MediaObserver {
public:
    AndroidMediaObserver(JNIEnv *env, jobject j_media_observer) :
        j_media_observer_(env, j_media_observer),
        j_media_observer_class_(env, webrtc_jni::GetObjectClass(env, *j_media_observer_)),
        j_audio_track_class_(env, env->FindClass("com/twilio/video/AudioTrack")),
        j_video_track_class_(env, env->FindClass("com/twilio/video/VideoTrack")),
        j_on_audio_track_added_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_observer_class_,
                                    "onAudioTrackAdded",
                                    "(Lcom/twilio/video/AudioTrack;)V")),
        j_on_audio_track_removed_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_observer_class_,
                                    "onAudioTrackRemoved",
                                    "(Ljava/lang/String;)V")),
        j_on_video_track_added_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_observer_class_,
                                    "onVideoTrackAdded",
                                    "(Lcom/twilio/video/VideoTrack;)V")),
        j_on_video_track_removed_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_observer_class_,
                                    "onVideoTrackRemoved",
                                    "(Ljava/lang/String;)V")),
        j_on_audio_track_enabled_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_observer_class_,
                                    "onAudioTrackEnabled",
                                    "(Ljava/lang/String;)V")),
        j_on_audio_track_disabled_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_observer_class_,
                                    "onAudioTrackDisabled",
                                    "(Ljava/lang/String;)V")),
        j_on_video_track_enabled_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_observer_class_,
                                    "onVideoTrackEnabled",
                                    "(Ljava/lang/String;)V")),
        j_on_video_track_disabled_(
            webrtc_jni::GetMethodID(env,
                                    *j_media_observer_class_,
                                    "onVideoTrackDisabled",
                                    "(Ljava/lang/String;)V")),
        j_audio_track_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_audio_track_class_,
                                    "<init>",
                                    "(Ljava/lang/String;Z)V")),
        j_video_track_ctor_id_(
            webrtc_jni::GetMethodID(env,
                                    *j_video_track_class_,
                                    "<init>",
                                    "(Lorg/webrtc/VideoTrack;)V")) {
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "AndroidMediaObserver");
    }

    ~AndroidMediaObserver(){
            TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                               kTSCoreLogLevelDebug,
                               "~AndroidMediaObserver");
    }

    void setObserverDeleted() {
        rtc::CritScope cs(&deletion_lock_);
        observer_deleted_ = true;
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                           kTSCoreLogLevelDebug,
                           "room observer deleted");
    }

protected:
    virtual void onAudioTrackAdded(twilio::media::Media *media,
                                   std::shared_ptr<twilio::media::AudioTrack> track) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jstring j_track_id = webrtc_jni::JavaStringFromStdString(jni(), track->getTrackId());
            jlong j_webrtc_track = webrtc_jni::jlongFromPointer(track->getWebRtcTrack());
            jboolean j_is_enabled = track->isEnabled();

            jobject j_audio_track =
                createJavaAudioTrack(jni(), track, *j_audio_track_class_, j_audio_track_ctor_id_);

            jni()->CallVoidMethod(*j_media_observer_, j_on_audio_track_added_, j_audio_track);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onAudioTrackRemoved(twilio::media::Media *media,
                                     std::shared_ptr<twilio::media::AudioTrack> track) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jstring j_track_id = webrtc_jni::JavaStringFromStdString(jni(), track->getTrackId());

            jni()->CallVoidMethod(*j_media_observer_, j_on_audio_track_removed_, j_track_id);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onVideoTrackAdded(twilio::media::Media *media,
                                   std::shared_ptr<twilio::media::VideoTrack> track) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jobject j_video_track =
                createJavaVideoTrack(jni(), track, *j_video_track_class_, j_video_track_ctor_id_);

            jni()->CallVoidMethod(*j_media_observer_, j_on_video_track_added_, j_video_track);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onVideoTrackRemoved(twilio::media::Media *media,
                                     std::shared_ptr<twilio::media::VideoTrack> track) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jstring j_track_id = webrtc_jni::JavaStringFromStdString(jni(), track->getTrackId());

            jni()->CallVoidMethod(*j_media_observer_, j_on_video_track_removed_, j_track_id);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }
    virtual void onAudioTrackEnabled(twilio::media::Media *media,
                                     std::shared_ptr<twilio::media::AudioTrack> track) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jstring j_track_id = webrtc_jni::JavaStringFromStdString(jni(), track->getTrackId());

            jni()->CallVoidMethod(*j_media_observer_, j_on_audio_track_enabled_, j_track_id);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onAudioTrackDisabled(twilio::media::Media *media,
                                      std::shared_ptr<twilio::media::AudioTrack> track) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jstring j_track_id = webrtc_jni::JavaStringFromStdString(jni(), track->getTrackId());

            jni()->CallVoidMethod(*j_media_observer_, j_on_audio_track_disabled_, j_track_id);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onVideoTrackEnabled(twilio::media::Media *media,
                                     std::shared_ptr<twilio::media::VideoTrack> track) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jstring j_track_id = webrtc_jni::JavaStringFromStdString(jni(), track->getTrackId());

            jni()->CallVoidMethod(*j_media_observer_, j_on_video_track_enabled_, j_track_id);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

    virtual void onVideoTrackDisabled(twilio::media::Media *media,
                                      std::shared_ptr<twilio::media::VideoTrack> track) {
        webrtc_jni::ScopedLocalRefFrame local_ref_frame(jni());
        std::string func_name = std::string(__FUNCTION__);
        TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform, kTSCoreLogLevelDebug, "%s", func_name.c_str());

        {
            rtc::CritScope cs(&deletion_lock_);

            if (!isObserverValid(func_name)) {
                return;
            }

            jstring j_track_id = webrtc_jni::JavaStringFromStdString(jni(), track->getTrackId());

            jni()->CallVoidMethod(*j_media_observer_, j_on_video_track_disabled_, j_track_id);
            CHECK_EXCEPTION(jni()) << "error during CallVoidMethod";
        }
    }

private:
    JNIEnv *jni() {
        return webrtc_jni::AttachCurrentThreadIfNeeded();
    }

    bool isObserverValid(const std::string &callbackName) {
        if (observer_deleted_) {
            TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                               kTSCoreLogLevelWarning,
                               "media observer is marked for deletion, skipping %s callback",
                               callbackName.c_str());
            return false;
        };
        if (webrtc_jni::IsNull(jni(), *j_media_observer_)) {
            TS_CORE_LOG_MODULE(kTSCoreLogModulePlatform,
                               kTSCoreLogLevelWarning,
                               "media observer reference has been destroyed, skipping %s callback",
                               callbackName.c_str());
            return false;
        }
        return true;
    }

    bool observer_deleted_ = false;
    mutable rtc::CriticalSection deletion_lock_;

    const webrtc_jni::ScopedGlobalRef<jobject> j_media_observer_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_media_observer_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_audio_track_class_;
    const webrtc_jni::ScopedGlobalRef<jclass> j_video_track_class_;
    jmethodID j_on_audio_track_added_;
    jmethodID j_on_audio_track_removed_;
    jmethodID j_on_video_track_added_;
    jmethodID j_on_video_track_removed_;
    jmethodID j_on_audio_track_enabled_;
    jmethodID j_on_audio_track_disabled_;
    jmethodID j_on_video_track_enabled_;
    jmethodID j_on_video_track_disabled_;
    jmethodID j_audio_track_ctor_id_;
    jmethodID j_video_track_ctor_id_;

};

#endif //VIDEO_ANDROID_INCLUDE_ANDROID_MEDIA_OBSERVER_H_
