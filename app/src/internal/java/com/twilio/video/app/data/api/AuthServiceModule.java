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
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AuthServiceModule {
    private static final String VIDEO_APP_SERVICE_DEV_URL =
            "https://dev-dot-twilio-video-react.appspot.com/";
    private static final String VIDEO_APP_SERVICE_STAGE_URL =
            "https://stage-dot-twilio-video-react.appspot.com/";
    private static final String VIDEO_APP_SERVICE_PROD_URL =
            "https://twilio-video-react.appspot.com/";

    @Provides
    @Singleton
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
    @Singleton
    @Named("InternalTokenApiDev")
    InternalTokenApi providesVideoAppServiceDev(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_DEV_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(InternalTokenApi.class);
    }

    @Provides
    @Singleton
    @Named("InternalTokenApiStage")
    InternalTokenApi providesVideoAppServiceStage(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_STAGE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(InternalTokenApi.class);
    }

    @Provides
    @Singleton
    @Named("InternalTokenApiProd")
    InternalTokenApi providesVideoAppServiceProd(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VIDEO_APP_SERVICE_PROD_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(InternalTokenApi.class);
    }

    @Provides
    InternalTokenService providesInternalTokenService(
            SharedPreferences sharedPreferences,
            @Named("InternalTokenApiDev") InternalTokenApi dev,
            @Named("InternalTokenApiStage") InternalTokenApi stage,
            @Named("InternalTokenApiProd") InternalTokenApi prod) {

        return new InternalTokenService(sharedPreferences, dev, stage, prod);
    }

    @Provides
    TokenService providesTokenService(final InternalTokenService internalTokenService) {
        return internalTokenService;
    }
}
