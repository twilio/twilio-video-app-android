package com.twilio.video;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class EglBaseProviderTest {
    private static final int NUM_EGL_PROVIDERS = 10;
    private EglBaseProvider eglBaseProvider;

    @Before
    public void setup() {
        eglBaseProvider = EglBaseProvider.instance(this);
    }

    @After
    public void teardown() {
        eglBaseProvider.release(this);
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
        eglBaseProvider.release(this);
        eglBaseProvider.release(this);
    }

    @Test(expected = IllegalStateException.class)
    public void getRootEglBase_shouldFailAfterAllEglBaseProvidersHaveBeenReleased() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0; i < NUM_EGL_PROVIDERS; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0; i < NUM_EGL_PROVIDERS; i++) {
            eglBaseProviders[i].release(owners[i]);
        }

        // Release the test suite instance
        eglBaseProvider.release(this);

        // Now an exception should be raised when trying to get local egl base
        eglBaseProvider.getRootEglBase();
    }

    @Test(expected = IllegalStateException.class)
    public void getLocalEglBase_shouldFailAfterAllEglBaseProvidersHaveBeenReleased() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            eglBaseProviders[i].release(owners[i]);
        }

        // Release the test suite instance
        eglBaseProvider.release(this);

        // Now an exception should be raised when trying to get local egl base
        eglBaseProvider.getLocalEglBase();
    }

    @Test(expected = IllegalStateException.class)
    public void getRemoteEglBase_shouldFailAfterAllEglBaseProvidersHaveBeenReleased() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            eglBaseProviders[i].release(owners[i]);
        }

        // Release the test suite instance
        eglBaseProvider.release(this);

        // Now an exception should be raised when trying to get remote egl base
        eglBaseProvider.getRemoteEglBase();
    }

    @Test
    public void getRootEglBase_shouldSucceedIfStillOwnedAfterRelease() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            eglBaseProviders[i].release(owners[i]);

            // Other egl provider owners should still be able to get root egl base
            for (int j = i + 1 ; j < NUM_EGL_PROVIDERS ; j++) {
                assertNotNull(eglBaseProviders[j].getRootEglBase());
            }
        }
    }

    @Test
    public void getLocalEglBase_shouldSucceedIfStillOwnedAfterRelease() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            eglBaseProviders[i].release(owners[i]);

            // Other egl provider owners should still be able to get local egl base
            for (int j = i + 1 ; j < NUM_EGL_PROVIDERS ; j++) {
                assertNotNull(eglBaseProviders[j].getLocalEglBase());
            }
        }
    }

    @Test
    public void getRemoteEglBase_shouldSucceedIfStillOwnedAfterRelease() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            eglBaseProviders[i].release(owners[i]);

            // Other egl provider owners should still be able to get remote egl base
            for (int j = i + 1 ; j < NUM_EGL_PROVIDERS ; j++) {
                assertNotNull(eglBaseProviders[j].getRemoteEglBase());
            }
        }
    }

    @Test
    public void getRootEglBase_shouldSucceedIfOneOnwerReleasesMultipleTimes() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            // Purposefully release egl base provider more than once
            for (int j = 0 ; j < NUM_EGL_PROVIDERS + 1 ; j++) {
                eglBaseProviders[i].release(owners[i]);
            }

            // Other egl provider owners should still be able to get root egl base
            for (int j = i + 1 ; j < NUM_EGL_PROVIDERS ; j++) {
                assertNotNull(eglBaseProviders[j].getRootEglBase());
            }
        }
    }

    @Test
    public void getLocalEglBase_shouldSucceedIfOneOnwerReleasesMultipleTimes() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            // Purposefully release egl base provider more than once
            for (int j = 0 ; j < NUM_EGL_PROVIDERS + 1 ; j++) {
                eglBaseProviders[i].release(owners[i]);
            }

            // Other egl provider owners should still be able to get local egl base
            for (int j = i + 1 ; j < NUM_EGL_PROVIDERS ; j++) {
                assertNotNull(eglBaseProviders[j].getLocalEglBase());
            }
        }
    }

    @Test
    public void getRemoteEglBase_shouldSucceedIfOneOnwerReleasesMultipleTimes() {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[NUM_EGL_PROVIDERS];
        Object[] owners = new Object[NUM_EGL_PROVIDERS];

        // Get EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            owners[i] = new Object();
            eglBaseProviders[i] = EglBaseProvider.instance(owners[i]);
        }

        // Release EGL base providers
        for (int i = 0 ; i < NUM_EGL_PROVIDERS ; i++) {
            // Purposefully release egl base provider more than once
            for (int j = 0 ; j < NUM_EGL_PROVIDERS + 1 ; j++) {
                eglBaseProviders[i].release(owners[i]);
            }

            // Other egl provider owners should still be able to get remote egl base
            for (int j = i + 1 ; j < NUM_EGL_PROVIDERS ; j++) {
                assertNotNull(eglBaseProviders[j].getRemoteEglBase());
            }
        }
    }
}
