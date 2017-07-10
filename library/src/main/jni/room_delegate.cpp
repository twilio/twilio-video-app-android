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

#include "room_delegate.h"
#include "android_stats_observer.h"
#include "com_twilio_video_ConnectOptions.h"
#include "com_twilio_video_MediaFactory.h"

namespace twilio_video_jni {

static const std::string kRoomNotifierThreadName = "RoomNotifier";

RoomDelegate::RoomDelegate(JNIEnv *env,
                           jobject j_connect_options,
                           jlong j_media_factory_handle,
                           jobject j_room,
                           jobject j_room_observer,
                           jobject j_stats_observer) :
        j_connect_options_(env, j_connect_options),
        j_room_(env, j_room),
        j_room_observer_(env, j_room_observer),
        j_stats_observer_(env, j_stats_observer),
        notifier_thread_(rtc::Thread::Create()) {
    notifier_thread_->SetName(kRoomNotifierThreadName, nullptr);
    notifier_thread_->Start();
    MediaFactoryContext *media_factory_context =
            reinterpret_cast<MediaFactoryContext*>(j_media_factory_handle);
    media_factory_ = media_factory_context->getMediaFactory();
}

RoomDelegate::~RoomDelegate() {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "~RoomDelegate");
    notifier_thread_->Stop();

    // Validate that room memory has been released
    RTC_CHECK(room_.get() == nullptr) << "Room not released. Invoke release() "
            "from notifier thread before deleting room delegate";
    RTC_CHECK(android_room_observer_.get() == nullptr) << "AndroidRoomObserver not released. "
            "Invoke release() from notifier thread before deleting room delegate";
    RTC_CHECK(stats_observer_.get() == nullptr) << "StatsObserver not released. "
            "Invoke release() from notifier thread before deleting room delegate";
}

void RoomDelegate::connect() {
    notifier_thread_->Post(RTC_FROM_HERE, this, kMessageTypeConnect);
}

bool RoomDelegate::isRecording() {
    return room_->isRecording();
}

void RoomDelegate::onNetworkChange(twilio::video::NetworkChangeEvent network_change_event) {
    notifier_thread_->Post(RTC_FROM_HERE,
                           this,
                           kMessageTypeNetworkChange,
                           new rtc::TypedMessageData<twilio::video::NetworkChangeEvent>(
                                   network_change_event));
}

void RoomDelegate::getStats() {
    notifier_thread_->Post(RTC_FROM_HERE, this, kMessageTypeGetStats);
}

void RoomDelegate::disconnect() {
    notifier_thread_->Post(RTC_FROM_HERE, this, kMessageTypeDisconnect);
}

void RoomDelegate::release() {
    RTC_CHECK(rtc::Thread::Current() == notifier_thread_.get()) << "release not called on "
            "notifier thread";
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "release")
    room_.reset();
    stats_observer_.reset();
    android_room_observer_.reset();
}

void RoomDelegate::OnMessage(rtc::Message *msg) {
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "onMessage");
    switch (msg->message_id) {
        case kMessageTypeConnect: {
            connectOnNotifier();
            break;
        }
        case kMessageTypeGetStats: {
            getStatsOnNotifier();
            break;
        }
        case kMessageTypeNetworkChange: {
            std::unique_ptr<rtc::TypedMessageData<twilio::video::NetworkChangeEvent>> data(
                    static_cast<rtc::TypedMessageData<twilio::video::NetworkChangeEvent> *>(
                            msg->pdata)
            );
            reportNetworkChangeOnNotifier(data->data());
            break;
        }
        case kMessageTypeDisconnect: {
            disconnectOnNotifier();
            break;
        }
        default: {
            FATAL() << "RoomDelegate received unknown message with id " << msg->message_id;
        }
    }
}

void RoomDelegate::connectOnNotifier() {
    RTC_CHECK(rtc::Thread::Current() == notifier_thread_.get()) << "connect not called on notifier "
            "thread";
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "connectOnNotifier");
    JNIEnv *env = webrtc_jni::AttachCurrentThreadIfNeeded();

    // Create the room observer
    android_room_observer_.reset(new AndroidRoomObserver(env, *j_room_, *j_room_observer_));

    // Create the stats observer
    stats_observer_.reset(new AndroidStatsObserver(env, *j_stats_observer_));

    // Get connect options
    jclass j_connect_options_class = webrtc_jni::GetObjectClass(env, *j_connect_options_);
    jmethodID j_create_native_connect_options_builder_id =
            webrtc_jni::GetMethodID(env, j_connect_options_class,
                                    "createNativeConnectOptionsBuilder", "()J");
    jlong j_connect_options_handle =
            env->CallLongMethod(*j_connect_options_,
                                j_create_native_connect_options_builder_id);
    CHECK_EXCEPTION(env) << "Error creating native connect options builder";
    std::unique_ptr<twilio::video::ConnectOptions::Builder> connect_options_builder(
            reinterpret_cast<twilio::video::ConnectOptions::Builder *>(j_connect_options_handle));

    // Specify the notifier thread and media factory
    connect_options_builder->setNotifierThread(notifier_thread_.get());
    connect_options_builder->setMediaFactory(media_factory_);

    // Connect to room
    room_ = twilio::video::connect(connect_options_builder->build(), android_room_observer_);
}

void RoomDelegate::getStatsOnNotifier() {
    RTC_CHECK(rtc::Thread::Current() == notifier_thread_.get()) << "getStats not called on "
            "notifier thread";

    room_->getStats(stats_observer_);
}

void RoomDelegate::reportNetworkChangeOnNotifier(
        twilio::video::NetworkChangeEvent network_change_event) {
    RTC_CHECK(rtc::Thread::Current() == notifier_thread_.get()) << "onNetworkChange not called on "
            "notifier thread";

    room_->onNetworkChange(network_change_event);
}

void RoomDelegate::disconnectOnNotifier() {
    RTC_CHECK(rtc::Thread::Current() == notifier_thread_.get()) << "disconnect not called on "
            "notifier thread";
    TS_CORE_LOG_MODULE(twilio::video::kTSCoreLogModulePlatform,
                       twilio::video::kTSCoreLogLevelDebug,
                       "disconnectOnNotifier")

    room_->disconnect();
}

}
