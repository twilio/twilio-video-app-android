package com.twilio.video;

import android.animation.ObjectAnimator;
import android.content.Context;

import com.getkeepsafe.relinker.ReLinker;

import org.webrtc.EglBase;

class MediaFactory {
    private static final String RELEASE_MESSAGE_TEMPLATE = "MediaFactory released %s unavailable";
    private static volatile boolean libraryIsLoaded = false;
    private static final Logger logger = Logger.getLogger(MediaFactory.class);
    private volatile static MediaFactory instance;
    private volatile static int mediaFactoryRefCount = 0;

    private long nativeMediaFactoryHandle;
    private EglBaseProvider eglBaseProvider;

    static MediaFactory instance(Context context) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (instance == null) {
            synchronized (MediaFactory.class) {
                if (instance == null) {
                    if (!libraryIsLoaded) {
                        ReLinker.loadLibrary(context, "jingle_peerconnection_so");
                        libraryIsLoaded = true;
                    }

                    /*
                     * We need to create a temporary owner of EglBaseProvider so create our native
                     * media factory.
                     */
                    Object temporaryEglOwner = new Object();
                    EglBaseProvider eglBaseProvider = EglBaseProvider.instance(temporaryEglOwner);
                    EglBase localEglBase = eglBaseProvider.getLocalEglBase();
                    EglBase remoteEglBase = eglBaseProvider.getRemoteEglBase();

                    long nativeMediaFactoryHandle = nativeCreate(context,
                            localEglBase.getEglBaseContext(),
                            remoteEglBase.getEglBaseContext());

                    if (nativeMediaFactoryHandle == 0) {
                        logger.e("Failed to instance MediaFactory");
                    } else {
                        instance = new MediaFactory(nativeMediaFactoryHandle);
                    }
                    /*
                     * MediaFactory constructor will retain instance of EglBaseProvider so we can
                     * release our temporary ownership in this method.
                     */
                    eglBaseProvider.release(temporaryEglOwner);
                }
            }
        }

        return instance;
    }

    LocalMedia createLocalMedia(Context context) {
        checkReleased("createLocalMedia");
        long nativeLocalMediaHandle = nativeCreateLocalMedia(nativeMediaFactoryHandle);

        if (nativeLocalMediaHandle == 0) {
            logger.e("Failed to instance LocalMedia");
            return null;
        }
        synchronized (MediaFactory.class) {
            mediaFactoryRefCount++;
        }

        return new LocalMedia(context, this, nativeLocalMediaHandle);
    }

    void release() {
        if (instance != null) {
            synchronized (MediaFactory.class) {
                mediaFactoryRefCount = Math.max(0, --mediaFactoryRefCount);
                if (instance != null && mediaFactoryRefCount == 0) {
                    // Release EGL base provider
                    eglBaseProvider.release(this);
                    eglBaseProvider = null;

                    // Release native media factory
                    nativeRelease(nativeMediaFactoryHandle);
                    nativeMediaFactoryHandle = 0;
                    instance = null;
                }
            }
        }
    }

    long getNativeMediaFactoryHandle() {
        return nativeMediaFactoryHandle;
    }

    private MediaFactory(long nativeMediaFactoryHandle) {
        this.nativeMediaFactoryHandle = nativeMediaFactoryHandle;
        this.eglBaseProvider = EglBaseProvider.instance(this);
    }

    private void checkReleased(String methodName) {
        if (nativeMediaFactoryHandle == 0) {
            String releaseErrorMessage = String.format(RELEASE_MESSAGE_TEMPLATE, methodName);

            throw new IllegalStateException(releaseErrorMessage);
        }
    }

    private static native long nativeCreate(Context context,
                                            EglBase.Context localEglBase,
                                            EglBase.Context remoteEglBase);
    private static native long nativeCreateLocalMedia(long nativeMediaFactoryHandle);
    private native void nativeRelease(long mediaFactoryHandle);
}
