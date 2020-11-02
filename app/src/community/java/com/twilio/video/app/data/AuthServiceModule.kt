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
package com.twilio.video.app.data

import android.content.SharedPreferences
import com.twilio.video.app.android.SharedPreferencesWrapper
import com.twilio.video.app.data.api.AuthService
import com.twilio.video.app.data.api.AuthServiceRepository
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.security.SecurePreferences
import com.twilio.video.app.security.SecurityModule
import com.twilio.video.app.util.isReleaseBuildType
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module(includes = [SecurityModule::class])
class AuthServiceModule {
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (!isReleaseBuildType) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }
        return builder
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()
    }

    @Provides
    fun providesAuthService(okHttpClient: OkHttpClient): AuthService {
        return Retrofit.Builder()
                .client(okHttpClient)
                /*
                 * Retrofit requires a base URL when constructing a client. The final URL will be determined by the
                 * user, so insert a placeholder base URL to be replaced at runtime.
                 */
                .baseUrl("https://PLACEHOLDER_URL")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthService::class.java)
    }

    @Provides
    fun providesTokenService(
        authService: AuthService,
        securePreferences: SecurePreferences,
        sharedPreferences: SharedPreferences
    ): TokenService {
        return AuthServiceRepository(authService, securePreferences, SharedPreferencesWrapper(sharedPreferences))
    }
}
