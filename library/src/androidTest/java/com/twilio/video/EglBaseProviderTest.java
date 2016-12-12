package com.twilio.video;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class EglBaseProviderTest {
    private EglBaseProvider eglBaseProvider;

    @Before
    public void setup() {
        eglBaseProvider = EglBaseProvider.instance();
    }

    @After
    public void teardown() {
        eglBaseProvider.release();
    }

    @Test
    public void canGetRootEglBase() {
        assertNotNull(eglBaseProvider.getRootEglBase());
    }

    @Test
    public void canGetLocalEglBase() {
        assertNotNull(eglBaseProvider.getLocalEglBase());
    }

    @Test
    public void canGetRemoteEglBase() {
        assertNotNull(eglBaseProvider.getRemoteEglBase());
    }

    @Test
    public void release_shouldBeIdempotent() {
        eglBaseProvider.release();
        eglBaseProvider.release();
    }

    @Test(expected = IllegalStateException.class)
    public void getRootEglBase_shouldFailAfterAllEglBaseProvidersHaveBeenReleased() {
        int numEglBaseProviders = 10;
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[numEglBaseProviders];

        // Get EGL base providers
        for (int i = 0 ; i < numEglBaseProviders ; i++) {
            eglBaseProviders[i] = EglBaseProvider.instance();
        }

        // Release EGL base providers
        for (int i = 0 ; i < numEglBaseProviders ; i++) {
            eglBaseProviders[i].release();
        }

        // Release the test suite instance
        eglBaseProvider.release();

        // Now an exception should be raised when trying to get root egl base
        eglBaseProvider.getRootEglBase();
    }

    @Test(expected = IllegalStateException.class)
    public void getLocalEglBase_shouldFailAfterAllEglBaseProvidersHaveBeenReleased() {
        int numEglBaseProviders = 10;
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[numEglBaseProviders];

        // Get EGL base providers
        for (int i = 0 ; i < numEglBaseProviders ; i++) {
            eglBaseProviders[i] = EglBaseProvider.instance();
        }

        // Release EGL base providers
        for (int i = 0 ; i < numEglBaseProviders ; i++) {
            eglBaseProviders[i].release();
        }

        // Release the test suite instance
        eglBaseProvider.release();

        // Now an exception should be raised when trying to get root egl base
        eglBaseProvider.getLocalEglBase();
    }

    @Test(expected = IllegalStateException.class)
    public void getRemoteEglBase_shouldFailAfterAllEglBaseProvidersHaveBeenReleased() {
        int numEglBaseProviders = 10;
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[numEglBaseProviders];

        // Get EGL base providers
        for (int i = 0 ; i < numEglBaseProviders ; i++) {
            eglBaseProviders[i] = EglBaseProvider.instance();
        }

        // Release EGL base providers
        for (int i = 0 ; i < numEglBaseProviders ; i++) {
            eglBaseProviders[i].release();
        }

        // Release the test suite instance
        eglBaseProvider.release();

        // Now an exception should be raised when trying to get root egl base
        eglBaseProvider.getRemoteEglBase();
    }
}
