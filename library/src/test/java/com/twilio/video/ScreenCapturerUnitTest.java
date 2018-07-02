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

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.twilio.video.util.ReflectionUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScreenCapturerUnitTest {
    @Mock Context context;

    @Before
    public void setup() throws Exception {
        ReflectionUtils.setFinalStaticField(Build.VERSION.class.getField("SDK_INT"),
                Build.VERSION_CODES.LOLLIPOP);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnDevicesLessThanLollipop() throws Exception {
        ReflectionUtils.setFinalStaticField(Build.VERSION.class.getField("SDK_INT"),
                Build.VERSION_CODES.KITKAT);
        new ScreenCapturer(context, 2, new Intent(), null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullContext() {
        new ScreenCapturer(null, 2, new Intent(), null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullIntent() {
        new ScreenCapturer(context, 2, null, null);
    }
}
