TWSDK_JNI_PATH := $(call my-dir)

ifneq ($(ENABLE_PROFILING),)
include $(TWSDK_JNI_PATH)/../thirdparty/android-ndk-profiler/jni/Android.mk
endif

OPENSSL_LIBS := \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libssl.a \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libcrypto.a

WEBRTC_LIB_DIR := $(TWSDK_JNI_PATH)/../thirdparty/webrtc-355/lib/$(TARGET_ARCH_ABI)
	
WEBRTC_LIBS := \
	-lCNG \
	-lG711 \
	-lG722 \
	-lPCM16B \
	-laudio_coding_module \
	-laudio_conference_mixer \
	-laudio_decoder_interface \
	-laudio_device \
	-laudio_encoder_interface \
	-laudio_processing \
	-laudio_processing_neon \
	-laudioproc_debug_proto \
	-lbitrate_controller \
	-lboringssl \
	-lcommon_audio \
	-lcommon_audio_neon \
	-lcommon_video \
	-lcpu_features \
	-lcpu_features_android \
	-lexpat \
	-lfield_trial_default \
	-liLBC \
	-liSAC \
	-liSACFix \
	-licudata \
	-licuuc \
	-lisac_neon \
	-ljingle_media \
	-ljingle_p2p \
	-ljingle_peerconnection \
	-ljpeg_turbo \
	-ljsoncpp \
	-lmedia_file \
	-lmetrics_default \
	-lneteq \
	-lopenmax_dl \
	-lopenmax_dl_armv7 \
	-lopenmax_dl_neon \
	-lopus \
	-lpaced_sender \
	-lprotobuf_full_do_not_use \
	-lprotobuf_lite \
	-lred \
	-lremote_bitrate_estimator \
	-lrtc_base \
	-lrtc_base_approved \
	-lrtc_p2p \
	-lrtc_sound \
	-lrtc_xmllite \
	-lrtc_xmpp \
	-lrtp_rtcp \
	-lsrtp \
	-lsystem_wrappers \
	-lusrsctplib \
	-lvideo_capture_module \
	-lvideo_capture_module_internal_impl \
	-lvideo_coding_utility \
	-lvideo_engine_core \
	-lvideo_processing \
	-lvideo_render_module \
	-lvideo_render_module_internal_impl \
	-lvoice_engine \
	-lvpx \
	-lvpx_intrinsics_neon \
	-lwebrtc \
	-lwebrtc_common \
	-lwebrtc_i420 \
	-lwebrtc_opus \
	-lwebrtc_utility \
	-lwebrtc_video_coding \
	-lwebrtc_vp8 \
	-lwebrtc_vp9 \
	-lyuv \
	-lyuv_neon


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
LOCAL_CPPFLAGS := -std=c++11

pj_includes := $(addsuffix /include,$(addprefix $(LOCAL_PATH)/../yb-thirdparty/pjproject/,pjlib pjlib-util pjmedia pjnath pjsip))
webrtc_includes := $(LOCAL_PATH)/../yb-thirdparty/webrtc-355/include
twilio_signal_includes := $(TWSDK_JNI_PATH)/../external/TwilioCoreSDK/TwilioCoreSDK/Sources/Core

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/../external/twilio-jni \
	$(pj_includes) \
	$(twilio_signal_includes) \
	$(LOCAL_PATH)/../thirdparty/webrtc-355/include
	

LOCAL_LDLIBS := \
	-llog \
	-lz \
	-ldl \
	$(OPENSSL_LIBS) \
	-L$(WEBRTC_LIB_DIR) \
	$(WEBRTC_LIBS)
	

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
	srtp \
	pjlib-util \
	pj \
	twilio-jni

include $(BUILD_SHARED_LIBRARY)
