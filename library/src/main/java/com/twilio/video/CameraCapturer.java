/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.support.annotation.VisibleForTesting;
import android.view.Surface;
import android.view.WindowManager;

import org.webrtc.Camera1Capturer;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera1Session;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.ThreadUtils;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Retention;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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
    /*
     * Some devices take up to three seconds before the camera resource is released and WebRTC
     * notifies this class that the camera session is closed.
     */
    private static final int CAMERA_CLOSED_TIMEOUT_MS = 3000;
    private static final String CAMERA_CLOSED_FAILED = "Failed to close camera";
    private static final String ERROR_MESSAGE_CAMERA_SERVER_DIED = "Camera server died!";
    private static final String ERROR_MESSAGE_UNKNOWN = "Camera error:";
    private static final Logger logger = Logger.getLogger(CameraCapturer.class);

    @Retention(SOURCE)
    @IntDef({ERROR_CAMERA_FREEZE,
            ERROR_CAMERA_SERVER_STOPPED,
            ERROR_UNSUPPORTED_SOURCE,
            ERROR_CAMERA_PERMISSION_NOT_GRANTED,
            ERROR_CAMERA_SWITCH_FAILED,
            ERROR_UNKNOWN})
    public @interface Error {}
    public static final int ERROR_CAMERA_FREEZE = 0;
    public static final int ERROR_CAMERA_SERVER_STOPPED = 1;
    public static final int ERROR_UNSUPPORTED_SOURCE = 2;
    public static final int ERROR_CAMERA_PERMISSION_NOT_GRANTED = 3;
    public static final int ERROR_CAMERA_SWITCH_FAILED = 5;
    public static final int ERROR_UNKNOWN = 6;

    /**
     * Camera source types.
     */
    public enum CameraSource {
        FRONT_CAMERA,
        BACK_CAMERA
    }

    /*
     * State definitions used to control interactions with the public API
     */
    private enum State {
        IDLE,
        STARTING,
        RUNNING,
        STOPPING
    }

    // These fields are used to safely transition between CameraCapturer states
    private final Object stateLock = new Object();
    private State state = State.IDLE;

    private final Context context;
    private final CameraCapturerFormatProvider formatProvider;
    private final AtomicBoolean picturePending = new AtomicBoolean(false);
    private final AtomicBoolean parameterUpdatePending = new AtomicBoolean(false);
    private CameraCapturer.Listener listener;
    private org.webrtc.Camera1Capturer webRtcCameraCapturer;
    private CameraSource cameraSource;
    private Camera1Session camera1Session;
    private VideoCapturer.Listener videoCapturerListener;
    private SurfaceTextureHelper surfaceTextureHelper;
    private final org.webrtc.VideoCapturer.CapturerObserver observerAdapter =
            new org.webrtc.VideoCapturer.CapturerObserver() {
                @Override
                public void onCapturerStarted(boolean success) {
                    videoCapturerListener.onCapturerStarted(success);

                    // Cache camera session immediately
                    CameraCapturer.this.camera1Session =
                            webRtcCameraCapturer.getCameraSession();

                    // Transition the camera capturer to running state
                    synchronized (stateLock) {
                        /*
                         * We only transition from STARTING to RUNNING
                         */
                        if (state == State.STARTING) {
                            state = State.RUNNING;
                            /*
                             * The user requested a camera parameter update while the capturer was
                             * not running. We need to apply these parameters after the capturer
                             * is started to ensure consistency on the camera capturer instance.
                             */
                            if (cameraParameterUpdater != null) {
                                updateCameraParametersOnCameraThread(cameraParameterUpdater);
                                cameraParameterUpdater = null;
                            }

                            /*
                             * The user requested a picture while capturer was not running. We
                             * service the request once we know the capturer is running so user
                             * does not have to wait for capturer to start to take a picture.
                             */
                            if (pictureListener != null) {
                                takePicture(pictureListener);
                                pictureListener = null;
                            }
                        } else {
                            logger.w("Attempted to transition from " + state + " to RUNNING");
                        }
                    }

                }

                @Override
                public void onCapturerStopped() {
                    /*
                     * Ignore this event because it does not accurately indicate that the we
                     * have stopped capturing frames and the camera resource is freed.
                     */
                }

                @Override
                public void onByteBufferFrameCaptured(byte[] bytes,
                                                      int width,
                                                      int height,
                                                      int rotation,
                                                      long timestamp) {
                    VideoDimensions frameDimensions = new VideoDimensions(width, height);
                    VideoFrame frame = new VideoFrame(bytes,
                            frameDimensions,
                            VideoFrame.RotationAngle.fromInt(rotation),
                            timestamp);

                    videoCapturerListener.onFrameCaptured(frame);
                }

                @Override
                public void onTextureFrameCaptured(int width,
                                                   int height,
                                                   int oesTextureId,
                                                   float[] transformMatrix,
                                                   int rotation,
                                                   long timestamp) {
                    VideoDimensions frameDimensions = new VideoDimensions(width, height);
                    VideoFrame frame = new VideoFrame(oesTextureId,
                            transformMatrix,
                            frameDimensions,
                            VideoFrame.RotationAngle.fromInt(rotation),
                            timestamp);

                    videoCapturerListener.onFrameCaptured(frame);
                }
            };
    private CameraParameterUpdater cameraParameterUpdater;
    private PictureListener pictureListener;

    private CountDownLatch cameraClosed;
    private final CameraVideoCapturer.CameraEventsHandler cameraEventsHandler =
            new CameraVideoCapturer.CameraEventsHandler() {
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
                    synchronized (stateLock) {
                        if (state == State.STOPPING) {
                            // Null out the camera session because it is no longer usable
                            CameraCapturer.this.camera1Session = null;

                            // We are awaiting the camera being freed in stopCapture
                            cameraClosed.countDown();
                        }
                    }
                }

                @Override
                public void onCameraDisconnected() {
                    // TODO: do we need to handle this?
                }
            };

    private final CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler =
            new CameraVideoCapturer.CameraSwitchHandler() {
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

    /**
     * Indicates if a camera source is available on the device.
     *
     * @param cameraSource the camera source
     * @return true if source is available on device and false otherwise.
     */
    public static boolean isSourceAvailable(@NonNull CameraSource cameraSource) {
        Preconditions.checkNotNull(cameraSource, "Camera source must not be null");
        CameraCapturerFormatProvider cameraCapturerFormatProvider =
                new CameraCapturerFormatProvider();

        return isSourceAvailable(cameraCapturerFormatProvider, cameraSource);
    }

    static boolean isSourceAvailable(@NonNull CameraCapturerFormatProvider cameraCapturerFormatProvider,
                                     @NonNull CameraSource cameraSource) {
        return cameraCapturerFormatProvider.getCameraId(cameraSource) != -1;
    }

    public CameraCapturer(Context context, CameraSource cameraSource) {
        this(context, cameraSource, null);
    }

    public CameraCapturer(@NonNull Context context,
                          @NonNull CameraSource cameraSource,
                          @Nullable Listener listener) {
        this(context, cameraSource, listener, new CameraCapturerFormatProvider());
    }

    /*
     * Visible for tests so we can provide a mocked format provider to emulate cases where
     * an error occurs connecting to camera service.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    CameraCapturer(@NonNull Context context,
                   @NonNull CameraSource cameraSource,
                   @Nullable Listener listener,
                   @NonNull CameraCapturerFormatProvider formatProvider) {
        Preconditions.checkNotNull(context, "Context must not be null");
        Preconditions.checkNotNull(cameraSource, "Camera source must not be null");
        Preconditions.checkArgument(isSourceAvailable(formatProvider, cameraSource),
                String.format("%s is not supported on this device", cameraSource));
        this.context = context;
        this.cameraSource = cameraSource;
        this.listener = listener;
        this.formatProvider = formatProvider;
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
    public synchronized List<VideoFormat> getSupportedFormats() {
        Preconditions.checkState(Util.permissionGranted(context,
                Manifest.permission.CAMERA), "CAMERA permission must be granted to create video" +
                "track with CameraCapturer");
        List<VideoFormat> supportedFormats = formatProvider.getSupportedFormats(cameraSource);
        Preconditions.checkState(!supportedFormats.isEmpty(), "Supported formats could not be " +
                "retrieved because an error occurred connecting to the camera service");

        return supportedFormats;
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
        boolean capturerCreated = createWebRtcCameraCapturer();
        if (capturerCreated) {
            synchronized (stateLock) {
                state = State.STARTING;
            }
            this.videoCapturerListener = videoCapturerListener;

            webRtcCameraCapturer.initialize(surfaceTextureHelper, context, observerAdapter);
            webRtcCameraCapturer.startCapture(captureFormat.dimensions.width,
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
        if (webRtcCameraCapturer != null) {
            synchronized (stateLock) {
                state = State.STOPPING;
                cameraClosed = new CountDownLatch(1);
            }
            webRtcCameraCapturer.stopCapture();
            webRtcCameraCapturer.dispose();
            webRtcCameraCapturer = null;

            /*
             * Wait until the camera closed event has fired. This event indicates
             * that CameraCapturer stopped capturing and that the camera resource is released. If
             * the event is not received then log an error and developer will be notified via
             * onError callback.
             */
            if (!ThreadUtils.awaitUninterruptibly(cameraClosed, CAMERA_CLOSED_TIMEOUT_MS)) {
                logger.e("Camera closed event not received");
            }
            synchronized (stateLock) {
                cameraClosed = null;
                state = State.IDLE;
            }
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
        CameraSource nextCameraSource = cameraSource == CameraSource.FRONT_CAMERA ?
                CameraSource.BACK_CAMERA :
                CameraSource.FRONT_CAMERA;
        boolean nextCameraSourceSupported = isSourceAvailable(formatProvider, nextCameraSource);

        if (!nextCameraSourceSupported) {
            logger.w(String.format("Cannot switch to unsupported camera source %s",
                    nextCameraSource));
            return;
        }

        synchronized (stateLock) {
            if (state != State.IDLE) {
                webRtcCameraCapturer.switchCamera(cameraSwitchHandler);
            } else {
                cameraSource = nextCameraSource;
                if (listener != null) {
                    listener.onCameraSwitched();
                }
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
     *     // Create camera capturer
     *     CameraCapturer cameraCapturer = new CameraCapturer(context,
     *          CameraCapturer.CameraSource.BACK_CAMERA, null);
     *
     *     // Start camera capturer
     *     LocalVideoTrack cameraVideoTrack = LocalVideoTrack.create(context, true, cameraCapturer);
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
    public synchronized boolean updateCameraParameters(@NonNull final CameraParameterUpdater cameraParameterUpdater) {
        synchronized (stateLock) {
            if (state == State.RUNNING) {
                if (!parameterUpdatePending.get()) {
                    parameterUpdatePending.set(true);
                    return camera1Session.cameraThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateCameraParametersOnCameraThread(cameraParameterUpdater);
                        }
                    });
                } else {
                    logger.w("Parameters will not be applied with parameter update pending");
                    return false;
                }
            } else {
                logger.i("Camera capturer is not running. Parameters will be applied when " +
                        "camera capturer is resumed");
                this.cameraParameterUpdater = cameraParameterUpdater;

                return true;
            }
        }
    }

    /**
     * Schedules an image capture.
     *
     * <p>
     *     The following snippet demonstrates how to capture and image and decode to a
     *     {@link android.graphics.Bitmap}.
     * </p>
     *
     * <pre><code>
     *     // Create camera capturer
     *     CameraCapturer cameraCapturer = new CameraCapturer(context,
     *          CameraCapturer.CameraSource.BACK_CAMERA, null);
     *
     *     // Start camera capturer
     *     LocalVideoTrack cameraVideoTrack = LocalVideoTrack.create(context, true, cameraCapturer)
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
    public synchronized boolean takePicture(@NonNull final PictureListener pictureListener) {
        synchronized (stateLock) {
            if (state == State.RUNNING) {
                if (!picturePending.get()) {
                    picturePending.set(true);
                    final Handler pictureListenerHandler = Util.createCallbackHandler();

                    return camera1Session.cameraThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            takePictureOnCameraThread(pictureListenerHandler, pictureListener);
                        }
                    });
                } else {
                    logger.w("Picture cannot be taken while picture is pending");

                    return false;
                }
            } else {
                logger.i("Camera capturer is not running. Picture request will be serviced " +
                        "when camera capturer is resumed");
                this.pictureListener = pictureListener;

                return true;
            }
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

    private boolean createWebRtcCameraCapturer() {
        if (!Util.permissionGranted(context, Manifest.permission.CAMERA)) {
            logger.e("CAMERA permission must be granted to start capturer");
            if (listener != null) {
                listener.onError(ERROR_CAMERA_PERMISSION_NOT_GRANTED);
            }
            return false;
        }
        int cameraId = formatProvider.getCameraId(cameraSource);
        String deviceName = formatProvider.getDeviceName(cameraId);

        if (cameraId < 0 || deviceName == null) {
            logger.e("Failed to find camera source");
            if (listener != null) {
                listener.onError(ERROR_UNSUPPORTED_SOURCE);
            }
            return false;
        }
        /*
         * Disable capturing and encoding to texture until we understand issues GSDK-1132 GSDK-1139
         */
        webRtcCameraCapturer = new Camera1Capturer(deviceName, cameraEventsHandler, false);

        return true;
    }

    /*
     * Aligns the picture data according to the current device orientation and camera source.
     */
    private byte[] alignPicture(Camera.CameraInfo info, byte[] pictureData) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);

        /*
         * Bitmap is not guaranteed to be decoded correctly. In this scenario the original picture
         * data is returned without being aligned.
         */
        if (bitmap != null) {
            int degree = getFrameOrientation(info);
            Matrix matrix = new Matrix();

            // Compensate for front camera mirroring
            if (cameraSource == CameraSource.FRONT_CAMERA) {
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
        } else {
            logger.e("Failed to align picture data. Returning original image.");
            return pictureData;
        }
    }

    private int getFrameOrientation(Camera.CameraInfo info) {
        int rotation = getDeviceOrientation();

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

    private void updateCameraParametersOnCameraThread(final CameraParameterUpdater cameraParameterUpdater) {
        synchronized (stateLock) {
            if (state == State.RUNNING) {
                // Validate execution on camera thread
                camera1Session.checkIsOnCameraThread();

                // Grab camera parameters and forward to updater
                Camera.Parameters cameraParameters = camera1Session.camera.getParameters();
                logger.i("Applying camera parameters");
                cameraParameterUpdater.apply(cameraParameters);

                // Stop preview and clear internal camera buffer to avoid camera freezes
                camera1Session.camera.stopPreview();
                camera1Session.camera.setPreviewCallbackWithBuffer(null);

                // Apply the parameters
                camera1Session.camera.setParameters(cameraParameters);

                // Reinitialize the preview callback and buffer.
                final int frameSize = camera1Session.captureFormat.frameSize();
                for (int i = 0; i < Camera1Session.NUMBER_OF_CAPTURE_BUFFERS; i++) {
                    final ByteBuffer buffer = ByteBuffer.allocateDirect(frameSize);
                    camera1Session.camera.addCallbackBuffer(buffer.array());
                }
                camera1Session.listenForBytebufferFrames();

                // Resume preview
                camera1Session.camera.startPreview();
            } else {
                logger.w("Attempted to update camera parameters while camera capturer is " +
                        "not running");
            }

            // Clear the parameter updating flag
            parameterUpdatePending.set(false);
        }
    }

    private void takePictureOnCameraThread(final Handler pictureListenerHandler,
                                           final PictureListener pictureListener) {
        synchronized (stateLock) {
            if (state == State.RUNNING) {
                // Validate execution on camera thread
                camera1Session.checkIsOnCameraThread();

                final Camera.CameraInfo info = camera1Session.info;
                camera1Session.camera
                        .takePicture(new android.hardware.Camera.ShutterCallback() {
                            @Override
                            public void onShutter() {
                                pictureListenerHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        pictureListener.onShutter();
                                    }
                                });
                            }
                        }, null, new android.hardware.Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] pictureData,
                                                       android.hardware.Camera camera) {
                                final byte[] alignedPictureData = alignPicture(info, pictureData);
                                pictureListenerHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        pictureListener.onPictureTaken(alignedPictureData);
                                        picturePending.set(false);
                                    }
                                });
                                synchronized (stateLock) {
                                    if (state == State.RUNNING) {
                                        camera1Session.camera.startPreview();
                                    }
                                }
                            }
                        });
            } else {
                logger.w("Attempted to take picture while capturing is not running");
            }
        }
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
