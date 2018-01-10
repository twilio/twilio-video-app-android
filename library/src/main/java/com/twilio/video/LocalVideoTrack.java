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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A local video track that gets video frames from a specified {@link VideoCapturer}.
 */
public class LocalVideoTrack extends VideoTrack {
    private static final Logger logger = Logger.getLogger(LocalVideoTrack.class);
    private static final double ASPECT_RATIO_TOLERANCE = 0.05;
    private static final String CAPTURER_MUST_HAVE_ONE_SUPPORTED_FORMAT = "A VideoCapturer " +
            "must provide at least one supported VideoFormat";
    static final VideoConstraints DEFAULT_VIDEO_CONSTRAINTS = new VideoConstraints.Builder()
            .maxFps(30)
            .maxVideoDimensions(VideoDimensions.VGA_VIDEO_DIMENSIONS)
            .build();

    private long nativeLocalVideoTrackHandle;
    private final String trackId;
    private final VideoCapturer videoCapturer;
    private final VideoConstraints videoConstraints;
    private final MediaFactory mediaFactory;

    /**
     * Creates a local video track. Local video track invokes
     * {@link VideoCapturer#getSupportedFormats()} to find the closest supported
     * {@link VideoFormat} to 640x480 at 30 frames per second. The closest format is used to apply
     * default {@link VideoConstraints} to the returned {@link LocalVideoTrack}.
     *
     * @param context application context.
     * @param enabled initial state of video track.
     * @param videoCapturer capturer that provides video frames.
     * @return local video track if successfully added or null if video track could not be created.
     */
    public static LocalVideoTrack create(@NonNull Context context,
                                         boolean enabled,
                                         @NonNull VideoCapturer videoCapturer) {
        return create(context, enabled, videoCapturer, null, null);
    }

    /**
     * Creates a local video track. Local video track will only apply {@code videoConstraints}
     * compatible with {@code videoCapturer}. Default constraints described in
     * {@link #create(Context, boolean, VideoCapturer)} will be applied to the returned
     * {@link LocalVideoTrack} for the following conditions:
     * <p>
     * <ol>
     *     <li>Passing {@code null} as {@code videoConstraints}.</li>
     *     <li>{@code videoConstraints} are incompatible with {@code videoCapturer}</li>
     * </ol>
     * <p>
     * Video constraints are incompatible with a capturer if there is not at least one supported
     * {@link VideoFormat} for which all the following conditions true:
     * <ol>
     *     <li>{@link VideoConstraints#minFps} and {@link VideoConstraints#maxFps} are both
     *     less than or equal supported capture format frame rate.</li>
     *     <li>{@link VideoConstraints#minVideoDimensions} width and height are less than or
     *     equal to a supported capture format width and height.</li>
     *     <li>{@link VideoConstraints#maxVideoDimensions} width and height are greater than or
     *     equal to a supported capture format width and height.</li>
     * </ol>
     *
     * @param context application context.
     * @param enabled initial state of video track.
     * @param videoCapturer capturer that provides video frames.
     * @param videoConstraints constraints to be applied on video track.
     * @return local video track if successfully added or null if video track could not be created.
     */
    public static LocalVideoTrack create(@NonNull Context context,
                                         boolean enabled,
                                         @NonNull VideoCapturer videoCapturer,
                                         @Nullable VideoConstraints videoConstraints) {
        return create(context, enabled, videoCapturer, videoConstraints, null);
    }

    /**
     * Creates a local video track. Local video track invokes
     * {@link VideoCapturer#getSupportedFormats()} to find the closest supported
     * {@link VideoFormat} to 640x480 at 30 frames per second. The closest format is used to apply
     * default {@link VideoConstraints} to the returned {@link LocalVideoTrack}.
     *
     * @param context application context.
     * @param enabled initial state of video track.
     * @param videoCapturer capturer that provides video frames.
     * @param name video track name.
     * @return local video track if successfully added or null if video track could not be created.
     */
    public static LocalVideoTrack create(@NonNull Context context,
                                         boolean enabled,
                                         @NonNull VideoCapturer videoCapturer,
                                         @Nullable String name) {
        return create(context, enabled, videoCapturer, null, name);
    }

