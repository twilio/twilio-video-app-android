TWSDK_JNI_PATH := $(call my-dir)

ifneq ($(ENABLE_PROFILING),)
include $(TWSDK_JNI_PATH)/../thirdparty/android-ndk-profiler/jni/Android.mk
endif

#Include build variables first
include $(TWSDK_JNI_PATH)/build-variables.mk

include $(TWSDK_JNI_PATH)/../thirdparty/yb-pjproject/Android.mk
include $(TWSDK_JNI_PATH)/../thirdparty/poco/Android.mk
include $(TWSDK_JNI_PATH)/../external/twilio-jni/Android.mk
include $(TWSDK_JNI_PATH)/../external/TwilioCoreSDK/Android.mk

LOCAL_PATH := $(TWSDK_JNI_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE := twilio-native
LOCAL_SRC_FILES := \
	hello.cpp

#ifeq ($(APP_DEBUGGABLE),true)
ifeq ($(shell test "$(APP_DEBUGGABLE)" = "true" -o "$(NDK_DEBUG)" = "1" && echo true || echo false),true)
debug_cflags := \
	-DENABLE_PJSIP_LOGGING \
	-DENABLE_JNI_DEBUG_LOGGING
endif

LOCAL_CFLAGS := \
	-Wall \
	-DPJ_IS_BIG_ENDIAN=0 \
	-DPJ_IS_LITTLE_ENDIAN=1 \
	-DPJSIP_SIGNALLING_ONLY=1 \
	-DPOSIX \
	-fvisibility=hidden \
	-DTW_EXPORT='__attribute__((visibility("default")))' \
	$(debug_cflags)
LOCAL_CPPFLAGS := -std=c++11 -fno-rtti

pj_includes := $(addsuffix /include,$(addprefix $(LOCAL_PATH)/../yb-thirdparty/pjproject/,pjlib pjlib-util pjmedia pjnath pjsip))
#webrtc_includes := $(LOCAL_PATH)/../yb-thirdparty/webrtc-355/include
webrtc_includes := $(LOCAL_PATH)/../thirdparty/webrtc/build-android/prebuild/include/
twilio_signal_includes := $(TWSDK_JNI_PATH)/../external/TwilioCoreSDK/TwilioCoreSDK/Sources/Core

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/../external/twilio-jni \
	$(pj_includes) \
	$(twilio_signal_includes) \
	$(webrtc_includes)
	


LOCAL_LDLIBS := \
	-llog \
	-lz \
	-ldl \
	-lGLESv2 \
	-ljnigraphics \
	-lOpenSLES \
	$(WEBRTC_LDLIBS)# \
	$(OPENSSL_LIBS)
	

# pjmedia is in here twice because there's a circular dependency
# between pjmedia and pjmedia-codec (the g711_init func)
#LOCAL_STATIC_LIBRARIES := \
	pjsua-lib \
	pjmedia \
	pjmedia-audiodev \
	pjmedia-videodev \
	pjmedia-codec \
	pjmedia \
	pjnath \
	pjsip \
	pjsip-simple \
	pjsip-ua \
	milenage \
	resample \
	speex \
	srtp \
	pjlib-util \
	pj \
	$(OPENSSL_STATIC_LIBS) \
	twilio-jni
LOCAL_STATIC_LIBRARIES := \
	SignalCoreSDK \
	poco-foundation \
	poco-net \
	poco-util \
	poco-xml \
	pjsua-lib \
	pjmedia \
	pjmedia-audiodev \
	pjmedia-videodev \
	pjmedia-codec \
	pjmedia \
	pjnath \
	pjsip \
	pjsip-simple \
	pjsip-ua \
	milenage \
	resample \
	speex \
	pjlib-util \
	pj \
	twilio-jni

include $(BUILD_SHARED_LIBRARY)
