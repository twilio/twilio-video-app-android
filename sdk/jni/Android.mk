TWSDK_JNI_PATH := $(call my-dir)

ifneq ($(ENABLE_PROFILING),)
include $(TWSDK_JNI_PATH)/../thirdparty/android-ndk-profiler/jni/Android.mk
endif

include $(TWSDK_JNI_PATH)/signal-core.mk

HPTEMP = $(shell uname -s)
HOST_PLATFORM = $(shell echo $(HPTEMP) | tr A-Z a-z)

ifeq ($(HOST_PLATFORM),linux)
   TOOLCHAIN_PLAT = linux-x86_64
else
   ifeq ($(HOST_PLATFORM),darwin)
     TOOLCHAIN_PLAT = darwin-x86_64
   else
     $(error Host platform not supported)
   endif
endif 

LOCAL_PATH := $(TWSDK_JNI_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE := twilio-native
LOCAL_SRC_FILES := \
	com_twilio_signal_impl_TwilioRTCImpl.cpp \
	com_twilio_signal_impl_EndpointImpl.cpp \
	com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal.cpp \
	com_twilio_signal_impl_ConversationImpl.cpp \
	com_twilio_signal_impl_ConversationImpl_SessionObserverInternal.cpp

LOCAL_C_INCLUDES := $(PREFIX)/webrtc/android/armeabiv7a/include/third_party/icu/source/common

ifeq ($(shell test "$(APP_DEBUGGABLE)" = "true" -o "$(NDK_DEBUG)" = "1" && echo true || echo false),true)
debug_cflags := \
	-DENABLE_PJSIP_LOGGING \
	-DENABLE_JNI_DEBUG_LOGGING
endif


LOCAL_CFLAGS += \
	-Wall \
	-DPOSIX \
	-fvisibility=hidden \
	-DTW_EXPORT='__attribute__((visibility("default")))' \
	$(debug_cflags)
	
LOCAL_CPPFLAGS := -std=gnu++11 -fno-rtti

LOCAL_LDLIBS := \
	-llog \
	-lz \
	-lm \
	-ldl \
	-lGLESv2 \
	-ljnigraphics \
	-lOpenSLES \
	-lEGL \
	-lGLESv1_CM \
	-landroid \

LOCAL_STATIC_LIBRARIES := \
	twilio-sdk-core


# Manually link the libwebrtc-jni static library. Using LOCAL_WHOLE_STATIC_LIBRARIES does not link the library
WEBRTC_JNI_STATIC_LIBRARY := -Wl,--whole-archive $(PREFIX)/webrtc/android/armeabiv7a/lib/libwebrtc-jni.a -Wl,--no-whole-archive
LOCAL_LDFLAGS := \
	$(WEBRTC_JNI_STATIC_LIBRARY)

include $(BUILD_SHARED_LIBRARY)