    /**
     * Creates a local video track. Local video track will only apply {@code videoConstraints}
     * compatible with {@code videoCapturer}. Default constraints described in
     * {@link #create(Context, boolean, VideoCapturer)} will be applied to the returned
     * {@link LocalVideoTrack} for the following conditions:
     * <p>
     * <ol>
     *     <li>Passing {@code null} as {@code videoConstraints}.</li>
     *     <li>{@code videoConstraints} are incompatible with {@code videoCapturer}</li>
     * </ol>
     * <p>
     * Video constraints are incompatible with a capturer if there is not at least one supported
     * {@link VideoFormat} for which all the following conditions true:
     * <ol>
     *     <li>{@link VideoConstraints#minFps} and {@link VideoConstraints#maxFps} are both
     *     less than or equal supported capture format frame rate.</li>
     *     <li>{@link VideoConstraints#minVideoDimensions} width and height are less than or
     *     equal to a supported capture format width and height.</li>
     *     <li>{@link VideoConstraints#maxVideoDimensions} width and height are greater than or
     *     equal to a supported capture format width and height.</li>
     * </ol>
     *
     * @param context application context.
     * @param enabled initial state of video track.
     * @param videoCapturer capturer that provides video frames.
     * @param name video track name.
     * @return local video track if successfully added or null if video track could not be created.
     */
    public static LocalVideoTrack create(@NonNull Context context,
                                         boolean enabled,
                                         @NonNull VideoCapturer videoCapturer,
                                         @Nullable VideoConstraints videoConstraints,
                                         @Nullable String name) {
        Preconditions.checkNotNull(context, "Context must not be null");
        Preconditions.checkNotNull(videoCapturer, "VideoCapturer must not be null");
        Preconditions.checkState(videoCapturer.getSupportedFormats() != null &&
                        !videoCapturer.getSupportedFormats().isEmpty(),
                CAPTURER_MUST_HAVE_ONE_SUPPORTED_FORMAT);

        // Use temporary media factory owner to create local video track
        Object temporaryMediaFactoryOwner = new Object();
        MediaFactory mediaFactory = MediaFactory.instance(temporaryMediaFactoryOwner, context);
        LocalVideoTrack localVideoTrack = mediaFactory
                .createVideoTrack(context,
                        enabled,
                        videoCapturer,
                        resolveConstraints(videoCapturer, videoConstraints),
                        name);

        if (localVideoTrack == null) {
            logger.e("Failed to create local video track");
        }

        // Local video track will obtain media factory instance in constructor so release ownership
        mediaFactory.release(temporaryMediaFactoryOwner);

        return localVideoTrack;
    }

    /**
     * Retrieves the {@link VideoCapturer} associated with this video track.
     */
    public VideoCapturer getVideoCapturer() {
        return videoCapturer;
    }

    /**
     * Retrieves the video constraints associated with this track.
     *
     * <p>If you do not provide any video constraints, the default video constraints are set to a
     * maximum video dimension size of 640x480 at a frame rate of 30 frames per second.</p>
     */
    public VideoConstraints getVideoConstraints() {
        return videoConstraints;
    }

    @Override
    public synchronized void addRenderer(@NonNull VideoRenderer videoRenderer) {
        Preconditions.checkState(!isReleased(), "Cannot add renderer to video track that has " +
                "been released");
        super.addRenderer(videoRenderer);
    }

    @Override
    public synchronized void removeRenderer(@NonNull VideoRenderer videoRenderer) {
        Preconditions.checkState(!isReleased(), "Cannot remove renderer from video track that has " +
                "been released");
        super.removeRenderer(videoRenderer);
    }

    /**
     * Check if the local video track is enabled.
     *
     * When the value is false, blank video frames are sent. When the value is true, frames from the
     * video capturer are provided.
     *
     * @return true if the local video is enabled.
     */
    @Override
    public synchronized boolean isEnabled() {
        if (!isReleased()) {
            return nativeIsEnabled(nativeLocalVideoTrackHandle);
        } else {
            logger.e("Local video track is not enabled because it has been released");
            return false;
        }
    }

    /**
     * Returns the local video track name. {@link #trackId} is returned if no name was specified.
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * This video track id.
     *
     * @return track id.
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * Sets the state of the local video track. The results of this operation are signaled to other
     * Participants in the same Room. When a video track is disabled, blank frames are sent in place
     * of video frames from a video capturer.
     *
     * @param enabled the desired state of the local video track.
     */
    public synchronized void enable(boolean enabled) {
        if (!isReleased()) {
            nativeEnable(nativeLocalVideoTrackHandle, enabled);
        } else {
            logger.e("Cannot enable a local video track that has been removed");
        }
    }

    /**
     * Releases native memory owned by video track.
     */
    public synchronized void release() {
        if (!isReleased()) {
            super.release();
            nativeRelease(nativeLocalVideoTrackHandle);
            nativeLocalVideoTrackHandle = 0;
            mediaFactory.release(this);
        }
    }

    LocalVideoTrack(long nativeLocalVideoTrackHandle,
                    boolean enabled,
                    VideoCapturer videoCapturer,
                    VideoConstraints videoConstraints,
                    org.webrtc.VideoTrack webrtcVideoTrack,
                    String name,
                    Context context) {
        super(webrtcVideoTrack, enabled, name);
        this.trackId = webrtcVideoTrack.id();
        this.nativeLocalVideoTrackHandle = nativeLocalVideoTrackHandle;
        this.videoCapturer = videoCapturer;
        this.videoConstraints = videoConstraints;
        this.mediaFactory = MediaFactory.instance(this, context);
    }

