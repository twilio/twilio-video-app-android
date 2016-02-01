package com.twilio.conversations.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoCapturerAndroid.CameraEventsHandler;
import org.webrtc.CameraEnumerationAndroid;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;
import com.twilio.conversations.CapturerException.ExceptionDomain;
import com.twilio.conversations.impl.logging.Logger;


public class CameraCapturerImpl implements CameraCapturer, Application.ActivityLifecycleCallbacks {

	private static String TAG = "CameraCapturerImpl";

	static final Logger logger = Logger.getLogger(CameraCapturerImpl.class);
    private long session;

    private enum CapturerState {
		IDLE,
		PREVIEWING,
		BROADCASTING
	}

	private final Activity activity;
	private CameraSource source;
	private CapturerState lastCapturerState;

	/* Preview capturer members */
    private final ViewGroup previewContainer;
    private Camera camera;
    private int cameraId;
    private CameraPreview cameraPreview;
    private CapturerState capturerState = CapturerState.IDLE;

	/* Conversation capturer members */
	private ViewGroup captureView;
	private VideoCapturerAndroid videoCapturerAndroid;
	private CapturerErrorListener listener;
	private long nativeVideoCapturerAndroid;

	private CameraCapturerImpl(Activity activity, CameraSource source,
			ViewGroup previewContainer, CapturerErrorListener listener) {
		this.activity = activity;
		this.source = source;
		this.previewContainer = previewContainer;
		this.listener = listener;
		cameraId = getCameraId();
		if(cameraId < 0 && listener != null) {
			listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Invalid camera source."));
		}
		activity.getApplication().registerActivityLifecycleCallbacks(this);
	}

	public static CameraCapturerImpl create(
			Activity activity,
			CameraSource source,
			ViewGroup previewContainer,
			CapturerErrorListener listener) {
		CameraCapturerImpl cameraCapturer =
				new CameraCapturerImpl(activity, source, previewContainer, listener);

		return cameraCapturer;
	}

	/*
	 * Use VideoCapturerAndroid to determine the camera id of the specified source.
	 */
	private int getCameraId() {
		String deviceName;
		if(source == CameraSource.CAMERA_SOURCE_BACK_CAMERA) {
			 deviceName = CameraEnumerationAndroid.getNameOfBackFacingDevice();
		} else {
			deviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
		}
		if(deviceName == null) {
			cameraId = -1;
		} else {
			String[] deviceNames = CameraEnumerationAndroid.getDeviceNames();
			for(int i = 0; i < deviceNames.length; i++) {
				if(deviceName.equals(deviceNames[i])) {
					cameraId = i;
					break;
				}
			}
		}
		return cameraId;
	}

    @Override
    public synchronized void startPreview() {
        if(capturerState.equals(CapturerState.PREVIEWING) || capturerState.equals(CapturerState.BROADCASTING)) {
            return;
        }

        if (camera == null) {
            try {
                camera = Camera.open(cameraId);
            } catch (Exception e) {
				if(listener != null) {
                    listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to open camera " + CameraEnumerationAndroid.getDeviceName(cameraId) + ":" + e.getMessage()));
				}
				return;
            }

            if (camera == null && listener != null) {
               	listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to open camera " + CameraEnumerationAndroid.getDeviceName(cameraId)));
				return;
            }
        }

        // Set camera to continually auto-focus
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(params);

        cameraPreview = new CameraPreview(activity, camera, listener);
        previewContainer.removeAllViews();
        previewContainer.addView(cameraPreview);

        capturerState = CapturerState.PREVIEWING;
    }

    @Override
    public synchronized void stopPreview() {
        if(capturerState.equals(CapturerState.PREVIEWING)) {
            previewContainer.removeAllViews();
            cameraPreview = null;
			if(camera != null) {
				camera.release();
				camera = null;
			}
           	capturerState = CapturerState.IDLE;
        }
    }

	@Override
	public synchronized boolean isPreviewing() {
		return capturerState.equals(CapturerState.PREVIEWING);
	}

