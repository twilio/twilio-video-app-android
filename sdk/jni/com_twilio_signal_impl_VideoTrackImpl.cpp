#include <twilio-jni/twilio-jni.h>
#include "webrtc/modules/utility/interface/helpers_android.h"

#include "com_twilio_signal_impl_VideoTrackImpl.h"
#include "TSCoreSDKTypes.h"

#include "talk/app/webrtc/mediastreaminterface.h"

using namespace webrtc;
using namespace twiliosdk;
// Return a Java Webrtc Video Renderer 
JNIEXPORT jobject JNICALL Java_com_twilio_signal_impl_VideoTrackImpl_nativeWrapVideoRenderer
  (JNIEnv *jni, jobject j_twilio_video_renderer_object) {
/**
	j_trackinfo_class_(jni, FindClass(jni, "twilio/signal/impl/TrackInfoImpl")),
**/
	return nullptr; // jni->NewObject( *j_trackinfo_class_, j_trackinfo_ctor_id_, j_twilio_video_renderer_object);
}
/**
class TwilioToWebRtcVideoRendererAdapter: public VideoRendererInterface {

	public:
		TwilioToWebRtcVideoRendererAdapter(JNIEnv* jni, jobject j_twilio_video_renderer_object)
			: j_set_size_id_(tw_jni_get_method(jni, j_twilio_video_renderer_object, "setSize", "(II)V")),
			  j_render_frame_id_(tw_jni_get_method(jni, j_twilio_video_renderer_object, "renderFrame", "(Lcom/twilio/signal/I420Frame;)V")),
			  j_twilio_video_renderer_global_(jni, j_twilio_video_renderer_object) {

		}


	virtual ~TwilioToWebRtcVideoRendererAdapter() { }

	void SetSize(int width, int height) {
		TS_CORE_LOG_DEBUG("Adapter.SetSize");

	}

	void RenderFrame(const cricket::VideoFrame *frame) {
		TS_CORE_LOG_DEBUG("Adapter.RenderFrame");

	}

	private:

	const jmethodID j_set_size_id_;
	const jmethodID j_render_frame_id_;
	const ScopedGlobalRef<jobject> j_twilio_video_renderer_global_;

}
**/
