package com.twilio.signal;

import java.lang.reflect.Field;

import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoCapturerAndroid.CameraErrorHandler;

import android.view.ViewGroup;

import com.twilio.signal.impl.logging.Logger;

public class CameraCapturer implements VideoCapturer {
	
	/** Camera Capture Source */
	public static enum CameraSource {
		CAMERA_SOURCE_FRONT_CAMERA, ///< Front facing device camera
		CAMERA_SOURCE_BACK_CAMERA ///< Back facing device camera
	};

	private static String TAG = "CameraCapturer";

	static final Logger logger = Logger.getLogger(CameraCapturer.class);

	
	private CameraSource source;
	private ViewGroup captureView;
	private VideoCapturerAndroid webrtcCapturer;
	
	public CameraCapturer(CameraSource source, ViewGroup previewContainerView) {
		this.source = source;
		this.captureView = previewContainerView;
		createWebrtcVideoCapturer();
	}
	
	/**
	 * Start previewing the local camera.
	 *
	 */
	public boolean startPreview() {
		return false;
	}
	
	/**
	 * Stop previewing the local camera.
	 *
	 */
	public boolean stopPreview() {
		return false;
	}
	
	/**
	 * Specifies whether or not your camera video is being shared
	 *
	 * @param enabled <code>true</code> if camera should be shared, false otherwise
	 */
	public void enableCamera(boolean enabled) {
		
	}

	/**
	 * Gets whether or not your camera video is being shared
	 *
	 * @return <code>true</code> if camera is being shared, false otherwise
	 */
	public boolean isCameraEnabled() {
		return false;
	}
	
	/**
	 * Specifies whether or not your local video should be paused
	 *
	 * @param paused <code>true</code> if local video should be paused, false otherwise
	 */
	public void pauseVideo(boolean paused) {
		
	}

	/**
	 * Indicates whether your local video is paused.
	 *
	 * @return <code>true</code> if local video is paused, false otherwise
	 */
	public boolean isPaused() {
		return false;
	}
	
	/**
	 * Flips the camera that is being shared.
	 *
	 */
	public boolean switchCamera(Runnable switchDoneEvent) {
		if (webrtcCapturer.switchCamera(switchDoneEvent)) {
			source = (source == CameraSource.CAMERA_SOURCE_FRONT_CAMERA) ?
					CameraSource.CAMERA_SOURCE_BACK_CAMERA :
					CameraSource.CAMERA_SOURCE_FRONT_CAMERA;
			return true;
		}
		return false;
	}
	
	public ViewGroup getCapturerView() {
		return captureView;
	}
	
	public long getNativeVideoCapturer() {
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
