package com.twilio.signal.impl;

import java.lang.reflect.Field;

import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoCapturerAndroid.CameraErrorHandler;

import android.view.ViewGroup;

import com.twilio.signal.CameraCapturer;
import com.twilio.signal.CameraErrorListener;
import com.twilio.signal.CameraException;
import com.twilio.signal.impl.logging.Logger;

public class CameraCapturerImpl implements CameraCapturer {

	private static String TAG = "CameraCapturerImpl";

	static final Logger logger = Logger.getLogger(CameraCapturerImpl.class);

	
	private CameraSource source;
	private ViewGroup captureView;
	private VideoCapturerAndroid webrtcCapturer;
	private CameraErrorListener listener;
	private long nativeWebrtcVideoCapturer;
	
	private CameraCapturerImpl(CameraSource source,
			ViewGroup previewContainerView, CameraErrorListener listener) {
		this.source = source;
		this.captureView = previewContainerView;
		this.listener = listener;
		
	}
	
	public static CameraCapturerImpl create(
			CameraSource source,
			ViewGroup previewContainerView,
			CameraErrorListener listener) {
		CameraCapturerImpl camera =
				new CameraCapturerImpl(source, previewContainerView, listener);
		boolean success = camera.createWebrtcVideoCapturer();
		if (!success) {
			return null;
		}
		return camera;
	}
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#startPreview()
	 */
	@Override
	public boolean startPreview() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#stopPreview()
	 */
	@Override
	public boolean stopPreview() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#switchCamera(java.lang.Runnable)
	 */
	@Override
	public boolean switchCamera(Runnable switchDoneEvent) {
		if (webrtcCapturer.switchCamera(switchDoneEvent)) {
			source = (source == CameraSource.CAMERA_SOURCE_FRONT_CAMERA) ?
					CameraSource.CAMERA_SOURCE_BACK_CAMERA :
					CameraSource.CAMERA_SOURCE_FRONT_CAMERA;
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#getCapturerView()
	 */
	@Override
	public ViewGroup getPreviewContainerView() {
		return captureView;
	}
	
	long getNativeVideoCapturer()  {
		return nativeWebrtcVideoCapturer;
	}
	
	private boolean obtainNativeVideoCapturer() {
		// Use reflection to obtain the native video capturer handle
		nativeWebrtcVideoCapturer = 0;
		CameraException exception = null;
		try {
			Field field = webrtcCapturer.getClass().getSuperclass().getDeclaredField("nativeVideoCapturer");
			field.setAccessible(true);
			nativeWebrtcVideoCapturer = field.getLong(webrtcCapturer);
		} catch (NoSuchFieldException e) {
			logger.d("Unable to find webrtc video capturer");
			exception = new CameraException("Unable to find webrtc video capturer: "+e.getMessage());
			
		} catch (IllegalAccessException e) {
			logger.e("Unable to access webrtc video capturer");
			exception = new CameraException("Unable to access webrtc video capturer");
		}
		if ((exception != null) && (listener != null)) {
			listener.onError(exception);
			nativeWebrtcVideoCapturer = 0;
			return false;
		}
		return true;
	}
	
	private String getPreferredDeviceName() {
		if(VideoCapturerAndroid.getDeviceCount() == 0) {
			return null;
		}
		// Use the front-facing camera if one is available otherwise use the first available device
		String deviceName =
				(source == CameraSource.CAMERA_SOURCE_FRONT_CAMERA) ?
						VideoCapturerAndroid.getNameOfFrontFacingDevice() :
						VideoCapturerAndroid.getNameOfBackFacingDevice();
		if(deviceName == null) {
			deviceName = VideoCapturerAndroid.getDeviceName(0);
		}
		return deviceName;
	}
	
	private boolean createWebrtcVideoCapturer() {
		String deviceName = getPreferredDeviceName();
		if(deviceName != null) {
			webrtcCapturer = VideoCapturerAndroid.create(deviceName, cameraErrorHandler);
			if (!obtainNativeVideoCapturer()) {
				return false;
			}
		} else {
			if (listener != null) {
				CameraException exception = new CameraException("Camera device not found");
				listener.onError(exception);
				return false;
			}
		}
		return true;
	}
	
	private CameraErrorHandler cameraErrorHandler = new CameraErrorHandler() {
		
		@Override
		public void onCameraError(String errorMsg) {
			CameraCapturerImpl.this.listener.onError(new CameraException(errorMsg));
		}
	};

}
