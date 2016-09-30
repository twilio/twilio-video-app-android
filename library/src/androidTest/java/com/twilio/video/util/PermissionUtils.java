package com.twilio.video.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import java.util.List;

import static junit.framework.TestCase.fail;

public class PermissionUtils {
    private static final String SCREEN_CAPTURE_DO_NOT_SHOW_AGAIN = "Don't show again";
    private static final String ALLOW_SCREEN_CAPTURE = "Start now";
    private static final String ALLOW_PERMISSION = "Allow";

    public static void allowPermissions(PermissionRequester permissionRequester)  {
        List<String> neededPermissions = permissionRequester.getNeededPermssions();

        for (String permission : neededPermissions) {
            clickAllowPermission(ALLOW_PERMISSION, permission);
        }
    }

    @TargetApi(21)
    public static void allowScreenCapture(boolean showAgain)  {
        if (!showAgain) {
            clickAllowPermission(SCREEN_CAPTURE_DO_NOT_SHOW_AGAIN, "screen capture " +
                    "do not ask again");
        }
        clickAllowPermission(ALLOW_SCREEN_CAPTURE, Manifest.permission.CAPTURE_VIDEO_OUTPUT);
    }

    private static void clickAllowPermission(String allowButtonText, String permission) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        UiDevice device = UiDevice.getInstance(instrumentation);
        UiObject allowScreenCapture =
                device.findObject(new UiSelector().text(allowButtonText));

        if (allowScreenCapture.exists()) {
            try {
                allowScreenCapture.click();
            } catch (UiObjectNotFoundException e) {
                fail("Failed to allow permission: " + permission);
            }
        }
    }
}
