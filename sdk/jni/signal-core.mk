ifneq ($(TARGET_SIMULATOR),true)

LOCAL_PATH:= $(call my-dir)
TWILIO_COMMON_ROOT:= $(LOCAL_PATH)/../target/android

# SDK Core
include $(CLEAR_VARS)

ifeq ($(PREFIX),)
PREFIX:= /usr/local/twilio-sdk
endif

LOCAL_MODULE            := twilio-sdk-core
LOCAL_SRC_FILES         := $(PREFIX)/core/android/armeabiv7a/lib/libtwilio-sdk-core-full.a
LOCAL_EXPORT_C_INCLUDES := $(PREFIX)/core/android/armeabiv7a/include \
                           $(PREFIX)/webrtc/android/armeabiv7a/include \
                           $(PREFIX)/pjsip/android/armeabiv7a/include


LOCAL_EXPORT_CFLAGS := -Wall -DARM -DPJ_IS_LITTLE_ENDIAN=1 -DTWILIO_SDK_2_0 -DPJ_IS_BIG_ENDIAN=0 -DWEBRTC_POSIX -DWEBRTC_ANDROID -DWEBRTC_ANDROID_OPENSLES -DPOCO_ANDROID
include $(PREBUILT_STATIC_LIBRARY)

# AccessManager
include $(CLEAR_VARS)
TWILIO_COMMON_DIR:= $(TWILIO_COMMON_ROOT)/twilio-access-manager
# LOCAL_SRC_FILES adds prefix jni/, that's why we need additional ../
LOCAL_MODULE            := access-manager
LOCAL_SRC_FILES         := ../$(TWILIO_COMMON_DIR)/lib/$(APP_OPTIM)/armv7/libtwilio-access-manager.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_DIR)/include
LOCAL_EXPORT_CFLAGS := -Wall -DARM -DPJ_IS_LITTLE_ENDIAN=1 -DTWILIO_SDK_2_0 -DPJ_IS_BIG_ENDIAN=0 -DWEBRTC_POSIX -DWEBRTC_ANDROID -DWEBRTC_ANDROID_OPENSLES -DPOCO_ANDROID
include $(PREBUILT_STATIC_LIBRARY)


# POCO Foundation
include $(CLEAR_VARS)
LOCAL_MODULE := PocoFoundation 
LOCAL_SRC_FILES := ../$(TWILIO_COMMON_ROOT)/twilio-poco/lib/release/armv7/libPocoFoundation.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_ROOT)/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# POCO Net
include $(CLEAR_VARS)
LOCAL_MODULE := PocoNet
LOCAL_SRC_FILES := ../$(TWILIO_COMMON_ROOT)/twilio-poco/lib/release/armv7/libPocoNet.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_ROOT)/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

#POCO Crypto
include $(CLEAR_VARS)
LOCAL_MODULE := PocoCrypto
LOCAL_SRC_FILES := ../$(TWILIO_COMMON_ROOT)/twilio-poco/lib/release/armv7/libPocoCrypto.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_ROOT)/twilio-poco/include/Crypto
include $(PREBUILT_STATIC_LIBRARY)

# POCO Net SSL
LOCAL_MODULE := PocoNetSSL
LOCAL_SRC_FILES := ../$(TWILIO_COMMON_ROOT)/twilio-poco/lib/release/armv7/libPocoNetSSL.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_ROOT)/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# POCO Util
include $(CLEAR_VARS)
LOCAL_MODULE := PocoUtil
LOCAL_SRC_FILES := ../$(TWILIO_COMMON_ROOT)/twilio-poco/lib/release/armv7/libPocoUtil.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_ROOT)/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# POCO XML
include $(CLEAR_VARS)
LOCAL_MODULE := PocoXML
LOCAL_SRC_FILES := ../$(TWILIO_COMMON_ROOT)/twilio-poco/lib/release/armv7/libPocoXML.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_ROOT)/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# POCO JSON
include $(CLEAR_VARS)
LOCAL_MODULE := PocoJSON
LOCAL_SRC_FILES := ../$(TWILIO_COMMON_ROOT)/twilio-poco/lib/release/armv7/libPocoJSON.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_ROOT)/twilio-poco/include
include $(PREBUILT_STATIC_LIBRARY)

# BORING SSL
include $(CLEAR_VARS)
LOCAL_MODULE := boringssl
LOCAL_SRC_FILES := ../$(TWILIO_COMMON_ROOT)/twilio-boringssl/lib/release/armv7/libboringssl.a
LOCAL_EXPORT_C_INCLUDES := $(TWILIO_COMMON_ROOT)/twilio-boringssl/include
include $(PREBUILT_STATIC_LIBRARY)

endif  # TARGET_SIMULATOR != true

