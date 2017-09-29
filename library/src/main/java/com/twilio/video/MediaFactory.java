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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

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

    static MediaFactory instance(@NonNull Context context) {
        Preconditions.checkNotNull(context, "Context must not be null");
        if (instance == null) {
            synchronized (MediaFactory.class) {
                if (instance == null) {
                    if (!libraryIsLoaded) {
                        ReLinker.loadLibrary(context, "jingle_peerconnection_so");
                        libraryIsLoaded = true;
                    }

                    /*
                     * We need to create a temporary owner of EglBaseProvider to create our native
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

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    static boolean isReleased() {
        synchronized (MediaFactory.class) {
            return instance == null;
        }
    }

    synchronized LocalAudioTrack createAudioTrack(boolean enabled,
                                                  @Nullable AudioOptions audioOptions,
                                                  @Nullable String name) {
        Preconditions.checkState(nativeMediaFactoryHandle != 0,
                RELEASE_MESSAGE_TEMPLATE,
                "createAudioTrack");
        /*
         * Add a reference to ensure that if the creation of the track fails the MediaFactory
         * instance is destroyed when release() is called below.
         */
        addRef();
        LocalAudioTrack localAudioTrack = nativeCreateAudioTrack(nativeMediaFactoryHandle,
                enabled,
                audioOptions,
                name);

        if (localAudioTrack != null) {
            return localAudioTrack;
        } else {
            release();
            logger.e("Failed to create local audio track");
            return null;
        }
    }

    synchronized LocalVideoTrack createVideoTrack(boolean enabled,
                                                  @NonNull VideoCapturer videoCapturer,
                                                  @Nullable VideoConstraints videoConstraints,
                                                  @Nullable String name) {
        Preconditions.checkState(nativeMediaFactoryHandle != 0,
                RELEASE_MESSAGE_TEMPLATE,
                "createVideoTrack");
        /*
         * Add a reference to ensure that if the creation of the track fails the MediaFactory
         * instance is destroyed when release() is called below.
         */
        addRef();
        LocalVideoTrack localVideoTrack = nativeCreateVideoTrack(nativeMediaFactoryHandle,
                enabled,
                videoCapturer,
                videoConstraints,
                name,
                eglBaseProvider.getLocalEglBase().getEglBaseContext());

        if (localVideoTrack != null) {
            return localVideoTrack;
        } else {
            release();
            logger.e("Failed to create local video track");
            return null;
        }
    }

    synchronized LocalDataTrack createDataTrack(boolean ordered,
                                                int maxPacketLifeTime,
                                                int maxRetransmits,
                                                String name) {
        Preconditions.checkState(nativeMediaFactoryHandle != 0,
                RELEASE_MESSAGE_TEMPLATE,
                "createDataTrack");
        addRef();
        return nativeCreateDataTrack(nativeMediaFactoryHandle,
                ordered,
                maxPacketLifeTime,
                maxRetransmits,
                name);
    }

    void addRef() {
        synchronized (MediaFactory.class) {
            Preconditions.checkNotNull(instance, "MediaFactory instance must not be null");
            mediaFactoryRefCount++;
        }
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

    private static native long nativeCreate(Context context,
                                            EglBase.Context localEglBase,
                                            EglBase.Context remoteEglBase);
    private native LocalAudioTrack nativeCreateAudioTrack(long nativeMediaFactoryHandle,
                                                          boolean enabled,
                                                          AudioOptions audioOptions,
                                                          String name);
    private native LocalVideoTrack nativeCreateVideoTrack(long nativeMediaFactoryHandle,
                                                          boolean enabled,
                                                          VideoCapturer videoCapturer,
                                                          VideoConstraints videoConstraints,
                                                          String name,
                                                          EglBase.Context rootEglBase);
    private native LocalDataTrack nativeCreateDataTrack(long nativeMediaFactoryHandle,
                                                        boolean ordered,
                                                        int maxPacketLifeTime,
                                                        int maxRetransmits,
                                                        String name);
    private native void nativeRelease(long mediaFactoryHandle);
}
