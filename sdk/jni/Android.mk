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
	dummy.cpp \
	com_twilio_signal_impl_TwilioRTCImpl.cpp \
	com_twilio_signal_impl_VideoSurface.cpp \
	com_twilio_signal_impl_EndpointImpl.cpp \
	com_twilio_signal_impl_EndpointImpl_EndpointObserverInternal.cpp \
	com_twilio_signal_impl_ConversationImpl.cpp \
	com_twilio_signal_impl_ConversationImpl_SessionObserverInternal.cpp \

LOCAL_C_INCLUDES := /usr/local/twilio-sdk/webrtc/android/armeabiv7a/include/third_party/icu/source/common

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
	twilio-sdk-core \
	twilio-jni \

# Make JNI_OnLoad a local symbol in libwebrtc-jni.a since it is already defined by libtwilio-jni.a
# dummy.cpp is a fake depedency that causes this command to run prior to linking
LOCALIZE_SYMBOL := $(LOCAL_PATH)/dummy.cpp

$(LOCALIZE_SYMBOL):
	@echo "Localizing JNI_OnLoad symbol in libwebrtc.a to prevent a conflict with libtwilio-jni.a"
	$(ANDROID_NDK_HOME)/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64/aarch64-linux-android/bin/objcopy --localize-symbol JNI_OnLoad /usr/local/twilio-sdk/webrtc/android/armeabiv7a/lib/libwebrtc-jni.a
	touch $(LOCALIZE_SYMBOL)

.INTERMEDIATE: $(LOCALIZE_SYMBOL)

# Manually link the libwebrtc-jni static library. Using LOCAL_WHOLE_STATIC_LIBRARIES does not link the library
WEBRTC_JNI_STATIC_LIBRARY := -Wl,--whole-archive /usr/local/twilio-sdk/webrtc/android/armeabiv7a/lib/libwebrtc-jni.a -Wl,--no-whole-archive
LOCAL_LDFLAGS := \
	$(WEBRTC_JNI_STATIC_LIBRARY)

include $(BUILD_SHARED_LIBRARY)
