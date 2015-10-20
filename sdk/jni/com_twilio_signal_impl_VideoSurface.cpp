#include "talk/app/webrtc/java/jni/jni_helpers.h"
#include "talk/media/base/videorenderer.h"

#include "TSCoreSDKTypes.h"
#include "TSCLogger.h"
#include "TSCVideoSurface.h"
#include "TSCVideoSurfaceObserver.h"
#include "TSCMediaTrackInfo.h"
#include "TSCVideoTrackEventData.h"

#include "com_twilio_signal_impl_VideoSurface.h"
#include "com_twilio_signal_impl_VideoSurfaceFactory.h"

using namespace webrtc;
using namespace webrtc_jni;
using namespace twiliosdk;



class VideoSurfaceObserverJava : public TSCVideoSurfaceObserverObject {

	public:
		VideoSurfaceObserverJava(JNIEnv* jni, jobject j_observer)
			: j_observer_global_(
					jni, j_observer),
			j_observer_class_(
					jni, GetObjectClass(jni, j_observer)),
			j_add_track_id_(
					GetMethodID(jni, *j_observer_class_, "onDidAddVideoTrack", "(Lcom/twilio/signal/impl/TrackInfo;)V")),
			j_remove_track_id_(
					GetMethodID(jni, *j_observer_class_, "onDidRemoveVideoTrack", "(Lcom/twilio/signal/impl/TrackInfo;)V")),
			j_video_track_event_id_(
					GetMethodID(jni, *j_observer_class_, "onDidReceiveVideoTrackEvent", "(Lorg/webrtc/VideoRenderer$I420Frame;Lcom/twilio/signal/impl/TrackInfo;)V")),
			j_trackinfo_class_(
					jni, jni->FindClass("com/twilio/signal/impl/TrackInfoImpl")),
			j_trackinfo_ctor_id_(
					GetMethodID(jni, *j_trackinfo_class_, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V")),
			j_frame_class_(
					jni, jni->FindClass("org/webrtc/VideoRenderer$I420Frame")),
			j_frame_ctor_id_(
					GetMethodID(jni, *j_frame_class_, "<init>", "(III[I[Ljava/nio/ByteBuffer;)V")),
			j_byte_buffer_class_(
					jni, jni->FindClass("java/nio/ByteBuffer"))
			{}

	virtual ~VideoSurfaceObserverJava() {

  	}

	void onDidAddVideoTrack(const TSCVideoTrackInfoObjectRef& trackInfo) {
		TS_CORE_LOG_DEBUG("onDidAddVideoTrack");

    		jni()->CallVoidMethod(*j_observer_global_, j_add_track_id_, nullptr);
	}

	void onDidRemoveVideoTrack(const TSCVideoTrackInfoObjectRef& trackInfo) {
		TS_CORE_LOG_DEBUG("onDidRemoveVideoTrack");

    		jni()->CallVoidMethod(*j_observer_global_, j_remove_track_id_, nullptr);
	}

	void onDidReceiveVideoTrackEvent(const TSCVideoTrackInfoObjectRef& trackInfo,
                                         const TSCVideoTrackEventDataObjectRef& data) {
		TS_CORE_LOG_DEBUG("onDidReceiveVideoTrackEvent");

		jobject j_frame = CricketToJavaFrame(data.get()->getFrame());
		jobject j_trackinfo = TrackInfoToJavaTrackInfoImpl(trackInfo);

    		jni()->CallVoidMethod(*j_observer_global_, j_video_track_event_id_, j_frame, j_trackinfo);
	}

	private:

	JNIEnv* jni() {
		    	return AttachCurrentThreadIfNeeded();
	}

	jstring stringToJString(JNIEnv * env, const std::string & nativeString) {
		return env->NewStringUTF(nativeString.c_str());
	}

	// Return a TrackInfoImpl
	jobject TrackInfoToJavaTrackInfoImpl(const TSCVideoTrackInfoObjectRef& trackInfo) {

    		jstring j_participant_address = stringToJString(jni(), trackInfo->getParticipantAddress());
    		jstring j_track_id = stringToJString(jni(), trackInfo->getTrackId());

		return jni()->NewObject(
				*j_trackinfo_class_, j_trackinfo_ctor_id_,
				j_participant_address, j_track_id);
	}

	// Return a VideoRenderer.I420Frame referring to the data in |frame|.
	jobject CricketToJavaFrame(const cricket::VideoFrame* frame) {

		jintArray strides = jni()->NewIntArray(3);
		jint* strides_array = jni()->GetIntArrayElements(strides, NULL);
		strides_array[0] = frame->GetYPitch();
		strides_array[1] = frame->GetUPitch();
		strides_array[2] = frame->GetVPitch();
		jni()->ReleaseIntArrayElements(strides, strides_array, 0);
		jobjectArray planes = jni()->NewObjectArray(3, *j_byte_buffer_class_, NULL);
		jobject y_buffer = jni()->NewDirectByteBuffer(
				const_cast<uint8*>(frame->GetYPlane()),
				frame->GetYPitch() * frame->GetHeight());
		jobject u_buffer = jni()->NewDirectByteBuffer(
				const_cast<uint8*>(frame->GetUPlane()), frame->GetChromaSize());
		jobject v_buffer = jni()->NewDirectByteBuffer(
				const_cast<uint8*>(frame->GetVPlane()), frame->GetChromaSize());
		jni()->SetObjectArrayElement(planes, 0, y_buffer);
		jni()->SetObjectArrayElement(planes, 1, u_buffer);
		jni()->SetObjectArrayElement(planes, 2, v_buffer);
		return jni()->NewObject(
				*j_frame_class_, j_frame_ctor_id_,
				frame->GetWidth(), frame->GetHeight(),
				static_cast<int>(frame->GetVideoRotation()),
				strides, planes);
	}

	const jmethodID j_add_track_id_;
	const jmethodID j_remove_track_id_;
	const jmethodID j_video_track_event_id_;
	const ScopedGlobalRef<jclass> j_trackinfo_class_;
	const jmethodID j_trackinfo_ctor_id_;
	const ScopedGlobalRef<jclass> j_frame_class_;
	const jmethodID j_frame_ctor_id_;
	const ScopedGlobalRef<jclass> j_byte_buffer_class_;
	const ScopedGlobalRef<jobject> j_observer_global_;
	const ScopedGlobalRef<jclass> j_observer_class_;
};


JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_VideoSurfaceFactory_nativeCreateVideoSurfaceObserver
  (JNIEnv *jni, jclass, jobject j_observer)
{
	TS_CORE_LOG_DEBUG("nativeCreateVideoSurfaceObserver");
  	rtc::scoped_ptr<VideoSurfaceObserverJava> vso(
		new VideoSurfaceObserverJava(jni, j_observer)
	);
  	return (jlong)vso.release();
}


JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_VideoSurfaceFactory_nativeCreateVideoSurface
  (JNIEnv *jni, jclass, jlong observer_p)
{
	TS_CORE_LOG_DEBUG("nativeCreateVideoSurface");

 	TSCVideoSurfaceObserverObjectRef observerObjectRef =
                        TSCVideoSurfaceObserverObjectRef(reinterpret_cast<TSCVideoSurfaceObserverObject*>(observer_p));

	TSCVideoSurfaceObjectRef vs = new TSCVideoSurfaceObject(observerObjectRef);

	return (jlong)vs.release();
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_VideoSurface_freeVideoSurface
  (JNIEnv *, jclass, jlong)
{
	TS_CORE_LOG_DEBUG("freeVideoSurface");
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_VideoSurface_freeObserver
  (JNIEnv *, jclass, jlong observer_p)
{
	TS_CORE_LOG_DEBUG("freeObserver");
	delete reinterpret_cast<VideoSurfaceObserverJava*>(observer_p);
}