	/*
	 * Called internally prior to a session being started to setup
	 * the capturer used during a Conversation.
	 */
	synchronized void startConversationCapturer(long session) {
        this.session = session;

		if(isPreviewing()) {
			stopPreview();
		}
		createVideoCapturerAndroid();
		capturerState = CapturerState.BROADCASTING;
	}

	@Override
	public synchronized boolean switchCamera() {
		if(capturerState.equals(CapturerState.PREVIEWING)) {
			stopPreview();
			cameraId = (cameraId + 1) % Camera.getNumberOfCameras();
			startPreview();
			return true;
		} else if (capturerState.equals(CapturerState.BROADCASTING)) {
			// TODO: propagate error
			videoCapturerAndroid.switchCamera(null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void pause() {
		lastCapturerState = capturerState;
		if(capturerState.equals(CapturerState.PREVIEWING)) {
			stopPreview();
		} else if(capturerState.equals(CapturerState.BROADCASTING)) {
		    stopVideoSource(session);
		}
	}

	@Override
	public void resume() {
		if(lastCapturerState != null) {
			if(lastCapturerState.equals(CapturerState.PREVIEWING)) {
				startPreview();
			} else if(lastCapturerState.equals(CapturerState.BROADCASTING)) {
			    restartVideoSource(session);
			}
			lastCapturerState = null;
		}
	}

	long getNativeVideoCapturer()  {
		return nativeVideoCapturerAndroid;
	}

	void resetNativeVideoCapturer() {
		nativeVideoCapturerAndroid = 0;
		capturerState = CapturerState.IDLE;
	}

	private long retrieveNativeVideoCapturerAndroid(VideoCapturerAndroid videoCapturerAndroid) {
		// Use reflection to retrieve the native video capturer handle
		long nativeHandle = 0;
		try {
			Field field = videoCapturerAndroid.getClass().getSuperclass().getDeclaredField("nativeVideoCapturer");
			field.setAccessible(true);
			nativeHandle = field.getLong(videoCapturerAndroid);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Unable to get nativeVideoCapturer field: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to access nativeVideoCapturer field: " + e.getMessage());
		}
		return nativeHandle;
	}
	
	private void createVideoCapturerAndroid() {
		String deviceName = CameraEnumerationAndroid.getDeviceName(cameraId);
		if(deviceName == null && listener != null) {
			listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Camera device not found"));
			return;
		}
		videoCapturerAndroid = VideoCapturerAndroid.create(deviceName, cameraEventsHandler);
		nativeVideoCapturerAndroid = retrieveNativeVideoCapturerAndroid(videoCapturerAndroid);
	}
	
	
	private final CameraEventsHandler cameraEventsHandler = new CameraEventsHandler() {

		@Override
		public void onCameraError(String errorMsg) {
			if(CameraCapturerImpl.this.listener != null) {
				CameraCapturerImpl.this.listener.onError(new CapturerException(ExceptionDomain.CAMERA, errorMsg));
			}
		}

		@Override
		public void onCameraOpening(int cameraId) {

		}

		@Override
		public void onFirstFrameAvailable() {

		}

		@Override
		public void onCameraClosed() {

		} 
	};

	private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		private Context context;
		private SurfaceHolder holder;
		private Camera camera;
		private CapturerErrorListener listener;
		private OrientationEventListener orientationEventListener;

		public CameraPreview(Context context, Camera camera, CapturerErrorListener listener) {
			super(context);
			this.context = context;
			this.camera = camera;
			this.listener = listener;

			holder = getHolder();
			holder.addCallback(this);
			orientationEventListener = new OrientationEventListener(context) {
				@Override
				public void onOrientationChanged(int orientation) {
					updatePreviewOrientation();
				}
			};
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if (camera != null) {
					camera.setPreviewDisplay(holder);
					camera.startPreview();
					orientationEventListener.enable();
				}

			} catch (IOException e) {
				if(listener != null) {
					listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to start preview: " + e.getMessage()));
				}
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if(camera != null) {
				orientationEventListener.disable();
				camera.stopPreview();
				try {
					camera.setPreviewDisplay(null);
				} catch(IOException e) {
					if(listener != null) {
						listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to reset preview: " + e.getMessage()));
					}
				}
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if (this.holder.getSurface() == null) {
				return;
			}

			if(camera != null) {
				try {
					camera.stopPreview();
					camera.setPreviewDisplay(this.holder);
					camera.startPreview();
					updatePreviewOrientation();
				} catch (Exception e) {
					if(listener != null) {
						listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to restart preview: " + e.getMessage()));
					}
				}
			}
		}

		private void updatePreviewOrientation() {
			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, info);
			int degrees = getDeviceOrientation();
			int resultOrientation;

			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				resultOrientation = (info.orientation + degrees) % 360;
				resultOrientation = (360 - resultOrientation) % 360;
			} else {
				resultOrientation = (info.orientation - degrees + 360) % 360;
			}
			camera.setDisplayOrientation(resultOrientation);
		}

		private int getDeviceOrientation() {
			int orientation = 0;

			WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			switch(wm.getDefaultDisplay().getRotation()) {
				case Surface.ROTATION_90:
					orientation = 90;
					break;
				case Surface.ROTATION_180:
					orientation = 180;
					break;
				case Surface.ROTATION_270:
					orientation = 270;
					break;
				case Surface.ROTATION_0:
				default:
					orientation = 0;
					break;
			}
			return orientation;
		}

	}

	private void startCapture() {
		/*
		 * Get requestedWidth, requestedHeight, requestedFramerate, frameObserver, applicationContext
		 * using reflection.
		 */
		try {
			Field requestedWidthField = videoCapturerAndroid.getClass().getDeclaredField("requestedWidth");
			requestedWidthField.setAccessible(true);
			int requestedWidth = requestedWidthField.getInt(videoCapturerAndroid);

			Field requestedHeightField = videoCapturerAndroid.getClass().getDeclaredField("requestedHeight");
			requestedHeightField.setAccessible(true);
			int requestedHeight = requestedHeightField.getInt(videoCapturerAndroid);

			Field requestedFramerateField = videoCapturerAndroid.getClass().getDeclaredField("requestedFramerate");
			requestedFramerateField.setAccessible(true);
			int requestedFramerate = requestedFramerateField.getInt(videoCapturerAndroid);

			Field applicationContextField = videoCapturerAndroid.getClass().getDeclaredField("applicationContext");
			applicationContextField.setAccessible(true);
			Context applicationContext = (Context) applicationContextField.get(videoCapturerAndroid);

			Field frameObserverField = videoCapturerAndroid.getClass().getDeclaredField("frameObserver");
			frameObserverField.setAccessible(true);

			Method startCaptureMethod = videoCapturerAndroid.getClass().getDeclaredMethod("startCapture", int.class, int.class, int.class, Context.class, frameObserverField.getType());
			startCaptureMethod.setAccessible(true);
			startCaptureMethod.invoke(videoCapturerAndroid, requestedWidth, requestedHeight, requestedFramerate, applicationContext, frameObserverField.get(videoCapturerAndroid));

		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Unable to get startCapture field: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to access startCapture field: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unable to access get startCapture method: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unable to invoke startCapture method: " + e.getMessage());
		}
	}

	private void stopCapture() {
		try {
			Method stopCaptureMethod = videoCapturerAndroid.getClass().getDeclaredMethod("stopCapture");
			stopCaptureMethod.setAccessible(true);
			stopCaptureMethod.invoke(videoCapturerAndroid);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unable to get stopCapture method: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unable to invoke stopCapture method: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to access stopCapture method: " + e.getMessage());
		}
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

	}

	@Override
	public void onActivityStarted(Activity activity) {

	}

	@Override
	public void onActivityResumed(Activity activity) {
		resume();
	}

	@Override
	public void onActivityPaused(Activity activity) {
		pause();
	}

	@Override
	public void onActivityStopped(Activity activity) {

	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	@Override
	public void onActivityDestroyed(Activity activity) {

	}

    private native void stopVideoSource(long nativeSession);
    private native void restartVideoSource(long nativeSession);

}
