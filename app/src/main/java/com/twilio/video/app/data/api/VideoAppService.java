package com.twilio.video.app.data.api;

import com.twilio.video.app.data.api.model.VideoConfiguration;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VideoAppService {
    @GET("api/v1/token")
    Single<String> getToken(@Query("environment") String environment,
                            @Query("identity") String identity,
                            @Query("configurationProfileSid") String configurationProfileSid);

    @GET("api/v1/configuration")
    Single<VideoConfiguration> getConfiguration(@Query("environment") String environment);
}
