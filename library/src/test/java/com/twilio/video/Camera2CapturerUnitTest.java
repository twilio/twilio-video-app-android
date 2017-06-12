package com.twilio.video;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;

import com.twilio.video.util.ReflectionUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@RunWith(MockitoJUnitRunner.class)
public class Camera2CapturerUnitTest {
    private static final String[] cameraIds = new String[]{
        "0", "1"
    };
    @Mock Context context;
    @Mock Camera2Capturer.Listener listener;
    @Mock CameraManager cameraManager;
    @Mock Handler handler;

    @Before
    public void setup() throws Exception {
        when(context.getApplicationContext()).thenReturn(context);
        when(cameraManager.getCameraIdList()).thenReturn(cameraIds);
        when(context.getSystemService(Context.CAMERA_SERVICE)).thenReturn(cameraManager);
        ReflectionUtils.setFinalStaticField(Build.VERSION.class.getField("SDK_INT"),
                Build.VERSION_CODES.LOLLIPOP);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnDevicesLessThanLollipop() throws Exception {
        ReflectionUtils.setFinalStaticField(Build.VERSION.class.getField("SDK_INT"),
                Build.VERSION_CODES.KITKAT);
        new Camera2Capturer(context, "0", listener, handler);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullContext() {
        new Camera2Capturer(null, "1", listener, handler);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullCameraId() {
        new Camera2Capturer(context, null, listener, handler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptyCameraId() {
        new Camera2Capturer(context, "", listener, handler);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullListener() {
        new Camera2Capturer(context, "2", null, handler);
    }

    @Test(expected = IllegalStateException.class)
    public void getSupportedFormats_shouldFailWhenCameraPermissionNotGranted() {
        when(context.checkCallingOrSelfPermission(Manifest.permission.CAMERA))
                .thenReturn(PackageManager.PERMISSION_DENIED);
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, "0", listener, handler);

        camera2Capturer.getSupportedFormats();
    }

    @Test(expected = IllegalStateException.class)
    public void getSupportedFormats_shouldFailWhenCameraIdIsNotSupported() {
        when(context.checkCallingOrSelfPermission(Manifest.permission.CAMERA))
                .thenReturn(PackageManager.PERMISSION_GRANTED);
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, "111111", listener, handler);

        camera2Capturer.getSupportedFormats();
    }

    @Test(expected = IllegalStateException.class)
    public void startCapture_shouldFailWhenCameraPermissionNotGranted() {
        VideoFormat videoFormat = Mockito.mock(VideoFormat.class);
        VideoCapturer.Listener videoCapturerListener = Mockito.mock(VideoCapturer.Listener.class);
        when(context.checkCallingOrSelfPermission(Manifest.permission.CAMERA))
                .thenReturn(PackageManager.PERMISSION_DENIED);
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, "0", listener, handler);

        camera2Capturer.startCapture(videoFormat, videoCapturerListener);
    }

    @Test(expected = IllegalStateException.class)
    public void startCapture_shouldFailWhenCameraIdIsNotSupported() {
        VideoFormat videoFormat = Mockito.mock(VideoFormat.class);
        VideoCapturer.Listener videoCapturerListener = Mockito.mock(VideoCapturer.Listener.class);
        when(context.checkCallingOrSelfPermission(Manifest.permission.CAMERA))
                .thenReturn(PackageManager.PERMISSION_GRANTED);
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, "111111", listener, handler);

        camera2Capturer.startCapture(videoFormat, videoCapturerListener);
    }

    @Test(expected = NullPointerException.class)
    public void switchCamera_shouldFailWithNullCameraId() {
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, "0", listener, handler);

        camera2Capturer.switchCamera(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void switchCamera_shouldFailWithEmptyCameraId() {
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, "0", listener, handler);

        camera2Capturer.switchCamera("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void switchCamera_shouldFailWhenCameraIdIsNotSupported() {
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, "0", listener, handler);

        camera2Capturer.switchCamera("111111");
    }

    @Test(expected = IllegalArgumentException.class)
    public void switchCamera_shouldFailWhenCameraIdMatchesCurrentCameraId() {
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, "0", listener, handler);

        camera2Capturer.switchCamera("0");
    }
}
