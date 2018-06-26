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

import android.support.annotation.VisibleForTesting;
import java.util.HashSet;
import java.util.Set;
import org.webrtc.EglBase;

class EglBaseProvider {
  private static final String RELEASE_MESSAGE_TEMPLATE =
      "EglBaseProvider released %s " + "unavailable";
  private static volatile EglBaseProvider instance;
  private static volatile Set<Object> eglBaseProviderOwners = new HashSet<>();

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
