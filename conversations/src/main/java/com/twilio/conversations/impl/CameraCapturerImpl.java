package com.twilio.conversations.impl;

import java.io.IOException;
import java.lang.reflect.Field;

import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoCapturerAndroid.CameraEventsHandler;
import org.webrtc.CameraEnumerationAndroid;

import android.content.Context;
import android.hardware.Camera;
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
import com.twilio.conversations.R;
import com.twilio.conversations.impl.logging.Logger;


public class CameraCapturerImpl implements CameraCapturer {
    private static String TAG = "CameraCapturerImpl";

    static final Logger logger = Logger.getLogger(CameraCapturerImpl.class);
    private long session;

    private enum CapturerState {
        IDLE,
        PREVIEWING,
        BROADCASTING
    }

    private final Context context;
    private CameraSource source;
    private CapturerState lastCapturerState;

    // Preview capturer members
    private ViewGroup previewContainer;
    private Camera camera;
    private int cameraId;
    private CapturerPreview capturerPreview;
    private CapturerState capturerState = CapturerState.IDLE;

    // Conversation capturer members
    private VideoCapturerAndroid videoCapturerAndroid;
    private CapturerErrorListener listener;
    private long nativeVideoCapturerAndroid;
    private boolean broadcastCapturerPaused = false;

    private CameraCapturerImpl(Context context,
                               CameraSource source,
                               CapturerErrorListener listener) {
        if(context == null) {
            throw new NullPointerException("context must not be null");
        }
        if(source == null) {
            throw new NullPointerException("source must not be null");
        }

        this.context = context;
        this.source = source;
        this.listener = listener;
        cameraId = getCameraId();
        if(cameraId < 0 && listener != null) {
            listener.onError(new CapturerException(ExceptionDomain.CAMERA,
                    "Invalid camera source."));
        }
    }

    public static CameraCapturerImpl create(
            Context context,
            CameraSource source,
            ViewGroup previewContainer,
            CapturerErrorListener listener) {
        CameraCapturerImpl cameraCapturer =
                new CameraCapturerImpl(context, source, listener);
        cameraCapturer.previewContainer = previewContainer;

        return cameraCapturer;
    }

    /**
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
    public void startPreview(ViewGroup previewContainer) {
        if (this.previewContainer != null) {
            this.previewContainer.removeAllViews();
        }
        this.previewContainer = previewContainer;
        startPreview();
    }

    @Override
    public synchronized void startPreview() {
        if(capturerState.equals(CapturerState.PREVIEWING) ||
                capturerState.equals(CapturerState.BROADCASTING)) {
            return;
        }

        if (previewContainer == null && listener != null) {
            listener.onError(new CapturerException(ExceptionDomain.CAMERA,
                    "Cannot start preview without a preview container"));
            return;
        }

        if (camera == null) {
            try {
                camera = Camera.open(cameraId);
            } catch (Exception e) {
                if(listener != null) {
                    listener.onError(new CapturerException(ExceptionDomain.CAMERA,
                            "Unable to open camera " +
                                    CameraEnumerationAndroid.getDeviceName(cameraId) + ":" +
                                    e.getMessage()));
                }
                return;
            }

            if (camera == null && listener != null) {
                listener.onError(new CapturerException(ExceptionDomain.CAMERA,
                        "Unable to open camera " +
                                CameraEnumerationAndroid.getDeviceName(cameraId)));
                return;
            }
        }

        // Set camera to continually auto-focus
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes()
                .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(params);

        capturerPreview = new CapturerPreview(context, camera, listener);
        previewContainer.removeAllViews();
        previewContainer.addView(capturerPreview);

        capturerState = CapturerState.PREVIEWING;
    }

    @Override
    public synchronized void stopPreview() {
        if(capturerState.equals(CapturerState.PREVIEWING)) {
            if (previewContainer != null) {
                previewContainer.removeAllViews();
            }
            capturerPreview = null;
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

    /**
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
        } else if (capturerState.equals(CapturerState.BROADCASTING) && !broadcastCapturerPaused) {
            // TODO: propagate error
            videoCapturerAndroid.switchCamera(null);
            return true;
        } else {
            return false;
        }
    }

    void pause() {
        lastCapturerState = capturerState;
        if(capturerState.equals(CapturerState.BROADCASTING)) {
            stopVideoSource(session);
            broadcastCapturerPaused = true;
        }
    }

    void resume() {
        if(lastCapturerState != null) {
            if(lastCapturerState.equals(CapturerState.BROADCASTING)) {
                restartVideoSource(session);
            }
            lastCapturerState = null;
            broadcastCapturerPaused = false;
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
            Field field = videoCapturerAndroid.getClass()
                    .getSuperclass().getDeclaredField("nativeVideoCapturer");
            field.setAccessible(true);
            nativeHandle = field.getLong(videoCapturerAndroid);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Unable to get nativeVideoCapturer field: " +
                    e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access nativeVideoCapturer field: " +
                    e.getMessage());
        }

        return nativeHandle;
    }

    private void createVideoCapturerAndroid() {
        String deviceName = CameraEnumerationAndroid.getDeviceName(cameraId);
        if(deviceName == null && listener != null) {
            listener.onError(new CapturerException(ExceptionDomain.CAMERA,
                    "Camera device not found"));
            return;
        }
        videoCapturerAndroid = VideoCapturerAndroid.create(deviceName, cameraEventsHandler);
        nativeVideoCapturerAndroid = retrieveNativeVideoCapturerAndroid(videoCapturerAndroid);
    }


    private final CameraEventsHandler cameraEventsHandler = new CameraEventsHandler() {
        @Override
        public void onCameraError(String errorMsg) {
            if(CameraCapturerImpl.this.listener != null) {
                CameraCapturerImpl.this.listener
                        .onError(new CapturerException(ExceptionDomain.CAMERA, errorMsg));
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

    private class CapturerPreview extends SurfaceView implements SurfaceHolder.Callback {
        private Context context;
        private SurfaceHolder holder;
        private Camera camera;
        private CapturerErrorListener listener;
        private OrientationEventListener orientationEventListener;

        public CapturerPreview(Context context, Camera camera, CapturerErrorListener listener) {
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
            setContentDescription(context.getString(R.string.capturer_preview_content_description));
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
                    listener.onError(new CapturerException(ExceptionDomain.CAMERA,
                            "Unable to start preview: " + e.getMessage()));
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
                        listener.onError(new CapturerException(ExceptionDomain.CAMERA,
                                "Unable to reset preview: " + e.getMessage()));
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
                        listener.onError(new CapturerException(ExceptionDomain.CAMERA,
                                "Unable to restart preview: " + e.getMessage()));
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

    private native void stopVideoSource(long nativeSession);
    private native void restartVideoSource(long nativeSession);
}
