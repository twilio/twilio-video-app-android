package com.twilio.conversations;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UtilUnitTests {
    @Mock Context mockContext;

    @Test
    public void permissionGranted() {
        String grantedPermission = Manifest.permission.CAMERA;
        String deniedPermission = Manifest.permission.RECORD_AUDIO;

        when(mockContext.checkCallingOrSelfPermission(grantedPermission))
                .thenReturn(PackageManager.PERMISSION_GRANTED);
        when(mockContext.checkCallingOrSelfPermission(deniedPermission))
                .thenReturn(PackageManager.PERMISSION_DENIED);

        assertTrue(Util.permissionGranted(mockContext, grantedPermission));
        assertFalse(Util.permissionGranted(mockContext, deniedPermission));
    }
}
