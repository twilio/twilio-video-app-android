/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALVIDEOTRACK_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALVIDEOTRACK_H_

#include <jni.h>
#include "media/track.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

class LocalVideoTrackContext {
public:
    LocalVideoTrackContext(std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track)
            : local_video_track_(local_video_track) {

    }

    virtual ~LocalVideoTrackContext() {
        local_video_track_.reset();
    }

    std::shared_ptr<twilio::media::LocalVideoTrack> getLocalVideoTrack() {
        return local_video_track_;
    }
private:
    std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track_;
};

std::shared_ptr<twilio::media::LocalVideoTrack> getLocalVideoTrack(jlong);

jobject createJavaLocalVideoTrack(std::shared_ptr<twilio::media::LocalVideoTrack> local_video_track,
                                  jboolean j_enabled,
                                  jobject j_video_capturer,
                                  jobject j_video_constraints,
                                  jobject j_media_factory);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_LocalVideoTrack_nativeIsEnabled(JNIEnv *,
                                                                                 jobject,
                                                                                 jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_LocalVideoTrack_nativeEnable(JNIEnv *,
                                                                          jobject,
                                                                          jlong,
                                                                          jboolean);

JNIEXPORT void JNICALL Java_com_twilio_video_LocalVideoTrack_nativeRelease(JNIEnv *,
                                                                           jobject,
                                                                           jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALVIDEOTRACK_H_
