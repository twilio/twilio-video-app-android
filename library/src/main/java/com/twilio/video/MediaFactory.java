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

import java.util.HashSet;
import java.util.Set;

class MediaFactory {
    private static final String RELEASE_MESSAGE_TEMPLATE = "MediaFactory released %s unavailable";
    private static volatile boolean libraryIsLoaded = false;
    private static final Logger logger = Logger.getLogger(MediaFactory.class);
    private volatile static MediaFactory instance;
    private volatile static Set<Object> mediaFactoryOwners = new HashSet<>();

    private long nativeMediaFactoryHandle;
    private EglBaseProvider eglBaseProvider;

    static MediaFactory instance(@NonNull Object owner, @NonNull Context context) {
        Preconditions.checkNotNull(owner, "Owner must not be null");
        Preconditions.checkNotNull(context, "Context must not be null");
        synchronized (MediaFactory.class) {
            if (instance == null) {
                if (!libraryIsLoaded) {
                    ReLinker.loadLibrary(context, "jingle_peerconnection_so");
                    libraryIsLoaded = true;
                }


                // Create a temporary owner of EglBaseProvider to create the native media factory
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
                 * MediaFactory constructor will retain instance of EglBaseProvider so release
                 * temporary ownership.
                 */
                eglBaseProvider.release(temporaryEglOwner);
            }
            mediaFactoryOwners.add(owner);
        }

        return instance;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    static boolean isReleased() {
        synchronized (MediaFactory.class) {
            return instance == null;
        }
    }

    synchronized @Nullable LocalAudioTrack createAudioTrack(Context context,
                                                            boolean enabled,
                                                            @Nullable AudioOptions audioOptions) {
        Preconditions.checkNotNull(context, "Context must not be null");
        Preconditions.checkState(nativeMediaFactoryHandle != 0,
                RELEASE_MESSAGE_TEMPLATE,
                "createAudioTrack");
        return nativeCreateAudioTrack(nativeMediaFactoryHandle, context, enabled, audioOptions);
    }

    synchronized @Nullable LocalVideoTrack createVideoTrack(Context context,
                                                            boolean enabled,
                                                            VideoCapturer videoCapturer,
                                                            VideoConstraints videoConstraints) {
        Preconditions.checkNotNull(context, "Context must not be null");
        Preconditions.checkState(nativeMediaFactoryHandle != 0,
                RELEASE_MESSAGE_TEMPLATE,
                "createVideoTrack");
        return nativeCreateVideoTrack(nativeMediaFactoryHandle,
                context,
                enabled,
                videoCapturer,
                videoConstraints,
                eglBaseProvider.getLocalEglBase().getEglBaseContext());
    }

    void release(Object owner) {
        if (instance != null) {
            synchronized (MediaFactory.class) {
                mediaFactoryOwners.remove(owner);
                if (instance != null && mediaFactoryOwners.isEmpty()) {
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
                                                          Context context,
                                                          boolean enabled,
                                                          AudioOptions audioOptions);
    private native LocalVideoTrack nativeCreateVideoTrack(long nativeMediaFactoryHandle,
                                                          Context context,
                                                          boolean enabled,
                                                          VideoCapturer videoCapturer,
                                                          VideoConstraints videoConstraints,
                                                          EglBase.Context rootEglBase);
    private native void nativeRelease(long mediaFactoryHandle);
}
