PROJECT_ROOT := $(call my-dir)/../../

LOCAL_PATH := $(call my-dir)/TwilioCoreSDK/Sources/Core

include $(CLEAR_VARS)

LOCAL_MODULE := SignalCoreSDK

pj_includes := $(addsuffix /include,$(addprefix $(PROJECT_ROOT)/thirdparty/yb-pjproject/,pjlib pjlib-util pjmedia pjnath pjsip))

#poco_includes := $(addsuffix /include,$(addprefix $(PROJECT_ROOT)/thirdparty/poco/POCO/,Foundation Net Util XML))

pjproject_cppflags = \
	-DPJ_LINUX=1 \
	-DPJ_ANDROID=1 \
	-DPJ_HAS_ANDROID_API_LEVEL_H=1 \
	-DPJ_HAS_ARPA_INET_H=1 \
	-DPJ_HAS_ASSERT_H=1 \
	-DPJ_HAS_CTYPE_H=1 \
	-DPJ_HAS_ERRNO_H=1 \
	-DPJ_HAS_FCNTL_H=1 \
	-DPJ_HAS_LIMITS_H=1 \
	-DPJ_HAS_MALLOC_H=1 \
	-DPJ_HAS_NETDB_H=1 \
	-DPJ_HAS_NETINET_IN_SYSTM_H=1 \
	-DPJ_HAS_LINUX_IN_H=0 \
	-DPJ_HAS_NETINET_IN_H=1 \
	-DPJ_HAS_NETINET_TCP_H=1 \
	-DPJ_HAS_NET_IF_H=1 \
	-DPJ_HAS_SEMAPHORE_H=1 \
	-DPJ_HAS_SETJMP_H=1 \
	-DPJ_HAS_STDARG_H=1 \
	-DPJ_HAS_STDDEF_H=1 \
	-DPJ_HAS_STDIO_H=1 \
	-DPJ_HAS_STDINT_H=1 \
	-DPJ_HAS_STDLIB_H=1 \
	-DPJ_HAS_STRING_H=1 \
	-DPJ_HAS_SYS_IOCTL_H=1 \
	-DPJ_HAS_SYS_SELECT_H=1 \
	-DPJ_HAS_SYS_SOCKET_H=1 \
	-DPJ_HAS_SYS_TIME_H=1 \
	-DPJ_HAS_SYS_TIMEB_H=1 \
	-DPJ_HAS_SYS_TYPES_H=1 \
	-DPJ_HAS_SYS_UTSNAME_H=1 \
	-DPJ_HAS_TIME_H=1 \
	-DPJ_HAS_UNISTD_H=1 \
	-DPJ_SOCK_HAS_INET_ATON=1 \
	-DPJ_SOCK_HAS_INET_PTON=1 \
	-DPJ_SOCK_HAS_INET_NTOP=1 \
	-DPJ_SOCK_HAS_GETADDRINFO=1 \
	-DPJ_HAS_SEMAPHORE=1 \
	-DPJ_HAS_PTHREAD_MUTEXATTR_SETTYPE=1 \
	-DPJ_HAS_SOCKLEN_T=1 \
	-DPJ_SELECT_NEEDS_NFDS=0 \
	-DPJ_HAS_ERRNO_VAR=1 \
	-DPJ_HAS_SO_ERROR=1 \
	-DPJ_BLOCKING_ERROR_VAL=EAGAIN \
	-DPJ_BLOCKING_CONNECT_ERROR_VAL=EINPROGRESS \
	-DPJ_HAS_THREADS=1 \
	-DPJ_HAS_HIGH_RES_TIMER=1 \
	-DPJ_HAS_MALLOC=1 \
	-DPJ_OS_HAS_CHECK_STACK=0 \
	-DPJ_NATIVE_STRING_IS_UNICODE=0 \
	-DPJ_POOL_ALIGNMENT=4 \
	-DPJ_ATOMIC_VALUE_TYPE=long \
	-DPJ_EMULATE_RWMUTEX=1 \
	-DPJ_THREAD_SET_STACK_SIZE=0 \
	-DPJ_THREAD_ALLOCATE_STACK=0 \
	-DPJ_HAS_SSL_SOCK=1 \
	-DPJ_IS_BIG_ENDIAN=0 \
	-DPJ_IS_LITTLE_ENDIAN=1 \
	-DPJ_HAS_FLOATING_POINT=0 \
	-DPJMEDIA_HAS_G711_CODEC=1 \
	-DPJMEDIA_HAS_L16_CODEC=0 \
	-DPJMEDIA_HAS_GSM_CODEC=0 \
	-DPJMEDIA_HAS_ILBC_CODEC=0 \
	-DPJMEDIA_HAS_G722_CODEC=0 \
	-DPJMEDIA_HAS_G7221_CODEC=0 \
	-DPJMEDIA_HAS_OPENCORE_AMRNB_CODEC=0 \
	-DPJMEDIA_HAS_SPEEX_CODEC=1 \
	-DPJMEDIA_HAS_OPUS_CODEC=0 \
	-DPJMEDIA_AUDIO_DEV_HAS_WMME=0 \
	-DPJMEDIA_AUDIO_DEV_HAS_PORTAUDIO=0 \
	-DPJMEDIA_AUDIO_DEV_HAS_WMME=0 \
	-DPJMEDIA_AUDIO_DEV_HAS_ANDROID_TWILIO=1 \
	-DPJMEDIA_AUDIO_DEV_HAS_ANDROID_TWILIO_JNI=1 \
	-DPJMEDIA_AUDIO_DEV_HAS_ANDROID_TWILIO_SLES=1 \
	-DPJSIP_HAS_TLS_TRANSPORT=1 \
	-DPJSIP_SIGNALLING_ONLY=1


