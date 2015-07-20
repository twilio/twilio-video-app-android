ifneq ($(TARGET_SIMULATOR),true)

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE            := twilio-sdk-core
LOCAL_SRC_FILES         := /usr/local/twilio-sdk/core/android/armeabiv7a/lib/libtwilio-sdk-core-full.a
LOCAL_EXPORT_C_INCLUDES := /usr/local/twilio-sdk/core/android/armeabiv7a/include \
                           /usr/local/twilio-sdk/webrtc/android/armeabiv7a/include \
                           /usr/local/twilio-sdk/pjsip/android/armeabiv7a/include
LOCAL_EXPORT_CFLAGS := -Wall -DARM -DPJ_IS_LITTLE_ENDIAN=1 -DPJ_IS_BIG_ENDIAN=0 -DWEBRTC_POSIX -DWEBRTC_ANDROID -DWEBRTC_ANDROID_OPENSLES -DPOCO_ANDROID
include $(PREBUILT_STATIC_LIBRARY)

endif  # TARGET_SIMULATOR != true