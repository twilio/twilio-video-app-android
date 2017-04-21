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
