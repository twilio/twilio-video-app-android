package com.twilio.video;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.WindowManager;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturerAndroid;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * The CameraCapturer class is used to provide video frames for a {@link LocalVideoTrack} from a
 * given {@link CameraSource}. The frames are provided via the preview API of
 * {@link android.hardware.Camera}.
 *
 * <p>This class represents an implementation of a {@link VideoCapturer} interface. Although
 * public, these methods are not meant to be invoked directly.</p>
 *
 * <p><b>Note</b>: This capturer can be reused, but cannot be shared across multiple
 * {@link LocalVideoTrack}s simultaneously.</p>
 */
public class CameraCapturer implements VideoCapturer {
    private static final String ERROR_MESSAGE_CAMERA_SERVER_DIED = "Camera server died!";
    private static final String ERROR_MESSAGE_UNKNOWN = "Camera error:";
    private static final Logger logger = Logger.getLogger(CameraCapturer.class);

    @Retention(SOURCE)
    @IntDef({ERROR_CAMERA_FREEZE,
            ERROR_CAMERA_SERVER_STOPPED,
            ERROR_UNSUPPORTED_SOURCE,
            ERROR_CAMERA_PERMISSION_NOT_GRANTED,
            ERROR_CAPTURER_CREATION_FAILED,
            ERROR_CAMERA_SWITCH_FAILED,
            ERROR_UNKNOWN})
    public @interface Error {}
    public static final int ERROR_CAMERA_FREEZE = 0;
    public static final int ERROR_CAMERA_SERVER_STOPPED = 1;
    public static final int ERROR_UNSUPPORTED_SOURCE = 2;
    public static final int ERROR_CAMERA_PERMISSION_NOT_GRANTED = 3;
    public static final int ERROR_CAPTURER_CREATION_FAILED = 4;
    public static final int ERROR_CAMERA_SWITCH_FAILED = 5;
    public static final int ERROR_UNKNOWN = 6;

    /**
     * Camera source types.
     */
    public enum CameraSource {
        FRONT_CAMERA,
        BACK_CAMERA
    }

    private final Context context;
    private final CameraCapturerFormatProvider formatProvider = new CameraCapturerFormatProvider();
    private CameraCapturer.Listener listener;
    private VideoCapturerAndroid webrtcCapturer;
    private CameraSource cameraSource;
    private Camera.CameraInfo info;
    private VideoCapturer.Listener videoCapturerListener;
    private SurfaceTextureHelper surfaceTextureHelper;
    private final org.webrtc.VideoCapturer.CapturerObserver observerAdapter =
            new org.webrtc.VideoCapturer.CapturerObserver() {
                @Override
                public void onCapturerStarted(boolean success) {
                    videoCapturerListener.onCapturerStarted(success);

                    synchronized (CameraCapturer.this) {
                        /*
                         * Here the user has specified a camera parameter updater. We need to apply
                         * these parameters after the capturer is started to ensure consistency
                         * on the camera capturer instance.
                         */
                        if (cameraParameterUpdater != null) {
                            boolean parameterUpdatedScheduled =
                                    webrtcCapturer.injectCameraParameters(cameraParameterInjector);

                            if (!parameterUpdatedScheduled) {
                                logger.e("Failed to schedule camera parameter update after " +
                                        "capturer started.");
                            }
                        }
                    }
                }

                @Override
                public void onCapturerStopped() {
                    // TODO: This is currently not required but investigate the requirement of this
                }

                @Override
                public void onByteBufferFrameCaptured(byte[] bytes,
                                                      int width,
                                                      int height,
                                                      int rotation,
                                                      long timestamp) {
                    VideoDimensions frameDimensions = new VideoDimensions(width, height);
                    VideoFrame frame = new VideoFrame(bytes, frameDimensions, rotation, timestamp);

                    videoCapturerListener.onFrameCaptured(frame);
                }

                @Override
                public void onTextureFrameCaptured(int width,
                                                   int height,
                                                   int oesTextureId,
                                                   float[] transformMatrix,
                                                   int rotation,
                                                   long timestampNs) {
                    // TODO: Do we need to support capturing to texture?
                }
            };

    private final VideoCapturerAndroid.CameraParameterInjector cameraParameterInjector =
            new VideoCapturerAndroid.CameraParameterInjector() {
                /*
                 * We use the internal CameraParameterInjector we added in WebRTC to apply
                 * a users custom camera parameters.
                 */
                @Override
                public void onCameraParameters(Camera.Parameters parameters) {
                    synchronized (CameraCapturer.this) {
                        if (cameraParameterUpdater != null) {
                            logger.i("Updating camera parameters");
                            cameraParameterUpdater.apply(parameters);
                        }
                    }
                }
            };
    private CameraParameterUpdater cameraParameterUpdater;

