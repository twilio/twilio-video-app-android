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

package com.twilio.video.app.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.common.base.Strings;
import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.util.BuildConfigUtils;
import dagger.android.AndroidInjection;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        // TODO Replace with auto updates from AppCenter
//        if (registerForHockeyAppUpdates()) {
//            UpdateManager.register(this, BuildConfig.HOCKEY_APP_ID);
//        }
    }

    @Override
    protected void onDestroy() {
//        if (registerForHockeyAppUpdates()) {
//            UpdateManager.unregister();
//        }
        super.onDestroy();
    }

    private boolean registerForHockeyAppUpdates() {
        return BuildConfigUtils.isInternalRelease()
                && !Strings.isNullOrEmpty(BuildConfig.HOCKEY_APP_ID);
    }
}
