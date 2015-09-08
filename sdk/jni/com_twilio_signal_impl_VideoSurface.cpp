#include <jni.h>

#include "webrtc/modules/utility/interface/helpers_android.h"

#include "TSCoreSDKTypes.h"
#include "TSCLogger.h"

#include "com_twilio_signal_impl_VideoSurface.h"
#include "com_twilio_signal_impl_VideoSurface_Observer.h"
#include "com_twilio_signal_impl_VideoSurfaceFactory.h"

#include "TSCVideoSurfaceObserver.h"
#include "TSCMediaTrackInfo.h"
#include "TSCVideoTrackEventData.h"

using namespace webrtc;
using namespace twiliosdk;

class VideoSurfaceObserverJava : public TSCVideoSurfaceObserver {
	public:
		VideoSurfaceObserverJava(JNIEnv* jni, jobject j_observer)
			: j_observer_global_(jni, j_observer),	
			  j_observer_class_(jni, jni->GetObjectClass(*j_observer_global_)) {

		}

	virtual ~VideoSurfaceObserverJava() {

  	}

	void onDidAddVideoTrack(const TSCVideoTrackInfoObjectRef& trackInfo) {
		TS_CORE_LOG_DEBUG("onDidAddVideoTrack");
	}

	void onDidRemoveVideoTrack(const TSCVideoTrackInfoObjectRef& trackInfo) {
		TS_CORE_LOG_DEBUG("onDidRemoveVideoTrack");
	}
    
	void onDidReceiveVideoTrackEvent(const TSCVideoTrackInfoObjectRef& trackInfo,
                                         const TSCVideoTrackEventDataObjectRef& data) {
		TS_CORE_LOG_DEBUG("onDidReceiveVideoTrackEvent");
	}
    

	private:

	const ScopedGlobalRef<jobject> j_observer_global_;
	const ScopedGlobalRef<jclass> j_observer_class_;
};


JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_VideoSurfaceFactory_nativeCreateVideoSurfaceObserver
  (JNIEnv *jni, jclass, jobject j_observer)
{
	TS_CORE_LOG_DEBUG("nativeCreateVideoSurfaceObserver");

	return (jlong)new VideoSurfaceObserverJava(jni, j_observer);
}


JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_VideoSurfaceFactory_nativeCreateVideoSurface
  (JNIEnv *jni, jclass, jlong nativeObserver)
{
	TS_CORE_LOG_DEBUG("nativeCreateVideoSurface");
	return 0;
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_VideoSurface_freeVideoSurface
  (JNIEnv *, jclass, jlong)
{
	TS_CORE_LOG_DEBUG("freeVideoSurface");
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_VideoSurface_freeObserver
  (JNIEnv *, jclass, jlong)
{
	TS_CORE_LOG_DEBUG("freeObserver");
}