    private final VideoCapturerAndroid.CameraEventsHandler cameraEventsHandler =
            new VideoCapturerAndroid.CameraEventsHandler() {
                @Override
                public void onCameraError(String errorMsg) {
                    if (listener != null) {
                        if (errorMsg.equals(ERROR_MESSAGE_CAMERA_SERVER_DIED)) {
                            logger.e("Camera server stopped.");
                            listener.onError(CameraCapturer.ERROR_CAMERA_SERVER_STOPPED);
                        } else if (errorMsg.contains(ERROR_MESSAGE_UNKNOWN)) {
                            logger.e("Unknown camera error occurred.");
                            listener.onError(CameraCapturer.ERROR_UNKNOWN);
                        }
                    }
                }

                @Override
                public void onCameraFreezed(String s) {
                    logger.e("Camera froze.");
                    if(listener != null) {
                        listener.onError(CameraCapturer.ERROR_CAMERA_FREEZE);
                    }
                }

                @Override
                public void onCameraOpening(String message) {
                    // Ignore this event for now
                }

                @Override
                public void onFirstFrameAvailable() {
                    if (listener != null) {
                        listener.onFirstFrameAvailable();
                    }
                }

                @Override
                public void onCameraClosed() {
                    // Ignore this event for now
                }
            };

    private final VideoCapturerAndroid.CameraSwitchHandler cameraSwitchHandler =
            new VideoCapturerAndroid.CameraSwitchHandler() {
                @Override
                public void onCameraSwitchDone(boolean isFrontCamera) {
                    synchronized (CameraCapturer.this) {
                        cameraSource = (cameraSource == CameraSource.FRONT_CAMERA) ?
                                (CameraSource.BACK_CAMERA) :
                                (CameraSource.FRONT_CAMERA);
                    }
                    if (listener != null) {
                        listener.onCameraSwitched();
                    }
                }

                @Override
                public void onCameraSwitchError(String errorMessage) {
                    logger.e("Failed to switch to camera source " + cameraSource);
                    if (listener != null) {
                        listener.onError(ERROR_CAMERA_SWITCH_FAILED);
                    }
                }
            };

