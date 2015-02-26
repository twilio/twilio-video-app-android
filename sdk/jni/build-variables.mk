
OPENSSL_LIBS := \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libssl.a \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libcrypto.a
	
WEBRTC_LIB_DIR := $(TWSDK_JNI_PATH)/../thirdparty/webrtc-355/lib/$(TARGET_ARCH_ABI)
	
WEBRTC_LIBS := \
	-lwebrtc-full
	#-lCNG \
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
	-lyuv_neon \
	-lcore_neon_offsets \
	-ljingle \
	-ljingle_sound \
	-lopenssl \
	-lrbe_components \
	-lvpx_asm_offsets_vp8 \
	-lvpx_asm_offsets_vpx_scale \
	-lwebrtc_base
	
WEBRTC_LDLIBS := -L$(WEBRTC_LIB_DIR) $(WEBRTC_LIBS)
