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

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_

#include <jni.h>
#include "media/media_factory.h"

namespace twilio_video_jni {

class MediaFactoryContext {
public:
    MediaFactoryContext(twilio::media::MediaOptions media_options,
                        std::shared_ptr<twilio::media::MediaFactory> media_factory) :
            media_options_(media_options),
            media_factory_(media_factory) {

    }

    virtual ~MediaFactoryContext() {
        media_factory_.reset();
    }

    std::shared_ptr<twilio::media::MediaFactory> getMediaFactory() {
        return media_factory_;
    }
private:
    twilio::media::MediaOptions media_options_;
    std::shared_ptr<twilio::media::MediaFactory> media_factory_;
};

std::shared_ptr<twilio::media::MediaFactory> getMediaFactory(jlong);

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeCreate(JNIEnv *,
                                                                        jobject,
                                                                        jobject,
                                                                        jobject,
                                                                        jobject);

JNIEXPORT jobject JNICALL Java_com_twilio_video_MediaFactory_nativeCreateAudioTrack(JNIEnv *,
                                                                                    jobject,
                                                                                    jlong,
                                                                                    jobject,
                                                                                    jboolean,
                                                                                    jobject,
                                                                                    jstring);
JNIEXPORT jobject JNICALL Java_com_twilio_video_MediaFactory_nativeCreateVideoTrack(JNIEnv *,
                                                                                    jobject,
                                                                                    jlong,
                                                                                    jobject,
                                                                                    jboolean,
                                                                                    jobject,
                                                                                    jobject,
                                                                                    jstring,
                                                                                    jobject);
JNIEXPORT jobject JNICALL Java_com_twilio_video_MediaFactory_nativeCreateDataTrack(JNIEnv *,
                                                                                   jobject,
                                                                                   jlong,
                                                                                   jobject,
                                                                                   jboolean,
                                                                                   jint,
                                                                                   jint,
                                                                                   jstring);
JNIEXPORT void JNICALL Java_com_twilio_video_MediaFactory_nativeRelease(JNIEnv *, jobject, jlong);

/*
 * Only for testing.
 */
JNIEXPORT jlong JNICALL Java_com_twilio_video_MediaFactory_nativeTestCreate(JNIEnv *,
                                                                            jobject,
                                                                            jobject,
                                                                            jobject);
/*
 * Only for testing.
 */
JNIEXPORT void JNICALL Java_com_twilio_video_MediaFactory_nativeTestRelease(JNIEnv *,
                                                                            jobject,
                                                                            jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_MEDIAFACTORY_H_
