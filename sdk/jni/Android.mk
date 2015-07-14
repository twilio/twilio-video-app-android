TWSDK_JNI_PATH := $(call my-dir)

ifneq ($(ENABLE_PROFILING),)
include $(TWSDK_JNI_PATH)/../thirdparty/android-ndk-profiler/jni/Android.mk
endif


include $(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/Android.mk
include $(TWSDK_JNI_PATH)/../external/signal-sdk-core/SDKs/PJSIP/Android.mk
include $(TWSDK_JNI_PATH)/../external/signal-sdk-core/SDKs/POCO/Android.mk
include $(TWSDK_JNI_PATH)/../external/signal-sdk-core/SDKs/WebRTC/build-android/Android.mk
include $(TWSDK_JNI_PATH)/../external/signal-sdk-core/TwilioCoreSDK/Android.mk
include $(TWSDK_JNI_PATH)/../external/twilio-jni/Android.mk


LOCAL_PATH := $(TWSDK_JNI_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE := twilio-native
LOCAL_SRC_FILES := \
	com_twilio_signal_impl_TwilioRTCImpl.cpp\
	com_twilio_signal_impl_EndpointImpl.cpp \
	com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal.cpp \
	com_twilio_signal_impl_ConversationImpl.cpp \
	com_twilio_signal_impl_ConversationImpl_SessionObserverInternal.cpp

#ifeq ($(APP_DEBUGGABLE),true)
ifeq ($(shell test "$(APP_DEBUGGABLE)" = "true" -o "$(NDK_DEBUG)" = "1" && echo true || echo false),true)
debug_cflags := \
	-DENABLE_PJSIP_LOGGING \
	-DENABLE_JNI_DEBUG_LOGGING
endif

#This exists due to bug in ndk in resolving circular dependencies. Techincally we should just name module
#in LOCAL_STATIC_LIBRARIES, however ndk doesn't respect order of the module named.
OPENSSL_LIBS := \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libssl.a \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libcrypto.a

OPUS_LIB := \
	$(TWSDK_JNI_PATH)/../external/signal-sdk-core/SDKs/WebRTC/build-android/prebuild/libs/$(TARGET_ARCH_ABI)/libopus.a

LOCAL_CFLAGS := \
	-Wall \
	-DPOSIX \
	-fvisibility=hidden \
	-DTW_EXPORT='__attribute__((visibility("default")))' \
	$(debug_cflags)
	
LOCAL_CPPFLAGS := -std=gnu++11 -fno-rtti

#Putting openssl libs and opus libs like this will give the warning, but as of today
#I can't find better way to resolve circular dependency (it is broken in ndk). 
LOCAL_LDLIBS := \
	$(OPENSSL_LIBS) \
	$(OPUS_LIB) \
	-llog \
	-lz \
    -lm \
	-ldl \
	-lGLESv2 \
	-ljnigraphics \
	-lOpenSLES 

# pjmedia is in here twice because there's a circular dependency
# between pjmedia and pjmedia-codec (the g711_init func)
LOCAL_STATIC_LIBRARIES := \
	SignalCoreSDK \
	webrtc \
	twilio-jni
	
	
	

include $(BUILD_SHARED_LIBRARY)
