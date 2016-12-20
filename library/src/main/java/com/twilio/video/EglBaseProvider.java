package com.twilio.video;

import android.support.annotation.VisibleForTesting;

import org.webrtc.EglBase;

import java.util.HashSet;
import java.util.Set;

class EglBaseProvider {
    private static final String RELEASE_MESSAGE_TEMPLATE = "EglBaseProvider released %s " +
            "unavailable";
    private volatile static EglBaseProvider instance;
    private volatile static Set<Object> eglBaseProviderOwners = new HashSet<>();

    private EglBase rootEglBase;
    private EglBase localEglBase;
    private EglBase remoteEglBase;

    static EglBaseProvider instance(Object owner) {
        synchronized (EglBaseProvider.class) {
            if (instance == null) {
                instance = new EglBaseProvider();
            }
            eglBaseProviderOwners.add(owner);

            return instance;
        }
    }

    EglBase getRootEglBase() {
        synchronized (EglBaseProvider.class) {
            checkReleased("getRootEglBase");
            return instance.rootEglBase;
        }
    }

    EglBase getLocalEglBase() {
        synchronized (EglBaseProvider.class) {
            checkReleased("getLocalEglBase");
            return instance.localEglBase;
        }
    }

    EglBase getRemoteEglBase() {
        synchronized (EglBaseProvider.class) {
            checkReleased("getRemoteEglBase");
            return instance.remoteEglBase;
        }
    }

    void release(Object owner) {
        synchronized (EglBaseProvider.class) {
            eglBaseProviderOwners.remove(owner);
            if (instance != null && eglBaseProviderOwners.isEmpty()) {
                instance.remoteEglBase.release();
                instance.remoteEglBase = null;
                instance.localEglBase.release();
                instance.localEglBase = null;
                instance.rootEglBase.release();
                instance.rootEglBase = null;
                instance = null;
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    static void waitForNoOwners() {
        while (true) {
            synchronized (EglBaseProvider.class) {
                if (eglBaseProviderOwners.isEmpty()) {
                    break;
                }
            }
        }
    }

    private EglBaseProvider() {
        rootEglBase = EglBase.create();
        localEglBase = EglBase.create(rootEglBase.getEglBaseContext());
        remoteEglBase = EglBase.create(rootEglBase.getEglBaseContext());
    }

    private void checkReleased(String methodName) {
        if (instance == null) {
            String releaseErrorMessage = String.format(RELEASE_MESSAGE_TEMPLATE, methodName);

            throw new IllegalStateException(releaseErrorMessage);
        }
    }
}
