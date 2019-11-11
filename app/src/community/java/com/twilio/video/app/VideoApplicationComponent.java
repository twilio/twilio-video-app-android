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

package com.twilio.video.app;

import com.twilio.video.app.auth.CommunityAuthModule;
import com.twilio.video.app.data.CommunityDataModule;
import com.twilio.video.app.ui.login.CommunityLoginActivityModule;
import com.twilio.video.app.ui.room.RoomActivityModule;
import com.twilio.video.app.ui.settings.SettingsActivityModule;
import com.twilio.video.app.ui.splash.SplashActivityModule;

import dagger.Component;

@ApplicationScope
@Component(
    modules = {
        ApplicationModule.class,
        CommunityTreeModule.class,
        CommunityDataModule.class,
        CommunityAuthModule.class,
        SplashActivityModule.class,
        CommunityLoginActivityModule.class,
        RoomActivityModule.class,
        SettingsActivityModule.class
    }
)
public interface VideoApplicationComponent extends VideoApplicationGraph {}
