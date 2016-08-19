package com.twilio.video;


import android.content.Context;

import com.twilio.video.internal.Logger;
import com.twilio.video.internal.ReLinker;

class MediaFactory {
    private static final String RELEASE_MESSAGE_TEMPLATE = "MediaFactory released %s unavailable";
    private static volatile boolean libraryIsLoaded = false;
    private static final Logger logger = Logger.getLogger(MediaFactory.class);
    private volatile static MediaFactory instance;
    private volatile static int mediaFactoryRefCount = 0;

    private long nativeMediaFactoryHandle;

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
                    long nativeMediaFactoryHandle = nativeCreate(context);

                    if (nativeMediaFactoryHandle == 0) {
                        logger.e("Failed to instance MediaFactory");
                    } else {
                        instance = new MediaFactory(nativeMediaFactoryHandle);
                    }
                }
            }
        }

        return instance;
    }

    LocalMedia createLocalMedia() {
        checkReleased("createLocalMedia");
        long nativeLocalMediaHandle = nativeCreateLocalMedia(nativeMediaFactoryHandle);

        if (nativeLocalMediaHandle == 0) {
            logger.e("Failed to instance LocalMedia");
            return null;
        }
        synchronized (MediaFactory.class) {
            mediaFactoryRefCount++;
        }

        return new LocalMedia(nativeLocalMediaHandle, this);
    }

    void release() {
        if (instance != null) {
            synchronized (MediaFactory.class) {
                mediaFactoryRefCount = Math.max(0, --mediaFactoryRefCount);
                if (instance != null && mediaFactoryRefCount == 0) {
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
    }

    private void checkReleased(String methodName) {
        if (nativeMediaFactoryHandle == 0) {
            String releaseErrorMessage = String.format(RELEASE_MESSAGE_TEMPLATE, methodName);

            throw new IllegalStateException(releaseErrorMessage);
        }
    }

    private static native long nativeCreate(Context context);
    private static native long nativeCreateLocalMedia(long nativeMediaFactoryHandle);
    private native void nativeRelease(long mediaFactoryHandle);
}
