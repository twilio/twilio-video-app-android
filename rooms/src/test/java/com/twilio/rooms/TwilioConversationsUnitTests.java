package com.twilio.rooms;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TwilioConversationsUnitTests {
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    @Mock Context mockContext;

    @Test
    public void initialize_shouldProceedWithAllPermissionGranted() {
        // Emulate granting all permissions
        for (String permission : REQUIRED_PERMISSIONS) {
            when(mockContext.checkCallingOrSelfPermission(permission))
                    .thenReturn(PackageManager.PERMISSION_GRANTED);
        }

        try {
            // We expect a runtime exception but want to validate the message somewhat
            TwilioConversationsClient.initialize(mockContext);
        } catch (RuntimeException e) {
            // Kinda wonky but ensures that we made it through permission check
            verify(mockContext, atLeastOnce()).getApplicationContext();
        }
    }
}
