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

package com.twilio.video.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.TestCase.fail;

public class PermissionUtils {
    private static final int PERMISSIONS_DIALOG_DELAY = 2000;
    private static final int DENY_BUTTON_INDEX = 0;
    private static final int GRANT_BUTTON_INDEX = 1;
    private static final String SCREEN_CAPTURE_DO_NOT_SHOW_AGAIN = "Don't show again";

    public static void allowPermissions(PermissionRequester permissionRequester)  {
        List<String> neededPermissions = permissionRequester.getNeededPermssions();

        sleep(PERMISSIONS_DIALOG_DELAY);

        for (String permission : neededPermissions) {
            clickPermissionAction(true, permission);
        }
    }

    @TargetApi(21)
    public static void allowScreenCapture(boolean showAgain)  {
        sleep(PERMISSIONS_DIALOG_DELAY);

        if (!showAgain) {
            clickAction(SCREEN_CAPTURE_DO_NOT_SHOW_AGAIN);
        }
        clickPermissionAction(true, Manifest.permission.CAPTURE_VIDEO_OUTPUT);
    }

    private static void clickPermissionAction(boolean enable, String permission) {
        try {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject permissionAction = device.findObject(new UiSelector()
                    .clickable(true)
                    .checkable(false)
                    .index(enable ? GRANT_BUTTON_INDEX : DENY_BUTTON_INDEX));
            if (permissionAction.exists()) {
                permissionAction.click();
            }
        } catch (UiObjectNotFoundException e) {
            fail("Failed to" + (enable ? " allow " : " deny ") + permission);
        }
    }

    private static void clickAction(String clickActionText) {
        Instrumentation instrumentation = getInstrumentation();

        UiDevice device = UiDevice.getInstance(instrumentation);
        UiObject clickActionObject =
                device.findObject(new UiSelector().text(clickActionText));

        if (clickActionObject.exists()) {
            try {
                clickActionObject.click();
            } catch (UiObjectNotFoundException e) {
                fail("Failed to click action: " + clickActionText);
            }
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot execute Thread.sleep()");
        }
    }

}
