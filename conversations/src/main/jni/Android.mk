LOCAL_PATH:= $(call my-dir)
include $(call my-dir)/conversations-core.mk

# WEBRTC JNI
include $(CLEAR_VARS)
LOCAL_MODULE := webrtc-jni
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-webrtc/lib/release/$(TARGET_ARCH_ABI)/libwebrtc-jni.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../build/prebuilt/twilio-webrtc/include
include $(PREBUILT_STATIC_LIBRARY)

# We rename this so that webrtc source loading native code does not crash
include $(CLEAR_VARS)
LOCAL_MODULE := jingle_peerconnection_so
LOCAL_SRC_FILES := \
	com_twilio_conversations_impl_TwilioConversationsImpl.cpp \
	com_twilio_conversations_impl_ConversationsClientImpl.cpp \
	com_twilio_conversations_impl_ConversationsClientImpl_EndpointObserverInternal.cpp \
	com_twilio_conversations_impl_ConversationImpl.cpp \
	com_twilio_conversations_impl_ConversationImpl_SessionObserverInternal.cpp \
	android_platform_info_provider.cpp

LOCAL_CPPFLAGS := -std=gnu++11 -fexceptions -Wunused-function

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
	twilio-conversations \
	twilio-common \
	PocoNetSSL \
	PocoCrypto \
	PocoNet \
	PocoUtil \
	PocoXML \
	PocoJSON \
	PocoFoundation \
	boringssl \

# Several webrtc classes use native webrtc-jni objects that are only
# called from Java classes. The compiler will incorrectly strip some
# of these native webrtc-jni objects during linking because it believes
# they are unused by the native library. Using whole static libraries
# ensures that all native webrtc-jni objects are included in the library.
LOCAL_WHOLE_STATIC_LIBRARIES := \
	webrtc-jni \

include $(BUILD_SHARED_LIBRARY)
