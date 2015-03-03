
OPENSSL_LIBS := \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libssl.a \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libcrypto.a
	
WEBRTC_LIB_DIR := $(TWSDK_JNI_PATH)/../thirdparty/webrtc/build-android/prebuild/local/release/libs/armv7
	
WEBRTC_LIBS := \
	-lwebrtc-full
	#-lCNG \
	-lG711 \
	-lG722 \
	-lPCM16B \
	-l_core_neon_offsets \
	-laudio_coding_module \
	-laudio_conference_mixer \
	-laudio_device \
	-laudio_processing \
	-laudio_processing_neon \
	-laudioproc_debug_proto \
	-lbitrate_controller \
	-lcommon_audio \
	-lcommon_audio_neon \
	-lcommon_video \
	-lcpu_features \
	-lcpu_features_android \
	-lexpat \
	-lfield_trial_default \
	-lgtest \
	-liLBC \
	-liSAC \
	-liSACFix \
	-licudata \
	-licuuc \
	-lisac_neon \
	-ljingle \
	-ljingle_media \
	-ljingle_p2p \
	-ljingle_peerconnection \
	-ljingle_sound \
	-ljpeg_turbo \
	-ljsoncpp \
	-lmedia_file \
	-lneteq \
	-lopenssl \
	-lopus \
	-lpaced_sender \
	-lprotobuf_full_do_not_use \
	-lprotobuf_lite \
	-lrbe_components \
	-lremote_bitrate_estimator \
	-lrtp_rtcp \
	-lsrtp \
	-lsystem_wrappers \
	-lusrsctplib \
	-lvideo_capture_module \
	-lvideo_coding_utility \
	-lvideo_engine_core \
	-lvideo_processing \
	-lvideo_render_module \
	-lvoice_engine \
	-lvpx \
	-lvpx_asm_offsets_vp8 \
	-lvpx_asm_offsets_vpx_scale \
	-lvpx_intrinsics_neon \
	-lwebrtc \
	-lwebrtc_base \
	-lwebrtc_common \
	-lwebrtc_i420 \
	-lwebrtc_opus \
	-lwebrtc_utility \
	-lwebrtc_video_coding \
	-lwebrtc_vp8 \
	-lyuv \
	-lyuv_neon
	#-lwebrtc-full
		
	
WEBRTC_LDLIBS := -L$(WEBRTC_LIB_DIR) $(WEBRTC_LIBS)
