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

package com.twilio.video;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.twilio.video.base.BaseVideoTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Camera2CapturerTest extends BaseVideoTest {

    /*
     * Validates that isSupported can be invoked on all API levels without resulting in a runtime
     * exception. See https://code.google.com/p/android/issues/detail?id=209129.
     */
    @Test
    public void shouldAllowCompatibilityCheck() {
        Camera2Capturer.isSupported(InstrumentationRegistry.getContext());
    }
}
