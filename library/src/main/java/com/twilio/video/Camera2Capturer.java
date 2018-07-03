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

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.SurfaceTextureHelper;

/**
 * The Camera2Capturer class is used to provide video frames for a {@link LocalVideoTrack} from the
 * provided {@link #cameraId}. The frames are provided via a {@link
 * android.hardware.camera2.CameraCaptureSession}. Camera2Capturer must be run on devices {@link
 * android.os.Build.VERSION_CODES#LOLLIPOP} or higher.
 *
 * <p>This class represents an implementation of a {@link VideoCapturer} interface. Although public,
 * these methods are not meant to be invoked directly.
 *
 * <p><b>Note</b>: This capturer can be reused, but cannot be shared across multiple {@link
 * LocalVideoTrack}s simultaneously.
 */
@TargetApi(21)
public class Camera2Capturer implements VideoCapturer {
    private static final Logger logger = Logger.getLogger(Camera2Capturer.class);

    private final Object stateLock = new Object();
    private Camera2Capturer.State state = Camera2Capturer.State.IDLE;
    private final Map<String, List<VideoFormat>> supportedFormatsMap = new HashMap<>();

    private final Context applicationContext;
    private final Camera2Enumerator camera2Enumerator;
    private final Listener listener;
    private final Handler handler;
    private String cameraId;
    private VideoCapturer.Listener videoCapturerListener;
    private org.webrtc.Camera2Capturer webrtcCamera2Capturer;
    private SurfaceTextureHelper surfaceTextureHelper;
    private String pendingCameraId;

