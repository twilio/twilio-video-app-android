LOCAL_PATH:= $(call my-dir)

ifeq ($(NDK_DEBUG),1)
    BUILD_TYPE := debug
else
    BUILD_TYPE := release
endif

# Twilio Conversations
include $(CLEAR_VARS)
LOCAL_MODULE            := twilio-conversations
LOCAL_SRC_FILES         := $(LOCAL_PATH)/../../../build/prebuilt/twilio-conversations/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libtwilio-conversations.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-conversations/include \
    $(LOCAL_PATH)/../../../build/prebuilt/twilio-webrtc/include 
LOCAL_EXPORT_CFLAGS := -Wall -DARM -DPJ_IS_LITTLE_ENDIAN=1 -DTWILIO_SDK_2_0 -DPJ_IS_BIG_ENDIAN=0 -DWEBRTC_POSIX -DWEBRTC_ANDROID -DWEBRTC_ANDROID_OPENSLES -DPOCO_ANDROID
include $(PREBUILT_STATIC_LIBRARY)

# Twilio Common 
include $(CLEAR_VARS)
LOCAL_MODULE            := twilio-common
LOCAL_SRC_FILES         := $(LOCAL_PATH)/../../../build/prebuilt/twilio-common/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libtwilio-common.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-common/include
LOCAL_EXPORT_CFLAGS := -Wall -DARM -DPJ_IS_LITTLE_ENDIAN=1 -DTWILIO_SDK_2_0 -DPJ_IS_BIG_ENDIAN=0 -DWEBRTC_POSIX -DWEBRTC_ANDROID -DWEBRTC_ANDROID_OPENSLES -DPOCO_ANDROID
include $(PREBUILT_STATIC_LIBRARY)

# POCO Foundation
include $(CLEAR_VARS)
LOCAL_MODULE := PocoFoundation
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libPocoFoundation.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# POCO Net
include $(CLEAR_VARS)
LOCAL_MODULE := PocoNet
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libPocoNet.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

#POCO Crypto
include $(CLEAR_VARS)
LOCAL_MODULE := PocoCrypto
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libPocoCrypto.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/include/Crypto
include $(PREBUILT_STATIC_LIBRARY)

# POCO Net SSL
LOCAL_MODULE := PocoNetSSL
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libPocoNetSSL.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# POCO Util
include $(CLEAR_VARS)
LOCAL_MODULE := PocoUtil
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libPocoUtil.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# POCO XML
include $(CLEAR_VARS)
LOCAL_MODULE := PocoXML
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libPocoXML.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# POCO JSON
include $(CLEAR_VARS)
LOCAL_MODULE := PocoJSON
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/lib/$(BUILD_TYPE)/$(TARGET_ARCH_ABI)/libPocoJSON.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# BORING SSL
include $(CLEAR_VARS)
LOCAL_MODULE := boringssl
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-boringssl/lib/release/$(TARGET_ARCH_ABI)/libboringssl.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-boringssl/include
include $(PREBUILT_STATIC_LIBRARY)
