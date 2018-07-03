/*
 * Copyright (C) 2017 Twilio, Inc.
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

import com.twilio.video.app.ApplicationScope;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import retrofit2.Retrofit;

@Module(includes = VideoAppServiceModule.class)
public class InternalVideoAppServiceModule {
    private static final String BASE_URL =
            "https://us-central1-video-app-79418.cloudfunctions.net/internal/";

    @Provides
    @ApplicationScope
    VideoAppService providesVideoAppService(
            @Named("VideoAppService") Retrofit.Builder retrofitBuilder) {
        Retrofit retrofit = retrofitBuilder.baseUrl(BASE_URL).build();

        return retrofit.create(VideoAppService.class);
    }
}
