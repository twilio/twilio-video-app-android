package com.twilio.signal.impl;

import java.lang.reflect.Field;
import java.io.IOException;

import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoCapturerAndroid.CameraErrorHandler;
import org.webrtc.videoengine.VideoCaptureAndroid;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.twilio.signal.CameraCapturer;
import com.twilio.signal.CameraErrorListener;
import com.twilio.signal.CapturerException;
import com.twilio.signal.CapturerException.ExceptionDomain;
import com.twilio.signal.impl.logging.Logger;

public class CameraCapturerImpl implements CameraCapturer {

	private static String TAG = "CameraCapturerImpl";

	static final Logger logger = Logger.getLogger(CameraCapturerImpl.class);

	private final Context context;
	private CameraSource source;

	/* Preview capturer members */
    private final FrameLayout previewLayout;
    private Camera camera;
    private int cameraId;
    private CameraPreview cameraPreview;
    private boolean previewing = false;

	/* Conversation capturer members */
	private ViewGroup captureView;
	private VideoCapturerAndroid webrtcCapturer;
	private CameraErrorListener listener;
	private long nativeWebrtcVideoCapturer;

	private CameraCapturerImpl(Context context, CameraSource source,
			FrameLayout previewLayout, CameraErrorListener listener) {
		this.context = context;
		this.source = source;
		this.previewLayout = previewLayout;
		this.listener = listener;
		determineCameraId();
	}

	public static CameraCapturerImpl create(
			Context context,
			CameraSource source,
			FrameLayout previewContainerView,
			CameraErrorListener listener) {
		CameraCapturerImpl cameraCapturer =
				new CameraCapturerImpl(context, source, previewContainerView, listener);

		return cameraCapturer;
	}

	/*
	 * Use VideoCapturerAndroid to determine the camera id of the specified source.
	 */
	private void determineCameraId() {
		String deviceName;
		if(source == CameraSource.CAMERA_SOURCE_BACK_CAMERA) {
			 deviceName = VideoCapturerAndroid.getNameOfBackFacingDevice();
		} else {
			deviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
		}
		if(deviceName == null) {
			cameraId = 0;
		} else {
			String[] deviceNames = VideoCapturerAndroid.getDeviceNames();
			for(int i = 0; i < deviceNames.length; i++) {
				if(deviceName.equals(deviceNames[i])) {
					cameraId = i;
					break;
				}
			}
		}
	}

    @Override
    public synchronized boolean startPreview() {
        if(previewing) {
            return true;
        }

        if (camera == null) {
            try {
                camera = Camera.open(cameraId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (camera == null) {
                return false;
            }
        }

        // Set camera to continually auto-focus
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(params);

        cameraPreview = new CameraPreview(context, camera);
        previewLayout.removeAllViews();
        previewLayout.addView(cameraPreview);

        previewing = true;
        return true;
    }

    @Override
    public boolean stopPreview() {
        if(previewing) {
            previewLayout.removeAllViews();
            cameraPreview = null;
            camera.release();
            camera = null;
            previewing = false;
        }
        return true;
    }

	@Override
	public boolean isPreviewing() {
		return previewing;
	}

	/*
	 * Called internally prior to a session being started to setup
	 * the capturer used during a Conversation.
	 */
	void startConversationCapturer() {
		if(isPreviewing()) {
			stopPreview();
		}
		boolean success = createWebrtcVideoCapturer();
		if (!success) {
			listener.onError(new CapturerException(ExceptionDomain.CAMERA, "Unable to start the capturer for the conversation"));
		}
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#switchCamera(java.lang.Runnable)
	 */
	@Override
	public boolean switchCamera(Runnable switchDoneEvent) {
        if(previewing) {
            stopPreview();
			cameraId = (cameraId + 1) % Camera.getNumberOfCameras();
			startPreview();
			return true;
        } else if (webrtcCapturer.switchCamera(switchDoneEvent)) {
			return true;
		}
		return false;
	}
	
	long getNativeVideoCapturer()  {
		return nativeWebrtcVideoCapturer;
	}
	
	private boolean obtainNativeVideoCapturer() {
		// Use reflection to obtain the native video capturer handle
		nativeWebrtcVideoCapturer = 0;
		CapturerException exception = null;
		try {
			Field field = webrtcCapturer.getClass().getSuperclass().getDeclaredField("nativeVideoCapturer");
			field.setAccessible(true);
			nativeWebrtcVideoCapturer = field.getLong(webrtcCapturer);
		} catch (NoSuchFieldException e) {
			logger.d("Unable to find webrtc video capturer");
			exception = new CapturerException(ExceptionDomain.WEBRTC, "Unable to find webrtc video capturer: "+e.getMessage());
			
		} catch (IllegalAccessException e) {
			logger.e("Unable to access webrtc video capturer");
			exception = new CapturerException(ExceptionDomain.WEBRTC, "Unable to access webrtc video capturer");
		}
		if ((exception != null) && (listener != null)) {
			listener.onError(exception);
			nativeWebrtcVideoCapturer = 0;
			return false;
		}
		return true;
	}
	
	private String getPrefferedCameraSourceName() {
		if(VideoCapturerAndroid.getDeviceCount() == 0) {
			return null;
		}
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
		String deviceName = VideoCapturerAndroid.getDeviceName(cameraId);
		if(deviceName != null) {
			webrtcCapturer = VideoCapturerAndroid.create(deviceName, cameraErrorHandler);
			if (!obtainNativeVideoCapturer()) {
				return false;
			}
		} else {
			if (listener != null) {
				CapturerException exception = new CapturerException(ExceptionDomain.CAMERA, "Camera device not found");
				listener.onError(exception);
				return false;
			}
		}
		return true;
	}
	
	private CameraErrorHandler cameraErrorHandler = new CameraErrorHandler() {
		
		@Override
		public void onCameraError(String errorMsg) {
			CameraCapturerImpl.this.listener.onError(new CapturerException(ExceptionDomain.CAMERA, errorMsg));
		}
	};

	private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		private Context context;
		private SurfaceHolder holder;
		private Camera camera;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			this.context = context;
			this.camera = camera;

			holder = getHolder();
			holder.addCallback(this);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if (camera != null) {
					camera.setPreviewDisplay(holder);
					camera.startPreview();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if(camera != null)
			{
				camera.stopPreview();

				try{
					camera.setPreviewDisplay(null);

				} catch(IOException e) {
					e.printStackTrace();
				}

			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if (this.holder.getSurface() == null) {
				return;
			}

			if(camera != null) {
				try {
					camera.stopPreview();
					camera.setPreviewDisplay(this.holder);
					updatePreviewOrientation(w, h);
					camera.startPreview();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void updatePreviewOrientation(int width, int height) {
			Camera.Parameters parameters = camera.getParameters();
			Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

			if(display.getRotation() == Surface.ROTATION_0) {
				parameters.setPreviewSize(height, width);
				camera.setDisplayOrientation(90);
			} else if(display.getRotation() == Surface.ROTATION_90) {
				parameters.setPreviewSize(width, height);
				camera.setDisplayOrientation(0);
			} else if(display.getRotation() == Surface.ROTATION_180) {
				parameters.setPreviewSize(height, width);
				camera.setDisplayOrientation(270);
			} else if(display.getRotation() == Surface.ROTATION_270) {
				parameters.setPreviewSize(width, height);
				camera.setDisplayOrientation(180);
			}
		}

	}
}