    /*
     * Safely resolves a set of VideoConstraints based on VideoCapturer supported formats
     */
    private static VideoConstraints resolveConstraints(VideoCapturer videoCapturer,
                                                       VideoConstraints videoConstraints) {
        if (videoConstraints == null || !constraintsCompatible(videoCapturer, videoConstraints)) {
            logger.e("Applying VideoConstraints closest to 640x480@30 FPS.");
            return getClosestCompatibleVideoConstraints(videoCapturer, DEFAULT_VIDEO_CONSTRAINTS);
        } else {
            return videoConstraints;
        }
    }

    /*
     * Returns true if at least one VideoFormat is compatible with the provided VideoConstraints.
     * Based on a similar algorithm in webrtc/api/videocapturertracksource.cc
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    static boolean constraintsCompatible(VideoCapturer videoCapturer,
                                         VideoConstraints videoConstraints) {
        for (VideoFormat videoFormat : videoCapturer.getSupportedFormats()) {
            VideoDimensions minVideoDimensions = videoConstraints.getMinVideoDimensions();
            VideoDimensions maxVideoDimensions = videoConstraints.getMaxVideoDimensions();
            AspectRatio aspectRatio = videoConstraints.getAspectRatio();
            int minFps = videoConstraints.getMinFps();
            int maxFps = videoConstraints.getMaxFps();
            boolean formatCompatible = minVideoDimensions.width <= videoFormat.dimensions.width &&
                    minVideoDimensions.height <= videoFormat.dimensions.height &&
                    minFps <= videoFormat.framerate;

            // Validate that max dimensions are compatible
            if (maxVideoDimensions.width > 0) {
                formatCompatible &= maxVideoDimensions.width >= videoFormat.dimensions.width;
            }
            if (maxVideoDimensions.height > 0) {
                formatCompatible &= maxVideoDimensions.height >= videoFormat.dimensions.height;
            }

            // Validate that max FPS is compatible
            if (maxFps > 0) {
                formatCompatible &= maxFps <= videoFormat.framerate;
            }

            // Check if format resolution is within aspect ratio tolerance
            if (aspectRatio.numerator > 0 && aspectRatio.denominator > 0) {
                double targetRatio = (double) aspectRatio.numerator /
                        (double) aspectRatio.denominator;
                double ratio = (double) videoFormat.dimensions.width /
                        (double) videoFormat.dimensions.height;

                formatCompatible &= Math.abs(ratio - targetRatio) < ASPECT_RATIO_TOLERANCE;
            }

            if (formatCompatible) {
                logger.i("VideoConstraints are compatible with VideoCapturer");
                return true;
            }
        }

        logger.e("VideoConstraints are not compatible with VideoCapturer");
        return false;
    }

    /*
     * Finds the closest compatible VideoConstraints for a given set of constraints to a
     * VideoCapturer.
     */
    private static VideoConstraints getClosestCompatibleVideoConstraints(VideoCapturer videoCapturer,
                                                                         final VideoConstraints videoConstraints) {
        // Find closest supported dimensions
        List<VideoFormat> supportedFormats = videoCapturer.getSupportedFormats();
        VideoDimensions closestSupportedVideoDimensions =
                Collections.min(supportedFormats,
                        new LocalVideoTrack.ClosestComparator<VideoFormat>() {
                            @Override int diff(VideoFormat videoFormat) {
                                return Math.abs(videoConstraints.getMaxVideoDimensions().width -
                                        videoFormat.dimensions.width) +
                                        Math.abs(videoConstraints.getMaxVideoDimensions().height -
                                                videoFormat.dimensions.height);
                            }
                        }).dimensions;

        // Find closest supported framerate with matching dimensions
        List<Integer> supportedFramerates = new ArrayList<>();
        for (VideoFormat videoFormat : supportedFormats) {
            if (videoFormat.dimensions.equals(closestSupportedVideoDimensions)) {
                supportedFramerates.add(videoFormat.framerate);
            }
        }
        int closestSupportedFramerate =
                Collections.min(supportedFramerates,
                        new LocalVideoTrack.ClosestComparator<Integer>() {
                            @Override int diff(Integer framerate) {
                                return Math.abs(videoConstraints.getMaxFps() - framerate);
                            }
                        });

        return new VideoConstraints.Builder()
                .maxFps(closestSupportedFramerate)
                .maxVideoDimensions(closestSupportedVideoDimensions)
                .build();
    }

    boolean isReleased() {
        return nativeLocalVideoTrackHandle == 0;
    }

    /*
     * Called by LocalParticipant at JNI level
     */
    @SuppressWarnings("unused")
    synchronized long getNativeHandle() {
        return nativeLocalVideoTrackHandle;
    }

    // Helper class for finding the closest supported contraints
    private static abstract class ClosestComparator<T> implements Comparator<T> {
        // Difference between supported and requested parameter.
        abstract int diff(T supportedParameter);

        @Override
        public int compare(T t1, T t2) {
            return diff(t1) - diff(t2);
        }
    }

    private native boolean nativeIsEnabled(long nativeLocalVideoTrackHandle);
    private native void nativeEnable(long nativeLocalVideoTrackHandle, boolean enable);
    private native void nativeRelease(long nativeLocalVideoTrackHandle);
}
