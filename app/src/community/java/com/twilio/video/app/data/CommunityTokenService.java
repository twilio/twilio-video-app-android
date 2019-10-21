package com.twilio.video.app.data;

import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.data.api.TokenService;
import com.twilio.video.app.data.api.model.RoomProperties;

import io.reactivex.Single;

public class CommunityTokenService implements TokenService {
    /*
     * TODO: Topology is ignored so the Room will be the default type setup for the account. Use
     * REST API to create a Room with topology and create token with Room SID.
     */
    @Override
    public Single<String> getToken(final String identity, final RoomProperties roomProperties) {
        return Single.fromCallable(
                () -> BuildConfig.TWILIO_ACCESS_TOKEN);
    }
}