LOCAL_CFLAGS := \
	-DPOSIX=1 \
	$(pjproject_cppflags)

LOCAL_CPPFLAGS := \
	 -std=c++11

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH) \
	$(poco_includes) \
	$(pj_includes)

LOCAL_SRC_FILES := \
	TSCAudioInputController.cpp \
	TSCAudioInputControllerImpl.cpp \
	TSCAudioInputControllerObserver.cpp \
	TSCCameraCapturer.cpp \
	TSCDeviceManager.cpp \
	TSCEndpoint.cpp \
	TSCEndpointObserver.cpp \
	TSCEvent.cpp \
	TSCIdentityService.cpp \
	TSCIncomingParticipantConnection.cpp \
	TSCIncomingPeerConnection.cpp \
	TSCIncomingPeerConnectionImpl.cpp \
	TSCIncomingSession.cpp \
	TSCIncomingSessionImpl.cpp \
	TSCLogger.cpp \
	TSCMediaDeviceInfo.cpp \
	TSCMediaStreamInfo.cpp \
	TSCMediaStreamUtils.cpp \
	TSCMediaTrackInfo.cpp \
	TSCOutgoingParticipantConnection.cpp \
	TSCOutgoingPeerConnection.cpp \
	TSCOutgoingPeerConnectionImpl.cpp \
	TSCOutgoingSession.cpp \
	TSCOutgoingSessionImpl.cpp \
	TSCPJSUA.cpp \
	TSCParticipant.cpp \
	TSCParticipantConnection.cpp \
	TSCPeerConnection.cpp \
	TSCPeerConnectionImpl.cpp \
	TSCPeerConnectionObserver.cpp \
	TSCSIPAccount.cpp \
	TSCSIPCall.cpp \
	TSCSIPUtils.cpp \
	TSCScreenCaptureProvider.cpp \
	TSCScreenCapturer.cpp \
	TSCSession.cpp \
	TSCSessionDescriptionObservers.cpp \
	TSCSessionImpl.cpp \
	TSCSessionLifeCycleObserver.cpp \
	TSCSessionObserver.cpp \
	TSCThreadManager.cpp \
	TSCThreadMonitor.cpp \
	TSCVideoCaptureController.cpp \
	TSCVideoCaptureControllerImpl.cpp \
	TSCVideoCaptureControllerObserver.cpp \
	TSCVideoCapturer.cpp \
	TSCVideoCodecManager.cpp \
	TSCVideoSurface.cpp \
	TSCVideoSurfaceObserver.cpp \
	TSCVideoTrackEventData.cpp \
	TSCVideoTrackRender.cpp \
	TSCWebRTCLoggerImpl.cpp \
	TSCoreConstants.cpp \
	TSCoreError.cpp \
	TSCoreSDK.cpp
	
LOCAL_STATIC_LIBRARIES := \
	webrtc \
	openssl-crypto \
	openssl \
	poco-foundation \
	poco-net \
	poco-util \
	poco-xml
	


include $(BUILD_STATIC_LIBRARY)
