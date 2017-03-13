package com.twilio.video;

import android.content.Context;
import android.support.annotation.Nullable;

import org.webrtc.EglBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.Manifest.permission.RECORD_AUDIO;

/**
 * LocalMedia provides local audio and video track management.
 *
 * <p>LocalMedia can be shared to the participants of a {@link Room} when provided via
 * {@link ConnectOptions}. All track operations will be published to participants after
 * connected. The lifecycle of local media is independent of {@link Room}. The same media can
 * be shared in zero, one, or many rooms.</p>
 */
public class LocalMedia {
    private static final double ASPECT_RATIO_TOLERANCE = 0.05;
    private static final String RELEASE_MESSAGE_TEMPLATE = "LocalMedia released %s unavailable";
    private static final Logger logger = Logger.getLogger(LocalMedia.class);
    static final VideoConstraints defaultVideoConstraints = new VideoConstraints.Builder()
            .maxFps(30)
            .maxVideoDimensions(VideoDimensions.VGA_VIDEO_DIMENSIONS)
            .build();

    private final Context context;
    private final MediaFactory mediaFactory;
    private long nativeLocalMediaHandle;
    private EglBaseProvider eglBaseProvider;
    private final List<LocalAudioTrack> localAudioTracks = new ArrayList<>();
    private final List<LocalVideoTrack> localVideoTracks = new ArrayList<>();

    /**
     * Creates a new local media.
     *
     * @param context application context
     * @return a new local media instance
     */
    public static LocalMedia create(Context context) {
        return MediaFactory.instance(context).createLocalMedia(context);
    }

    LocalMedia(Context context, MediaFactory mediaFactory, long nativeLocalMediaHandle) {
        this.context = context;
        this.mediaFactory = mediaFactory;
        this.nativeLocalMediaHandle = nativeLocalMediaHandle;
        this.eglBaseProvider = EglBaseProvider.instance(this);
    }

    /**
     * Returns a list of all currently added audio tracks.
     */
    public List<LocalAudioTrack> getAudioTracks() {
        checkReleased("getAudioTracks");
        return localAudioTracks;
    }

    /**
     * Returns a list of all currently added video tracks.
     */
    public List<LocalVideoTrack> getVideoTracks() {
        checkReleased("getVideoTracks");
        return localVideoTracks;
    }

    /**
     * Adds audio track to local media. Note that the RECORD_AUDIO permission must be granted
     * in order for this operation to succeed. If RECORD_AUDIO is not granted null is returned.
     *
     * @param enabled initial state of audio track.
     * @return local audio track if successfully added or null if audio track could not be added.
     */
    public LocalAudioTrack addAudioTrack(boolean enabled) {
        return addAudioTrack(enabled, null);
    }

    /**
     * Adds audio track to local media. Note that the RECORD_AUDIO permission must be granted
     * in order for this operation to succeed. If RECORD_AUDIO is not granted null is returned.
     *
     * @param enabled initial state of audio track.
     * @param audioOptions audio options to be applied to the track.
     * @return local audio track if successfully added or null if audio track could not be added.
     */
    public LocalAudioTrack addAudioTrack(boolean enabled, AudioOptions audioOptions) {
        checkReleased("addAudioTrack");
        LocalAudioTrack localAudioTrack = null;

        if (Util.permissionGranted(context, RECORD_AUDIO)) {
            localAudioTrack = nativeAddAudioTrack(nativeLocalMediaHandle, enabled, audioOptions);

            if (localAudioTrack != null) {
                localAudioTracks.add(localAudioTrack);
                return localAudioTrack;
            } else {
                logger.e("Failed to create local audio track");
            }
        } else {
            logger.e("RECORD_AUDIO permission must be granted to add audio track");
        }

        return localAudioTrack;
    }

    /**
     * Removes audio track from local media.
     *
     * @param localAudioTrack local audio track to be removed.
     * @return true if the removal succeeded or false if the audio track could not be removed.
     */
    public boolean removeAudioTrack(LocalAudioTrack localAudioTrack) {
        checkReleased("removeAudioTrack");
        boolean result = false;

        if (localAudioTrack != null && localAudioTracks.contains(localAudioTrack)) {
            localAudioTrack.release();
            result = nativeRemoveAudioTrack(nativeLocalMediaHandle, localAudioTrack.getTrackId());

            if (!result) {
                logger.e("Failed to remove audio track");
            } else {
                localAudioTracks.remove(localAudioTrack);
            }
        }

        return result;
    }

    /**
     * Adds local video track to local media. Local media invokes
     * {@link VideoCapturer#getSupportedFormats()} to find the closest supported
     * {@link VideoFormat} to 640x480 at 30 frames per second. The closest format is used to apply
     * default {@link VideoConstraints} to the returned {@link LocalVideoTrack}.
     *
     * @param enabled initial state of video track.
     * @param videoCapturer capturer that provides video frames.
     * @return local video track if successfully added or null if video track could not be added.
     */
    public LocalVideoTrack addVideoTrack(boolean enabled, VideoCapturer videoCapturer) {
        return addVideoTrack(enabled, videoCapturer, null);
    }

