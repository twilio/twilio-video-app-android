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
import android.content.pm.PackageManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static android.Manifest.permission.RECORD_AUDIO;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalAudioTrackUnitTest {
    @Mock Context mockContext;

    @Test(expected = NullPointerException.class)
    public void create_shouldFailWithNullContext() {
        LocalAudioTrack.create(null, true);
    }

    @Test(expected = IllegalStateException.class)
    public void create_shouldFailWhenRecordAudioPermissionDenied() {
        // Simulate RECORD_AUDIO permission denied
        when(mockContext.checkCallingOrSelfPermission(RECORD_AUDIO))
                .thenReturn(PackageManager.PERMISSION_DENIED);

        LocalAudioTrack.create(mockContext, true);
    }
}
