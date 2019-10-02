package com.twilio.video.helper;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Pair;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalDataTrack;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Track;
import com.twilio.video.util.FakeVideoCapturer;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for parameterizing by tracks, map key is the type of track and the value is a pair,
 * first being a boolean denoting whether the track is enabled for the test, second being the track
 * itself. Track can't be initialized until the test is fully setup.
 */
public class TrackContainer {
    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String DATA = "data";
    public Map<String, Pair<Boolean, Track>> trackMap = new HashMap<>();

    public TrackContainer(boolean hasVideo, boolean hasAudio, boolean hasData) {
        trackMap.put(AUDIO, new Pair<>(hasAudio, null));
        trackMap.put(VIDEO, new Pair<>(hasVideo, null));
        trackMap.put(DATA, new Pair<>(hasData, null));
    }

    public TrackContainer(Context context, boolean hasVideo, boolean hasAudio, boolean hasData) {
        LocalAudioTrack audioTrack = (hasAudio) ? LocalAudioTrack.create(context, true) : null;
        trackMap.put(AUDIO, new Pair<>(hasAudio, audioTrack));

        LocalVideoTrack videoTrack =
                (hasVideo) ? LocalVideoTrack.create(context, true, new FakeVideoCapturer()) : null;
        trackMap.put(VIDEO, new Pair<>(hasVideo, videoTrack));

        LocalDataTrack dataTrack = (hasData) ? LocalDataTrack.create(context) : null;
        trackMap.put(DATA, new Pair<>(hasData, dataTrack));
    }

    @Nullable
    public LocalVideoTrack getVideoTrack() {
        return (LocalVideoTrack) trackMap.get(VIDEO).second;
    }

    @Nullable
    public LocalAudioTrack getAudioTrack() {
        return (LocalAudioTrack) trackMap.get(AUDIO).second;
    }

    @Nullable
    public LocalDataTrack getDataTrack() {
        return (LocalDataTrack) trackMap.get(DATA).second;
    }

    public void release() {
        LocalAudioTrack audioTrack = (LocalAudioTrack) trackMap.get(AUDIO).second;
        LocalVideoTrack videoTrack = (LocalVideoTrack) trackMap.get(VIDEO).second;
        LocalDataTrack dataTrack = (LocalDataTrack) trackMap.get(DATA).second;
        if (audioTrack != null) {
            audioTrack.release();
        }
        if (videoTrack != null) {
            videoTrack.release();
        }
        if (dataTrack != null) {
            dataTrack.release();
        }
    }
}