    /**
     * Adds local video track to local media. Local media will only apply {@code videoConstraints}
     * compatible with {@code videoCapturer}. Default constraints described in
     * {@link #addVideoTrack(boolean, VideoCapturer)} will be applied to the returned
     * {@link LocalVideoTrack} for the following conditions:
     * <p>
     * <ol>
     *     <li>Passing {@code null} as {@code videoConstraints}.</li>
     *     <li>{@code videoConstraints} are incompatible with {@code videoCapturer}</li>
     * </ol>
     *
     * @param enabled initial state of video track.
     * @param videoCapturer capturer that provides video frames.
     * @param videoConstraints constraints to be applied on video track.
     * @return local video track if successfully added or null if video track could not be added.
     */
    public LocalVideoTrack addVideoTrack(boolean enabled,
                                         VideoCapturer videoCapturer,
                                         @Nullable VideoConstraints videoConstraints) {
        checkReleased("addVideoTrack");
        checkSupportedFormats(videoCapturer);
        LocalVideoTrack localVideoTrack = nativeAddVideoTrack(nativeLocalMediaHandle,
                enabled,
                videoCapturer,
                resolveConstraints(videoCapturer, videoConstraints),
                eglBaseProvider.getLocalEglBase().getEglBaseContext());

        if (localVideoTrack != null) {
            localVideoTracks.add(localVideoTrack);
            return localVideoTrack;
        } else {
            logger.e("Failed to create local video track");
        }

        return localVideoTrack;
    }

    /**
     * Removes video track from local media.
     *
     * @param localVideoTrack local video track to be removed.
     * @return true if the removal succeeded or false if the video track could not be removed.
     */
    public boolean removeVideoTrack(LocalVideoTrack localVideoTrack) {
        checkReleased("removeVideoTrack");
        boolean result = false;

        if (localVideoTrack != null && localVideoTracks.contains(localVideoTrack)) {
            localVideoTrack.release();
            result = nativeRemoveVideoTrack(nativeLocalMediaHandle, localVideoTrack.getTrackId());

            if (!result) {
                logger.e("Failed to remove video track");
            } else {
                localVideoTracks.remove(localVideoTrack);
            }
        }

        return result;
    }

    /**
     * Releases local media. This method must be called when local media is no longer needed. All
     * audio and video tracks will be removed. Local media should not be used after calling this
     * method.
     */
    public void release() {
        if (nativeLocalMediaHandle != 0) {
            while (!localAudioTracks.isEmpty()) {
                removeAudioTrack(localAudioTracks.get(0));
            }
            while (!localVideoTracks.isEmpty()) {
                removeVideoTrack(localVideoTracks.get(0));
            }
            eglBaseProvider.release(this);
            eglBaseProvider = null;
            nativeRelease(nativeLocalMediaHandle);
            nativeLocalMediaHandle = 0;

            mediaFactory.release();
        }
    }

    /*
     * Safely resolves a set of VideoConstraints based on VideoCapturer supported formats
     */
    private VideoConstraints resolveConstraints(VideoCapturer videoCapturer,
                                                VideoConstraints videoConstraints) {
        if (videoConstraints == null || !constraintsCompatible(videoCapturer, videoConstraints)) {
            logger.e("Applying VideoConstraints closest to 640x480@30 FPS.");
            return getClosestCompatibleVideoConstraints(videoCapturer, defaultVideoConstraints);
        } else {
            return videoConstraints;
        }
    }

    /*
     * Returns true if at least one VideoFormat is compatible with the provided VideoConstraints.
     * Based on a similar algorithm in webrtc/api/videocapturertracksource.cc
     */
    private boolean constraintsCompatible(VideoCapturer videoCapturer,
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
                formatCompatible &= maxFps >= videoFormat.framerate;
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
    private VideoConstraints getClosestCompatibleVideoConstraints(VideoCapturer videoCapturer,
                                                                  final VideoConstraints videoConstraints) {
        // Find closest supported dimensions
        List<VideoFormat> supportedFormats = videoCapturer.getSupportedFormats();
        VideoDimensions closestSupportedVideoDimensions =
                Collections.min(supportedFormats,
                        new ClosestComparator<VideoFormat>() {
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
                        new ClosestComparator<Integer>() {
                            @Override int diff(Integer framerate) {
                                return Math.abs(videoConstraints.getMaxFps() - framerate);
                            }
                        });

        return new VideoConstraints.Builder()
                .maxFps(closestSupportedFramerate)
                .maxVideoDimensions(closestSupportedVideoDimensions)
                .build();
    }

    long getNativeLocalMediaHandle() {
        return nativeLocalMediaHandle;
    }

    private void checkReleased(String methodName) {
        if (nativeLocalMediaHandle == 0) {
            String releaseErrorMessage = String.format(RELEASE_MESSAGE_TEMPLATE, methodName);

            throw new IllegalStateException(releaseErrorMessage);
        }
    }

    private void checkSupportedFormats(VideoCapturer videoCapturer) {
        if (videoCapturer.getSupportedFormats() == null ||
                videoCapturer.getSupportedFormats().isEmpty()) {
            throw new IllegalStateException("A VideoCapturer must provide at least one " +
                    "supported VideoFormat");
        }
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

    private native LocalAudioTrack nativeAddAudioTrack(long nativeLocalMediaHandle,
                                                       boolean enabled,
                                                       AudioOptions audioOptions);
    private native boolean nativeRemoveAudioTrack(long nativeLocalMediaHandle, String trackId);
    private native LocalVideoTrack nativeAddVideoTrack(long nativeLocalMediaHandle,
                                                       boolean enabled,
                                                       VideoCapturer videoCapturer,
                                                       VideoConstraints videoConstraints,
                                                       EglBase.Context rootEglBase);
    private native boolean nativeRemoveVideoTrack(long nativeLocalMediaHandle, String trackId);
    private native void nativeRelease(long nativeLocalMediaHandle);
}