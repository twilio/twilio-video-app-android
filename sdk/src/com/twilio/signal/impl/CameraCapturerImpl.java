package com.twilio.signal.impl;

import java.lang.reflect.Field;

import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoCapturerAndroid.CameraErrorHandler;

import android.view.ViewGroup;

import com.twilio.signal.CameraCapturer;
import com.twilio.signal.impl.logging.Logger;

public class CameraCapturerImpl implements CameraCapturer {

	private static String TAG = "CameraCapturerImpl";

	static final Logger logger = Logger.getLogger(CameraCapturerImpl.class);

	
	private CameraSource source;
	private ViewGroup captureView;
	private VideoCapturerAndroid webrtcCapturer;
	
	public CameraCapturerImpl(CameraSource source, ViewGroup previewContainerView) {
		this.source = source;
		this.captureView = previewContainerView;
		createWebrtcVideoCapturer();
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
	 * @see com.twilio.signal.CameraCapturer#enableCamera(boolean)
	 */
	@Override
	public void enableCamera(boolean enabled) {
		
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#isCameraEnabled()
	 */
	@Override
	public boolean isCameraEnabled() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#pauseVideo(boolean)
	 */
	@Override
	public void pauseVideo(boolean paused) {
		
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#isPaused()
	 */
	@Override
	public boolean isPaused() {
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
	public ViewGroup getCapturerView() {
		return captureView;
	}
	
	long getNativeVideoCapturer() {
		// TODO: throw exceptions to callee
		// Use reflection to obtain the native video capturer handle
		long nativeVideoCapturer = 0;
		try {
			Field field = webrtcCapturer.getClass().getSuperclass().getDeclaredField("nativeVideoCapturer");
			field.setAccessible(true);
			nativeVideoCapturer = field.getLong(webrtcCapturer);
		} catch (Exception e) {
			logger.e(e.toString());
		}
		return nativeVideoCapturer;
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
	
	private void createWebrtcVideoCapturer() {
		String deviceName = getPreferredDeviceName();
		if(deviceName != null) {
			webrtcCapturer = VideoCapturerAndroid.create(deviceName, cameraErrorHandler);
		}
		
	}
	
	private CameraErrorHandler cameraErrorHandler = new CameraErrorHandler() {
		
		@Override
		public void onCameraError(String arg0) {
			// TODO notify user about camera error, but how? which listener?
			
		}
	};

}
