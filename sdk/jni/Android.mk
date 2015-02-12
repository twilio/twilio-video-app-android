TWSDK_JNI_PATH := $(call my-dir)

ifneq ($(ENABLE_PROFILING),)
include $(TWSDK_JNI_PATH)/../thirdparty/android-ndk-profiler/jni/Android.mk
endif

#ifdef USE_ANDROID_OPENSSL
#include $(TWSDK_JNI_PATH)/../thirdparty/openssl/Android.mk
#OPENSSL_STATIC_LIBS :=
#	libssl_static \
#	libcrypto_static
#else
#OPENSSL_LIBS := \
#	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libssl.a \
#	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libcrypto.a
#endif

#include $(TWSDK_JNI_PATH)/../thirdparty/pjproject/Android.mk
$(info $(TWSDK_JNI_PATH)/../thirdparty/poco/Android.mk)
include $(TWSDK_JNI_PATH)/../thirdparty/poco/Android.mk
#include $(TWSDK_JNI_PATH)/../external/twilio-jni/Android.mk

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
	-fvisibility=hidden \
	-DTW_EXPORT='__attribute__((visibility("default")))' \
	$(debug_cflags)

#pj_includes := $(addsuffix /include,$(addprefix $(LOCAL_PATH)/../thirdparty/pjproject/,pjlib pjlib-util pjmedia pjnath pjsip))

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/../external/twilio-jni
	#$(pj_includes) \
	

LOCAL_LDLIBS := \
	-llog \
	-lz \
	-ldl \
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
	poco-foundation

include $(BUILD_SHARED_LIBRARY)
