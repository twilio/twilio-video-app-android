/*
 * Copyright (C) 2019 Twilio, Inc.
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

package com.twilio.video.app.data.api;

import static com.twilio.video.app.util.BuildConfigUtilsKt.isReleaseBuildType;

import android.content.SharedPreferences;
import com.twilio.video.app.ApplicationScope;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
public class VideoAppServiceModule {
    private static final String VIDEO_APP_SERVICE_DEV_URL = "https://app.dev.video.bytwilio.com";
    private static final String VIDEO_APP_SERVICE_STAGE_URL =
            "https://app.stage.video.bytwilio.com";
    private static final String VIDEO_APP_SERVICE_PROD_URL = "https://app.video.bytwilio.com";

    @Provides
    @ApplicationScope
    @Named("VideoAppService")
    OkHttpClient providesOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (!isReleaseBuildType()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        return builder.readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new FirebaseAuthInterceptor())
                .build();
    }

    @Provides
    @ApplicationScope
    @Named("VideoAppServiceDev")
    VideoAppService providesVideoAppServiceDev(
            @Named("VideoAppService") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_DEV_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VideoAppService.class);
    }

    @Provides
    @ApplicationScope
    @Named("VideoAppServiceStage")
    VideoAppService providesVideoAppServiceStage(
            @Named("VideoAppService") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_STAGE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VideoAppService.class);
    }

    @Provides
    @ApplicationScope
    @Named("VideoAppServiceProd")
    VideoAppService providesVideoAppServiceProd(
            @Named("VideoAppService") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_PROD_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VideoAppService.class);
    }

    @Provides
    @ApplicationScope
    VideoAppServiceDelegate providesVideoAppServiceDelegate(
            SharedPreferences sharedPreferences,
            @Named("VideoAppServiceDev") VideoAppService videoAppServiceDev,
            @Named("VideoAppServiceStage") VideoAppService videoAppServiceStage,
            @Named("VideoAppServiceProd") VideoAppService videoAppServiceProd) {

        return new VideoAppServiceDelegate(
                sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd);
    }

    @Provides
    @ApplicationScope
    TokenService providesTokenService(final VideoAppServiceDelegate videoAppServiceDelegate) {
        return videoAppServiceDelegate;
    }
}
