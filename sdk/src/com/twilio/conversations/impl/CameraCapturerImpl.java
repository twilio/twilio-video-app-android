package com.twilio.conversations.impl;

import java.io.IOException;
import java.lang.reflect.Field;

import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoCapturerAndroid.CameraErrorHandler;

import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
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


public class CameraCapturerImpl implements CameraCapturer {

	private static String TAG = "CameraCapturerImpl";

	static final Logger logger = Logger.getLogger(CameraCapturerImpl.class);

	private enum CapturerState {
		IDLE,
		PREVIEWING,
		BROADCASTING
	}

	private final Context context;
	private CameraSource source;

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

	private CameraCapturerImpl(Context context, CameraSource source,
			ViewGroup previewContainer, CapturerErrorListener listener) {
		this.context = context;
		this.source = source;
		this.previewContainer = previewContainer;
		this.listener = listener;
		cameraId = getCameraId();
		if(cameraId < 0 && listener != null) {
			listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Invalid camera source."));
		}
	}

	public static CameraCapturerImpl create(
			Context context,
			CameraSource source,
			ViewGroup previewContainer,
			CapturerErrorListener listener) {
		CameraCapturerImpl cameraCapturer =
				new CameraCapturerImpl(context, source, previewContainer, listener);

		return cameraCapturer;
	}

	/*
	 * Use VideoCapturerAndroid to determine the camera id of the specified source.
	 */
	private int getCameraId() {
		String deviceName;
		if(source == CameraSource.CAMERA_SOURCE_BACK_CAMERA) {
			 deviceName = VideoCapturerAndroid.getNameOfBackFacingDevice();
		} else {
			deviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
		}
		if(deviceName == null) {
			cameraId = -1;
		} else {
			String[] deviceNames = VideoCapturerAndroid.getDeviceNames();
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
                    listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to open camera " + VideoCapturerAndroid.getDeviceName(cameraId) + ":" + e.getMessage()));
				}
				return;
            }

            if (camera == null && listener != null) {
               	listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to open camera " + VideoCapturerAndroid.getDeviceName(cameraId)));
				return;
            }
        }

        // Set camera to continually auto-focus
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(params);

        cameraPreview = new CameraPreview(context, camera, listener);
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
	synchronized void startConversationCapturer() {
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
			return videoCapturerAndroid.switchCamera(null);
		} else {
			return false;
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
			if(listener != null) {
				listener.onError(new CapturerException(ExceptionDomain.CAPTURER, "Unable to setup video capturer: " + e.getMessage()));
			}
		} catch (IllegalAccessException e) {
			if(listener != null) {
				listener.onError(new CapturerException(ExceptionDomain.CAPTURER, "Unable to access video capturer: " + e.getMessage()));
			}
		}
		return nativeHandle;
	}
	
	private void createVideoCapturerAndroid() {
		String deviceName = VideoCapturerAndroid.getDeviceName(cameraId);
		if(deviceName == null && listener != null) {
			listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Camera device not found"));
			return;
		}
		videoCapturerAndroid = VideoCapturerAndroid.create(deviceName, cameraErrorHandler);
		nativeVideoCapturerAndroid = retrieveNativeVideoCapturerAndroid(videoCapturerAndroid);
	}
	
	
	private CameraErrorHandler cameraErrorHandler = new CameraErrorHandler() {

		@Override
		public void onCameraError(String errorMsg) {
			if(CameraCapturerImpl.this.listener != null) {
				CameraCapturerImpl.this.listener.onError(new CapturerException(ExceptionDomain.CAMERA, errorMsg));
			}
		}

	};

	private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		private Context context;
		private SurfaceHolder holder;
		private Camera camera;
		private CapturerErrorListener listener;

		public CameraPreview(Context context, Camera camera, CapturerErrorListener listener) {
			super(context);
			this.context = context;
			this.camera = camera;
			this.listener = listener;

			holder = getHolder();
			holder.addCallback(this);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if (camera != null) {
					camera.setPreviewDisplay(holder);
					camera.startPreview();
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
					updatePreviewOrientation(w, h);
				} catch (Exception e) {
					if(listener != null) {
						listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to restart preview: " + e.getMessage()));
					}
				}
			}
		}

		private void updatePreviewOrientation(int width, int height) {
			Camera.Parameters parameters = camera.getParameters();
			Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, info);

			int rotation = getDeviceOrientation();
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				rotation = (info.orientation + rotation - 180) % 360;
			} else {
				rotation = (info.orientation + rotation) % 360;
			}
			camera.setDisplayOrientation(rotation);
		}

		private int getDeviceOrientation() {
			int orientation = 0;

			WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			switch(wm.getDefaultDisplay().getRotation()) {
				case Surface.ROTATION_90:
					orientation = 90;
					break;
				case Surface.ROTATION_180:
					orientation = 0;
					break;
				case Surface.ROTATION_270:
					orientation = 270;
					break;
				case Surface.ROTATION_0:
				default:
					orientation = 180;
					break;
			}
			return orientation;
		}

	}
}
