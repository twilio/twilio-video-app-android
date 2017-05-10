#ifndef VIDEO_ANDROID_ROOM_DELEGATE_H_
#define VIDEO_ANDROID_ROOM_DELEGATE_H_

#include <jni.h>

#include "video/video.h"
#include "video/stats_observer.h"
#include "video/room.h"

#include "webrtc/base/messagehandler.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"

#include "android_room_observer.h"

namespace twilio_video_jni {

class RoomDelegate : public rtc::MessageHandler {
public:
    RoomDelegate(JNIEnv *env,
                 jobject j_connect_options,
                 jlong j_media_factory_handle,
                 jobject j_room,
                 jobject j_room_observer,
                 jobject j_stats_observer);
    ~RoomDelegate();

    void connect();
    bool isRecording();
    void onNetworkChange(twilio::video::NetworkChangeEvent network_change_event);
    void getStats();
    void disconnect();
    void release();

private:
    void OnMessage(rtc::Message *msg);
    void connectOnNotifier();
    void getStatsOnNotifier();
    void reportNetworkChangeOnNotifier(twilio::video::NetworkChangeEvent network_change_event);
    void disconnectOnNotifier();

    static const uint32_t kMessageTypeConnect = 0;
    static const uint32_t kMessageTypeGetStats = 1;
    static const uint32_t kMessageTypeNetworkChange = 2;
    static const uint32_t kMessageTypeDisconnect = 3;

    const webrtc_jni::ScopedGlobalRef<jobject> j_connect_options_;
    std::shared_ptr<twilio::media::MediaFactory> media_factory_;
    const webrtc_jni::ScopedGlobalRef<jobject> j_room_;
    const webrtc_jni::ScopedGlobalRef<jobject> j_room_observer_;
    const webrtc_jni::ScopedGlobalRef<jobject> j_stats_observer_;
    std::unique_ptr<rtc::Thread> notifier_thread_;
    std::unique_ptr<twilio::video::Room> room_;
    std::shared_ptr<AndroidRoomObserver> android_room_observer_;
    std::shared_ptr<twilio::video::StatsObserver> stats_observer_;
};

}

#endif // VIDEO_ANDROID_ROOM_DELEGATE_H_
