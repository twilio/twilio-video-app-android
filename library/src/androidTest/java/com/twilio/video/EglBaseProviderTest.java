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

package com.twilio.video;

import static junit.framework.Assert.assertNotNull;

import android.support.test.runner.AndroidJUnit4;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.testcategories.MediaTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediaTest
@RunWith(AndroidJUnit4.class)
public class EglBaseProviderTest extends BaseVideoTest {
    private static final int NUM_EGL_PROVIDERS = 10;
    private EglBaseProvider eglBaseProvider;

    @BeforeClass
    public static void suiteSetup() {
        // Ensure that there are no lingering owners from a previous test suite
        EglBaseProvider.waitForNoOwners();
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
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
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        releaseEglBaseProviders(owners, eglBaseProviders);

        // Release the test suite instance
        eglBaseProvider.release(this);

        // Now an exception should be raised when trying to get root egl base
        eglBaseProvider.getRootEglBase();
    }

    @Test(expected = IllegalStateException.class)
    public void getLocalEglBase_shouldFailAfterAllEglBaseProvidersHaveBeenReleased() {
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        releaseEglBaseProviders(owners, eglBaseProviders);

        // Release the test suite instance
        eglBaseProvider.release(this);

        // Now an exception should be raised when trying to get local egl base
        eglBaseProvider.getLocalEglBase();
    }

    @Test(expected = IllegalStateException.class)
    public void getRemoteEglBase_shouldFailAfterAllEglBaseProvidersHaveBeenReleased() {
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        releaseEglBaseProviders(owners, eglBaseProviders);

        // Release the test suite instance
        eglBaseProvider.release(this);

        // Now an exception should be raised when trying to get remote egl base
        eglBaseProvider.getRemoteEglBase();
    }

    @Test
    public void getRootEglBase_shouldSucceedIfStillOwnedAfterRelease() {
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        for (int i = 0; i < NUM_EGL_PROVIDERS; i++) {
            eglBaseProviders[i].release(owners[i]);

            // Other egl provider owners should still be able to get root egl base
            for (int j = i + 1; j < NUM_EGL_PROVIDERS; j++) {
                assertNotNull(eglBaseProviders[j].getRootEglBase());
            }
        }
    }

    @Test
    public void getLocalEglBase_shouldSucceedIfStillOwnedAfterRelease() {
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        for (int i = 0; i < NUM_EGL_PROVIDERS; i++) {
            eglBaseProviders[i].release(owners[i]);

            // Other egl provider owners should still be able to get local egl base
            for (int j = i + 1; j < NUM_EGL_PROVIDERS; j++) {
                assertNotNull(eglBaseProviders[j].getLocalEglBase());
            }
        }
    }

    @Test
    public void getRemoteEglBase_shouldSucceedIfStillOwnedAfterRelease() {
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        for (int i = 0; i < NUM_EGL_PROVIDERS; i++) {
            eglBaseProviders[i].release(owners[i]);

            // Other egl provider owners should still be able to get remote egl base
            for (int j = i + 1; j < NUM_EGL_PROVIDERS; j++) {
                assertNotNull(eglBaseProviders[j].getRemoteEglBase());
            }
        }
    }

    @Test
    public void getRootEglBase_shouldSucceedIfOneOwnerReleasesMultipleTimes() {
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        for (int i = 0; i < NUM_EGL_PROVIDERS; i++) {
            // Purposefully release egl base provider more than once
            for (int j = 0; j < NUM_EGL_PROVIDERS + 1; j++) {
                eglBaseProviders[i].release(owners[i]);
            }

            // Other egl provider owners should still be able to get root egl base
            for (int j = i + 1; j < NUM_EGL_PROVIDERS; j++) {
                assertNotNull(eglBaseProviders[j].getRootEglBase());
            }
        }
    }

    @Test
    public void getLocalEglBase_shouldSucceedIfOneOwnerReleasesMultipleTimes() {
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        for (int i = 0; i < NUM_EGL_PROVIDERS; i++) {
            // Purposefully release egl base provider more than once
            for (int j = 0; j < NUM_EGL_PROVIDERS + 1; j++) {
                eglBaseProviders[i].release(owners[i]);
            }

            // Other egl provider owners should still be able to get local egl base
            for (int j = i + 1; j < NUM_EGL_PROVIDERS; j++) {
                assertNotNull(eglBaseProviders[j].getLocalEglBase());
            }
        }
    }

    @Test
    public void getRemoteEglBase_shouldSucceedIfOneOwnerReleasesMultipleTimes() {
        Object[] owners = getEglBaseProviderOwners(NUM_EGL_PROVIDERS);
        EglBaseProvider[] eglBaseProviders = getEglBaseProviders(owners);

        // Release EGL base providers
        for (int i = 0; i < NUM_EGL_PROVIDERS; i++) {
            // Purposefully release egl base provider more than once
            for (int j = 0; j < NUM_EGL_PROVIDERS + 1; j++) {
                eglBaseProviders[i].release(owners[i]);
            }

            // Other egl provider owners should still be able to get remote egl base
            for (int j = i + 1; j < NUM_EGL_PROVIDERS; j++) {
                assertNotNull(eglBaseProviders[j].getRemoteEglBase());
            }
        }
    }

    private Object[] getEglBaseProviderOwners(int numEglBaseProviderOwners) {
        Object[] owners = new Object[numEglBaseProviderOwners];

        for (int i = 0; i < numEglBaseProviderOwners; i++) {
            owners[i] = new Object();
        }

        return owners;
    }

    private EglBaseProvider[] getEglBaseProviders(Object[] eglBaseProviderOwners) {
        EglBaseProvider[] eglBaseProviders = new EglBaseProvider[eglBaseProviderOwners.length];

        for (int i = 0; i < eglBaseProviderOwners.length; i++) {
            eglBaseProviders[i] = EglBaseProvider.instance(eglBaseProviderOwners[i]);
        }

        return eglBaseProviders;
    }

    private void releaseEglBaseProviders(
            Object[] eglBaseProviderOwners, EglBaseProvider[] eglBaseProviders) {
        for (int i = 0; i < eglBaseProviderOwners.length; i++) {
            eglBaseProviders[i].release(eglBaseProviderOwners[i]);
        }
    }
}
