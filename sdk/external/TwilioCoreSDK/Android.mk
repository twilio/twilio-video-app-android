PROJECT_ROOT := $(call my-dir)/../../

LOCAL_PATH := $(call my-dir)/TwilioCoreSDK/Sources/Core

include $(CLEAR_VARS)

LOCAL_MODULE := SignalCoreSDK

LOCAL_CFLAGS := \
	-DPOSIX=1
	
LOCAL_CPPFLAGS := \
	 -std=c++11

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
	
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
	pjlib
	


include $(BUILD_STATIC_LIBRARY)
