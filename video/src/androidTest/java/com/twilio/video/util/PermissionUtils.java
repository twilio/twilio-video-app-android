package com.twilio.video.util;

import android.app.Instrumentation;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import java.util.List;

import static junit.framework.TestCase.fail;

public class PermissionUtils {
    private static final String ALLOW_PERMISSION = "Allow";

    public static void allowPermissions(Instrumentation instrumentation,
                                        PermissionRequester permissionRequester)  {
        List<String> neededPermissions = permissionRequester.getNeededPermssions();

        for (String permission : neededPermissions) {
            UiDevice device = UiDevice.getInstance(instrumentation);
            UiObject allowPermissions = device.findObject(new UiSelector().text(ALLOW_PERMISSION));

            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    fail("Failed to allow permission: " + permission);
                }
            }
        }
    }
}