    private final CameraVideoCapturer.CameraEventsHandler cameraEventsHandler =
            new CameraVideoCapturer.CameraEventsHandler() {
                @Override
                public void onCameraError(final String errorMessage) {
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    listener.onError(
                                            new Exception(Exception.UNKNOWN, errorMessage));
                                }
                            });
                }

                @Override
                public void onCameraDisconnected() {}

                @Override
                public void onCameraFreezed(final String errorMessage) {
                    logger.e("Camera froze.");
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    listener.onError(
                                            new Exception(Exception.CAMERA_FROZE, errorMessage));
                                }
                            });
                }

                @Override
                public void onCameraOpening(String s) {}

                @Override
                public void onFirstFrameAvailable() {
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    listener.onFirstFrameAvailable();
                                }
                            });
                }

                @Override
                public void onCameraClosed() {}
            };
    private final org.webrtc.VideoCapturer.CapturerObserver observerAdapter =
            new org.webrtc.VideoCapturer.CapturerObserver() {
                @Override
                public void onCapturerStarted(boolean success) {
                    videoCapturerListener.onCapturerStarted(success);

                    // Transition the camera capturer to running state
                    synchronized (stateLock) {
                        state = Camera2Capturer.State.RUNNING;
                    }
                }

                @Override
                public void onCapturerStopped() {
                    // Transition the camera capturer to idle
                    synchronized (stateLock) {
                        state = Camera2Capturer.State.IDLE;
                    }
                }

                @Override
                public void onByteBufferFrameCaptured(
                        byte[] bytes, int width, int height, int rotation, long timestamp) {
                    VideoDimensions frameDimensions = new VideoDimensions(width, height);
                    VideoFrame frame =
                            new VideoFrame(
                                    bytes,
                                    frameDimensions,
                                    VideoFrame.RotationAngle.fromInt(rotation),
                                    timestamp);

                    videoCapturerListener.onFrameCaptured(frame);
                }

                @Override
                public void onTextureFrameCaptured(
                        int width,
                        int height,
                        int oesTextureId,
                        float[] transformMatrix,
                        int rotation,
                        long timestamp) {
                    VideoDimensions frameDimensions = new VideoDimensions(width, height);
                    VideoFrame frame =
                            new VideoFrame(
                                    oesTextureId,
                                    transformMatrix,
                                    frameDimensions,
                                    VideoFrame.RotationAngle.fromInt(rotation),
                                    timestamp);

                    videoCapturerListener.onFrameCaptured(frame);
                }
            };
    private final CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler =
            new CameraVideoCapturer.CameraSwitchHandler() {
                @Override
                public void onCameraSwitchDone(boolean isFrontCamera) {
                    synchronized (Camera2Capturer.this) {
                        cameraId = pendingCameraId;
                        pendingCameraId = null;
                    }
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    listener.onCameraSwitched(cameraId);
                                }
                            });
                }

                @Override
                public void onCameraSwitchError(final String errorMessage) {
                    logger.e("Failed to switch to camera with ID: " + pendingCameraId);
                    pendingCameraId = null;
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    listener.onError(
                                            new Exception(
                                                    Exception.CAMERA_SWITCH_FAILED, errorMessage));
                                }
                            });
                }
            };

    /**
     * Indicates if Camera2Capturer is compatible with device.
     *
     * <p>This method checks that all the following conditions are true: <br>
     *
     * <ol>
     *   <li>The device API level is at least {@link android.os.Build.VERSION_CODES#LOLLIPOP}.
     *   <li>All device cameras have hardware support level greater than {@link
     *       android.hardware.camera2.CameraCharacteristics#INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY}.
     * </ol>
     *
     * <br>
     * For more details on supported hardware levels see the <a
     * href="https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html#INFO_SUPPORTED_HARDWARE_LEVEL">Android
     * documentation</a>.
     *
     * @param context application context.
     * @return true if device supports Camera2Capturer and false if not.
     */
    public static boolean isSupported(@NonNull Context context) {
        Preconditions.checkNotNull(context, "Context must not be null");
        return Camera2Enumerator.isSupported(context);
    }

    /**
     * Constructs a Camera2Capturer instance.
     *
     * <p><b>Note</b>: It is possible to construct multiple instances with different camera IDs, but
     * there are often device limitations on how many camera2 sessions can be open.
     *
     * @param context application context
     * @param cameraId unique identifier of the camera device to open that must be specified in
     *     {@link android.hardware.camera2.CameraManager#getCameraIdList()}.
     * @param listener listener of camera 2 capturer events
     */
    public Camera2Capturer(
            @NonNull Context context, @NonNull String cameraId, @NonNull Listener listener) {
        this(context, cameraId, listener, Util.createCallbackHandler());
    }

    /*
     * Package scope constructor that allows passing in a mocked handler for unit tests.
     */
    @VisibleForTesting
    Camera2Capturer(
            @NonNull Context context,
            @NonNull String cameraId,
            @NonNull Listener listener,
            @NonNull Handler handler) {
        Preconditions.checkState(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP,
                "Camera2Capturer unavailable for " + Build.VERSION.SDK_INT);
        Preconditions.checkNotNull(context, "Context must not be null");
        Preconditions.checkState(
                isSupported(context), "Camera2Capturer is not supported on this device");
        Preconditions.checkNotNull(cameraId, "Camera ID must not be null");
        Preconditions.checkNotNull(listener, "Listener must not be null");
        Preconditions.checkArgument(!cameraId.isEmpty(), "Camera ID must not be empty");
        this.applicationContext = context.getApplicationContext();
        this.camera2Enumerator = new Camera2Enumerator(applicationContext);
        this.cameraId = cameraId;
        this.listener = listener;
        this.handler = handler;
    }

    /**
     * Returns a list of all supported video formats. This list is based on what is specified by
     * {@link android.hardware.camera2.CameraCharacteristics}, so can vary based on a device's
     * camera capabilities.
     *
     * <p><b>Note</b>: This method can be invoked for informational purposes, but is primarily used
     * internally.
     *
     * @return all supported video formats.
     */
    @Override
    public synchronized List<VideoFormat> getSupportedFormats() {
        checkCapturerState();
        List<VideoFormat> supportedFormats = supportedFormatsMap.get(cameraId);

        if (supportedFormats == null) {
            supportedFormats =
                    convertToVideoFormats(camera2Enumerator.getSupportedFormats(cameraId));
            supportedFormatsMap.put(cameraId, supportedFormats);
        }

        return supportedFormats;
    }

    /** Indicates that the camera2 capturer is not a screen cast. */
    @Override
    public boolean isScreencast() {
        return false;
    }

    /**
     * Starts capturing frames at the specified format. Frames will be provided to the given
     * listener upon availability.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.
     *
     * @param captureFormat the format in which to capture frames.
     * @param videoCapturerListener consumer of available frames.
     */
    @Override
    public void startCapture(
            VideoFormat captureFormat, VideoCapturer.Listener videoCapturerListener) {
        checkCapturerState();
        synchronized (stateLock) {
            state = Camera2Capturer.State.STARTING;
        }
        this.webrtcCamera2Capturer =
                (org.webrtc.Camera2Capturer)
                        camera2Enumerator.createCapturer(cameraId, cameraEventsHandler);
        this.videoCapturerListener = videoCapturerListener;
        this.webrtcCamera2Capturer.initialize(
                surfaceTextureHelper, applicationContext, observerAdapter);
        this.webrtcCamera2Capturer.startCapture(
                captureFormat.dimensions.width,
                captureFormat.dimensions.height,
                captureFormat.framerate);
    }

    /**
     * Stops all frames being captured.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.
     */
    @Override
    public void stopCapture() {
        if (webrtcCamera2Capturer != null) {
            synchronized (stateLock) {
                state = State.STOPPING;
            }
            webrtcCamera2Capturer.stopCapture();
            webrtcCamera2Capturer.dispose();
            webrtcCamera2Capturer = null;
        }
    }

    /** Returns the currently set camera ID. */
    public synchronized String getCameraId() {
        return cameraId;
    }

    /**
     * Switches the current {@link #cameraId}.
     *
     * @param newCameraId the new camera id.
     */
    public synchronized void switchCamera(@NonNull final String newCameraId) {
        Preconditions.checkNotNull(newCameraId, "Camera ID must not be null");
        Preconditions.checkArgument(!newCameraId.isEmpty(), "Camera ID must not be empty");
        Preconditions.checkArgument(
                !newCameraId.equals(cameraId),
                "Camera ID must be different " + "from current camera ID");
        Preconditions.checkArgument(
                Camera2Utils.cameraIdSupported(applicationContext, newCameraId),
                "Camera ID %s is not supported or could not be validated",
                newCameraId);
        synchronized (stateLock) {
            if (state != Camera2Capturer.State.IDLE) {
                pendingCameraId = newCameraId;
                webrtcCamera2Capturer.switchCamera(newCameraId, cameraSwitchHandler);
            } else {
                cameraId = newCameraId;
                listener.onCameraSwitched(cameraId);
            }
        }
    }

    void setSurfaceTextureHelper(SurfaceTextureHelper surfaceTextureHelper) {
        this.surfaceTextureHelper = surfaceTextureHelper;
    }

    private void checkCapturerState() {
        Preconditions.checkState(
                Util.permissionGranted(applicationContext, Manifest.permission.CAMERA),
                "CAMERA permission must be granted to create video" + "track with Camera2Capturer");
        Preconditions.checkState(
                Camera2Utils.cameraIdSupported(applicationContext, cameraId),
                "Camera ID %s is not supported or could not be validated",
                cameraId);
    }

    private List<VideoFormat> convertToVideoFormats(
            @NonNull List<CameraEnumerationAndroid.CaptureFormat> captureFormats) {
        final List<VideoFormat> videoFormats = new ArrayList<>(captureFormats.size());

        for (CameraEnumerationAndroid.CaptureFormat captureFormat : captureFormats) {
            VideoDimensions dimensions =
                    new VideoDimensions(captureFormat.width, captureFormat.height);
            int framerate = (captureFormat.framerate.max + 999) / 1000;
            videoFormats.add(new VideoFormat(dimensions, framerate, VideoPixelFormat.NV21));
        }

        return videoFormats;
    }

    /** Camera2Capturer exception class. */
    public static class Exception extends TwilioException {
        @Retention(SOURCE)
        @IntDef({CAMERA_SWITCH_FAILED, CAMERA_FROZE, UNKNOWN})
        public @interface Code {}

        public static final int CAMERA_FROZE = 0;
        public static final int CAMERA_SWITCH_FAILED = 1;
        public static final int UNKNOWN = 2;

        Exception(@Code int code, @NonNull String message, @Nullable String explanation) {
            super(code, message, explanation);
        }

        Exception(@Code int code, @NonNull String message) {
            this(code, message, null);
        }
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

    /** Interface that provides events and errors related to {@link Camera2Capturer}. */
    public interface Listener {
        /** Indicates when the first frame has been captured from the camera. */
        void onFirstFrameAvailable();

        /**
         * Notifies when a camera switch is complete.
         *
         * @param newCameraId the camera ID after camera switch is complete.
         */
        void onCameraSwitched(String newCameraId);

        /**
         * Reports an error that occurred in {@link Camera2Capturer}.
         *
         * @param camera2CapturerException the code that describes the error that occurred.
         */
        void onError(Exception camera2CapturerException);
    }
}
