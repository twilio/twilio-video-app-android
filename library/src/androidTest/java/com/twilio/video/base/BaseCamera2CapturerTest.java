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

package com.twilio.video.base;

import static org.junit.Assume.assumeTrue;

import android.Manifest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import com.twilio.video.Camera2Capturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.ui.CameraCapturerTestActivity;
import com.twilio.video.util.FrameCountRenderer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

public abstract class BaseCamera2CapturerTest extends BaseVideoTest {
  protected static final int CAMERA2_CAPTURER_DELAY_MS = 3500;

  @Rule
  public GrantPermissionRule cameraPermissionsRule =
      GrantPermissionRule.grant(Manifest.permission.CAMERA);

  @Rule
  public ActivityTestRule<CameraCapturerTestActivity> activityRule =
      new ActivityTestRule<>(CameraCapturerTestActivity.class);

  protected CameraCapturerTestActivity cameraCapturerActivity;
  protected Camera2Capturer camera2Capturer;
  protected LocalVideoTrack localVideoTrack;
  protected FrameCountRenderer frameCountRenderer;

  @Before
  public void setup() throws InterruptedException {
    super.setup();
    assumeTrue(Camera2Capturer.isSupported(activityRule.getActivity()));
    cameraCapturerActivity = activityRule.getActivity();
    frameCountRenderer = new FrameCountRenderer();
  }

  @After
  public void teardown() {
    if (localVideoTrack != null) {
      localVideoTrack.release();
    }
  }
}
