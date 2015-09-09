TWSDK_JNI_PATH := $(call my-dir)

ifneq ($(ENABLE_PROFILING),)
include $(TWSDK_JNI_PATH)/../thirdparty/android-ndk-profiler/jni/Android.mk
endif

include $(TWSDK_JNI_PATH)/signal-core.mk
include $(TWSDK_JNI_PATH)/../external/twilio-jni/Android.mk


LOCAL_PATH := $(TWSDK_JNI_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE := twilio-native
LOCAL_SRC_FILES := \
	com_twilio_signal_impl_TwilioRTCImpl.cpp \
	com_twilio_signal_impl_VideoSurface.cpp \
	com_twilio_signal_impl_EndpointImpl.cpp \
	com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal.cpp \
	com_twilio_signal_impl_ConversationImpl.cpp \
	com_twilio_signal_impl_ConversationImpl_SessionObserverInternal.cpp \
	peerconnection_jni/androidmediadecoder_jni.cc \
	peerconnection_jni/androidmediaencoder_jni.cc \
	peerconnection_jni/androidvideocapturer_jni.cc \
	peerconnection_jni/classreferenceholder.cc \
 	peerconnection_jni/jni_helpers.cc \
	peerconnection_jni/peerconnection_jni.cc

LOCAL_C_INCLUDES := /usr/local/twilio-sdk/webrtc/android/armeabiv7a/include/third_party/icu/source/common

ifeq ($(shell test "$(APP_DEBUGGABLE)" = "true" -o "$(NDK_DEBUG)" = "1" && echo true || echo false),true)
debug_cflags := \
	-DENABLE_PJSIP_LOGGING \
	-DENABLE_JNI_DEBUG_LOGGING
endif

LIBYUV := -l/usr/local/twilio-sdk/webrtc/android/armeabiv7a/Debug/libyuv.a

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
	-landroid

LOCAL_STATIC_LIBRARIES := \
	twilio-jni \
	twilio-sdk-core

include $(BUILD_SHARED_LIBRARY)
