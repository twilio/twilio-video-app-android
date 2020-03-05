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

package com.twilio.video.app.data;

import android.content.SharedPreferences;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.twilio.video.app.data.api.AuthService;
import com.twilio.video.app.data.api.AuthServiceRepository;
import com.twilio.video.app.data.api.TokenService;
import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(includes = DataModule.class)
public class AuthServiceModule {

    @Provides
    OkHttpClient providesOkHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    AuthService providesAuthService(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://video-app-dev.twil.io/token/")
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthService.class);
    }

    @Provides
    TokenService providesTokenService(
            AuthService authService, SharedPreferences sharedPreferences) {
        return new AuthServiceRepository(authService, sharedPreferences);
    }
}
