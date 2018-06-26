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

import static org.mockito.Mockito.when;

import android.content.Context;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalVideoTrackUnitTest {
  @Mock Context mockContext;
  @Mock VideoCapturer videoCapturer;

  @Test(expected = NullPointerException.class)
  public void create_shouldFailWithNullContext() {
    LocalVideoTrack.create(null, true, videoCapturer);
  }

  @Test(expected = NullPointerException.class)
  public void create_shouldFailWithNullCapturer() {
    LocalVideoTrack.create(mockContext, true, null);
  }

  @Test(expected = IllegalStateException.class)
  public void create_shouldFailIfVideoCapturerReturnsNullForSupportedFormats() {
    when(videoCapturer.getSupportedFormats()).thenReturn(null);
    LocalVideoTrack.create(mockContext, true, videoCapturer);
  }

  @Test(expected = IllegalStateException.class)
  public void create_shouldFailIfVideoCapturerProvidesNoSupportedFormats() {
    when(videoCapturer.getSupportedFormats()).thenReturn(Collections.<VideoFormat>emptyList());
    LocalVideoTrack.create(mockContext, true, videoCapturer);
  }
}
