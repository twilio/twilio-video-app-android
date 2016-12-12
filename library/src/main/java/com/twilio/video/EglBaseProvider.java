package com.twilio.video;

import org.webrtc.EglBase;

class EglBaseProvider {
    private static final String RELEASE_MESSAGE_TEMPLATE = "EglBaseProvider released %s " +
            "unavailable";
    private volatile static EglBaseProvider instance;
    private volatile static int eglBaseProviderRefCount = 0;

    private EglBase rootEglBase;
    private EglBase localEglBase;
    private EglBase remoteEglBase;

    static EglBaseProvider instance() {
        if (instance == null) {
            synchronized (EglBaseProvider.class) {
                if (instance == null) {
                    instance = new EglBaseProvider();
                }
                eglBaseProviderRefCount++;
            }
        }

        return instance;
    }

    EglBase getRootEglBase() {
        synchronized (EglBaseProvider.class) {
            checkReleased("getRootEglBase");
            return rootEglBase;
        }
    }

    EglBase getLocalEglBase() {
        synchronized (EglBaseProvider.class) {
            checkReleased("getLocalEglBase");
            return localEglBase;
        }
    }

    EglBase getRemoteEglBase() {
        synchronized (EglBaseProvider.class) {
            checkReleased("getRemoteEglBase");
            return remoteEglBase;
        }
    }

    void release() {
        if (instance != null) {
            synchronized (EglBaseProvider.class) {
                eglBaseProviderRefCount = Math.max(0, --eglBaseProviderRefCount);
                if (instance != null && eglBaseProviderRefCount == 0) {
                    remoteEglBase.release();
                    remoteEglBase = null;
                    localEglBase.release();
                    localEglBase = null;
                    rootEglBase.release();
                    rootEglBase = null;
                    instance = null;
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
