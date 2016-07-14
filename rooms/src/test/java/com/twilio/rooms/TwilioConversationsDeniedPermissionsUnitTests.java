package com.twilio.rooms;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(Parameterized.class)
public class TwilioConversationsDeniedPermissionsUnitTests {
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    @Mock Context mockContext;

    private final Set<String> deniedPermissions;

    public TwilioConversationsDeniedPermissionsUnitTests(Set deniedPermissions) {
        this.deniedPermissions = deniedPermissions;
    }

    @Parameterized.Parameters
    public static List<Object[]> initParams() {
        return Arrays.asList(new Object[][] {
                {Sets.newHashSet(Manifest.permission.INTERNET)},
                {Sets.newHashSet(Manifest.permission.MODIFY_AUDIO_SETTINGS)},
                {Sets.newHashSet(Manifest.permission.ACCESS_NETWORK_STATE)},
                {Sets.newHashSet(Manifest.permission.ACCESS_WIFI_STATE)},
                {Sets.newHashSet(Manifest.permission.INTERNET,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS)},
                {Sets.newHashSet(Manifest.permission.INTERNET,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.ACCESS_WIFI_STATE)},
                {Sets.newHashSet(Manifest.permission.INTERNET,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE)},
        });
    }

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void initialize_shouldFailWithoutRequiredPermissions() {
        // Establish the mock behavior for our context based permissions we want to emulate
        // as denied
        for (String permission : REQUIRED_PERMISSIONS) {
            int permissionResult = deniedPermissions.contains(permission) ?
                    PackageManager.PERMISSION_DENIED :
                    PackageManager.PERMISSION_GRANTED;
            when(mockContext.checkCallingOrSelfPermission(permission))
                    .thenReturn(permissionResult);
        }
        boolean testPassed = false;

        try {
            // We expect a runtime exception but want to validate the message somewhat
            TwilioConversationsClient.initialize(mockContext);
        } catch (RuntimeException e) {
            String exceptionMessage = e.getMessage();

            // Verify we received an error about app permissions
            boolean missingRequiredPermissions = exceptionMessage
                    .contains("Your app is missing the following required permissions:");

            // Verify the permissions denied are listed
            boolean permissionsDeniedListed = true;

            // Verify that permissions granted are NOT listed
            boolean permissionsGrantedNotListed = true;

            for (String permission : REQUIRED_PERMISSIONS) {
                if (deniedPermissions.contains(permission)) {
                    permissionsDeniedListed = permissionsDeniedListed &&
                            exceptionMessage.contains(permission);
                } else {
                    permissionsGrantedNotListed = permissionsGrantedNotListed &&
                            !exceptionMessage.contains(permission);
                }
            }

            // If each of these provisions passes the test passed
            testPassed = missingRequiredPermissions &&
                    permissionsDeniedListed &&
                    permissionsGrantedNotListed;
        } finally {
            assertTrue(testPassed);
        }
    }
}