    private PictureListener pictureListener;
    private Handler pictureListenerHandler;
    private final VideoCapturerAndroid.PictureEventHandler pictureEventHandler =
            new VideoCapturerAndroid.PictureEventHandler() {
                @Override
                public void onShutter() {
                    if (pictureListener != null) {
                        pictureListenerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                pictureListener.onShutter();
                            }
                        });
                    }
                }

                @Override
                public void onPictureTaken(final byte[] pictureData) {
                    if (pictureListener != null) {
                        // Perform alignment on camera thread
                        final byte[] alignedPictureData = alignPicture(pictureData);

                        pictureListenerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                pictureListener.onPictureTaken(alignedPictureData);
                            }
                        });
                    }
                }
            };



    public CameraCapturer(Context context, CameraSource cameraSource) {
        this(context, cameraSource, null);
    }

    public CameraCapturer(Context context, CameraSource cameraSource, @Nullable Listener listener) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (cameraSource == null) {
            throw new NullPointerException("camera source must not be null");
        }
        this.context = context;
        this.cameraSource = cameraSource;
        this.listener = listener;
    }

    /**
     * Returns a list of all supported video formats. This list is based on what is specified by
     * {@link android.hardware.Camera.Parameters}, so can vary based on a device's camera
     * capabilities.
     *
     * <p><b>Note</b>: This method can be invoked for informational purposes, but is primarily used
     * internally.</p>
     *
     * @return all supported video formats.
     */
    @Override
    public List<VideoFormat> getSupportedFormats() {
        // Camera permission is needed to query the supported formats of the device
        if (Util.permissionGranted(context, Manifest.permission.CAMERA)) {
            return formatProvider.getSupportedFormats(cameraSource);
        } else {
            /*
             * Return default parameters and permission error will be surfaced when the capturing
             * attempts to start.
             */
            return defaultFormats();
        }
    }

    /**
     * Indicates that the camera capturer is not a screen cast.
     */
    @Override
    public boolean isScreencast() {
        return false;
    }

    /**
     * Starts capturing frames at the specified format. Frames will be provided to the given
     * listener upon availability.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.</p>
     *
     * @param captureFormat the format in which to capture frames.
     * @param videoCapturerListener consumer of available frames.
     */
    @Override
    public void startCapture(VideoFormat captureFormat,
                             VideoCapturer.Listener videoCapturerListener) {
        boolean capturerCreated = createVideoCapturerAndroid();
        if (capturerCreated) {
            this.videoCapturerListener = videoCapturerListener;

            webrtcCapturer.initialize(surfaceTextureHelper, context, observerAdapter);
            webrtcCapturer.startCapture(captureFormat.dimensions.width,
                    captureFormat.dimensions.height,
                    captureFormat.framerate);
        } else {
            logger.e("Failed to startCapture");
            videoCapturerListener.onCapturerStarted(false);
        }
    }

    /**
     * Stops all frames being captured. The {@link android.hardware.Camera} interface should
     * be available for use upon completion.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.</p>
     */
    @Override
    public void stopCapture() {
        if (webrtcCapturer != null) {
            try {
                webrtcCapturer.stopCapture();
            } catch (InterruptedException e) {
                logger.e("Failed to stop camera capturer");
            }
            webrtcCapturer.dispose();
            webrtcCapturer = null;
        }
    }

    /**
     * Returns the currently specified camera source.
     */
    public synchronized CameraSource getCameraSource() {
        return cameraSource;
    }

    /**
     * Switches the current {@link CameraSource}. This method can be invoked while capturing frames
     * or not.
     */
    public synchronized void switchCamera() {
        if (webrtcCapturer != null) {
            webrtcCapturer.switchCamera(cameraSwitchHandler);
        } else {
            cameraSource = (cameraSource == CameraSource.FRONT_CAMERA) ?
                    (CameraSource.BACK_CAMERA) :
                    (CameraSource.FRONT_CAMERA);
            if (listener != null) {
                listener.onCameraSwitched();
            }
        }
    }

    /**
     * Schedules a camera parameter update. The current camera's
     * {@link android.hardware.Camera.Parameters} will be provided for modification via
     * {@link CameraParameterUpdater#apply(Camera.Parameters)}. Any changes
     * to the parameters will be applied after the invocation of this callback. This method can be
     * invoked while capturing frames or not.
     *
     * <p>
     *     The following snippet demonstrates how to turn on the flash of a camera while capturing.
     * </p>
     *
     * <pre><code>
     *     // Create local media and camera capturer
     *     LocalMedia localMedia = LocalMedia.create(context);
     *     CameraCapturer cameraCapturer = new CameraCapturer(context,
     *          CameraCapturer.CameraSource.BACK_CAMERA, null);
     *
     *     // Start camera capturer
     *     localMedia.addVideoTrack(true, cameraCapturer);
     *
     *     // Schedule camera parameter update
     *     cameraCapturer.updateCameraParameters(new CameraParameterUpdater() {
     *        {@literal @}Override
     *         public void apply(Camera.Parameters cameraParameters) {
     *             // Ensure camera supports flash and turn on
     *             if (cameraParameters.getFlashMode() != null) {
     *                  cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
     *             }
     *         }
     *     });
     * </code></pre>
     *
     * @param cameraParameterUpdater camera parameter updater that receives current camera
     *                               parameters for modification.
     * @return true if update was scheduled or false if an update is pending or could not be
     * scheduled.
     */
    public synchronized boolean updateCameraParameters(CameraParameterUpdater cameraParameterUpdater) {
        // Assume that parameter update can be scheduled
        boolean parameterUpdateScheduled = true;

        /*
         * If the camera capturer is running we can apply the parameters immediately. Otherwise
         * the parameters will be applied when the camera capturer is started again.
         */
        if (webrtcCapturer != null) {
            parameterUpdateScheduled =
                    webrtcCapturer.injectCameraParameters(cameraParameterInjector);
        }

        // Only set parameter updater if we scheduled the injection
        if (parameterUpdateScheduled) {
            this.cameraParameterUpdater = cameraParameterUpdater;
        }

        return parameterUpdateScheduled;
    }

    /**
     * Schedules an image capture. This call will only succeed while capturing frames.
     *
     * <p>
     *     The following snippet demonstrates how to capture and image and decode to a
     *     {@link android.graphics.Bitmap}.
     * </p>
     *
     * <pre><code>
     *     // Create local media and camera capturer
     *     LocalMedia localMedia = LocalMedia.create(context);
     *     CameraCapturer cameraCapturer = new CameraCapturer(context,
     *          CameraCapturer.CameraSource.BACK_CAMERA, null);
     *
     *     // Start camera capturer
     *     localMedia.addVideoTrack(true, cameraCapturer);
     *
     *     // Schedule an image capture
     *     cameraCapturer.takePicture(new CameraCapturer.PictureListener() {
     *        {@literal @}Override
     *         public void onShutter() {
     *             // Show some UI or play a sound
     *         }
     *
     *        {@literal @}Override
     *         public void onPictureTaken(byte[] pictureData) {
     *             Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0,
     *                 pictureData.length);
     *         }
     *     });
     * </code></pre>
     *
     * @param pictureListener listener that that receives the callback for the shutter and picture
     *                        taken event.
     * @return true if picture was scheduled to be taken or false if a picture is pending or could
     * not be scheduled.
     */
    public synchronized boolean takePicture(@NonNull PictureListener pictureListener) {
        pictureListenerHandler = Util.createCallbackHandler();
        if (webrtcCapturer != null) {
            this.pictureListener = pictureListener;
            return webrtcCapturer.takePicture(pictureEventHandler);
        } else {
            logger.e("Picture cannot be taken unless camera capturer is running");
            return false;
        }
    }

    void setSurfaceTextureHelper(SurfaceTextureHelper surfaceTextureHelper) {
        this.surfaceTextureHelper = surfaceTextureHelper;
    }

    private List<VideoFormat> defaultFormats() {
        List<VideoFormat> defaultFormats = new ArrayList<>();
        VideoDimensions defaultDimensions = new VideoDimensions(640, 480);
        VideoFormat defaultFormat = new VideoFormat(defaultDimensions, 30, VideoPixelFormat.NV21);

        defaultFormats.add(defaultFormat);

        return defaultFormats;
    }

    private boolean createVideoCapturerAndroid() {
        if (!Util.permissionGranted(context, Manifest.permission.CAMERA)) {
            logger.e("CAMERA permission must be granted to start capturer");
            if (listener != null) {
                listener.onError(ERROR_CAMERA_PERMISSION_NOT_GRANTED);
            }
            return false;
        }
        int cameraId = CameraCapturerFormatProvider.getCameraId(cameraSource);
        String deviceName = CameraEnumerationAndroid.getDeviceName(cameraId);

        if (cameraId < 0 || deviceName == null) {
            logger.e("Failed to find camera source");
            if (listener != null) {
                listener.onError(ERROR_UNSUPPORTED_SOURCE);
            }
            return false;
        }
        // TODO: Need to figure out the best way to get this to to webrtc
        // final EglBase.Context eglContext = EglBaseProvider.provideEglBase().getEglBaseContext();
        webrtcCapturer = VideoCapturerAndroid.create(deviceName, cameraEventsHandler);

        if (webrtcCapturer == null) {
            logger.e("Failed to create capturer");
            if (listener != null) {
                listener.onError(ERROR_CAPTURER_CREATION_FAILED);
            }
            return false;
        }

        return true;
    }

    /*
     * Aligns the picture data according to the current device orientation and camera source.
     */
    private byte[] alignPicture(byte[] pictureData) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
        int degree = getFrameOrientation();
        Matrix matrix = new Matrix();

        // Compensate for front camera mirroring
        if(cameraSource == CameraSource.FRONT_CAMERA) {
            switch (degree) {
                case 0:
                case 180:
                    matrix.setScale(-1, 1);
                    break;
                case 90:
                case 270:
                    matrix.setScale(1, -1);
                    break;
                default:
                    break;
            }

        }

        // Apply rotation
        matrix.postRotate(degree);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        // Write to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        return stream.toByteArray();
    }

    private int getFrameOrientation() {
        int rotation = getDeviceOrientation();

        if (info == null) {
            info = getCameraInfo();
        }
        if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
            rotation = 360 - rotation;
        }

        return (info.orientation + rotation) % 360;
    }

    private int getDeviceOrientation() {
        int orientation = 0;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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

    /*
     * FIXME
     * Remove this once we have completed upgrade to WebRTC 55. This information can be provided
     * without reflection with some small changes to WebRTC 55.
     */
    private Camera.CameraInfo getCameraInfo() {
        Camera.CameraInfo cameraInfo;
        try {
            Field cameraInfoField = webrtcCapturer.getClass().getDeclaredField("info");
            cameraInfoField.setAccessible(true);
            cameraInfo = (Camera.CameraInfo) cameraInfoField.get(webrtcCapturer);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Unable to retrieve camera info");
        } catch( IllegalAccessException e) {
            throw new RuntimeException("Could not access camera info");
        }

        return cameraInfo;
    }

    /**
     * Interface that provides events and errors related to {@link CameraCapturer}.
     */
    public interface Listener {
        /**
         * Indicates when the first frame has been captured from the camera.
         */
        void onFirstFrameAvailable();

        /**
         * Notifies when a camera switch is complete.
         */
        void onCameraSwitched();

        /**
         * Reports an error that occurred in {@link CameraCapturer}.
         *
         * @param errorCode the code that describes the error that occurred.
         */
        void onError(@CameraCapturer.Error int errorCode);
    }

    /**
     * Interface that provides events related to taking a picture while capturing.
     */
    public interface PictureListener {
        /**
         * Invoked when photo is captured from sensor. This callback is a wrapper of
         * {@link Camera.ShutterCallback#onShutter()}.
         */
        void onShutter();

        /**
         * Invoked when picture data is available.
         *
         * @param pictureData JPEG picture data.
         */
        void onPictureTaken(byte[] pictureData);
    }
}
