#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALPARTICIPANT_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALPARTICIPANT_H_

#include <jni.h>
#include "video/local_participant.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

class LocalParticipantContext {
public:
    LocalParticipantContext(std::shared_ptr<twilio::video::LocalParticipant> local_participant)
            : local_participant_(local_participant) {
    }

    virtual ~LocalParticipantContext(){
        local_participant_.reset();
    }

    std::shared_ptr<twilio::video::LocalParticipant> getLocalParticipant() {
        return local_participant_;
    }

private:
    std::shared_ptr<twilio::video::LocalParticipant> local_participant_;
};

std::shared_ptr<twilio::video::LocalParticipant> getLocalParticipant(jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeAddAudioTrack(JNIEnv *,
                                                                                  jobject,
                                                                                  jlong,
                                                                                  jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeAddVideoTrack(JNIEnv *,
                                                                                  jobject,
                                                                                  jlong,
                                                                                  jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeRemoveAudioTrack(JNIEnv *,
                                                                                     jobject,
                                                                                     jlong,
                                                                                     jlong);

JNIEXPORT bool JNICALL Java_com_twilio_video_LocalParticipant_nativeRemoveVideoTrack(JNIEnv *,
                                                                                     jobject,
                                                                                     jlong,
                                                                                     jlong);
JNIEXPORT void JNICALL Java_com_twilio_video_LocalParticipant_nativeRelease(JNIEnv *,
                                                                            jobject,
                                                                            jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALPARTICIPANT_H_
