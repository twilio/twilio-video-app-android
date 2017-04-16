package com.twilio.video.app.data.api;

import com.twilio.video.app.data.api.model.Topology;

import io.reactivex.Single;

public interface TokenService {
    Single<String> getToken(String identity, Topology topology);
}
