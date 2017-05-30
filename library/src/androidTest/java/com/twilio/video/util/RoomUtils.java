package com.twilio.video.util;

import android.support.test.espresso.core.deps.guava.base.Preconditions;

import com.twilio.video.test.BuildConfig;
import com.twilio.video.twilioapi.VideoApiUtils;
import com.twilio.video.twilioapi.model.VideoRoom;

import java.util.Map;

public class RoomUtils {

    public static VideoRoom createRoom(String name, Topology topology) {
        return createRoom(name, topology, false);
    }

    public static VideoRoom createRoom(String name, Topology topology, boolean enableRecording) {
        Preconditions.checkNotNull(BuildConfig.twilioCredentials,
            CredentialsUtils.TWILIO_VIDEO_JSON_NOT_PROVIDED);
        Map<String, String> credentials = CredentialsUtils.resolveCredentials(
            Environment.fromString(BuildConfig.ENVIRONMENT));
        String type = VideoApiUtils.P2P;
        boolean enableTurn = false;
        if (topology == Topology.P2P) {
            enableTurn = true;
        } else {
            type = VideoApiUtils.GROUP;
        }
        return VideoApiUtils.createRoom(credentials.get(CredentialsUtils.ACCOUNT_SID),
            credentials.get(CredentialsUtils.API_KEY),
            credentials.get(CredentialsUtils.API_KEY_SECRET),
            name,
            type,
            BuildConfig.ENVIRONMENT,
            enableTurn,
            enableRecording);
    }
}
