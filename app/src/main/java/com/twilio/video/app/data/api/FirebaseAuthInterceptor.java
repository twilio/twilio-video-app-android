/*
 * Copyright (C) 2019 Twilio, Inc.
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

package com.twilio.video.app.data.api;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

class FirebaseAuthInterceptor implements Interceptor {
    private static final int FIREBASE_TOKEN_TIMEOUT_MS = 10000;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String FIREBASE_TOKEN_TASK_FAILED = "Failed to get Firebase Token";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request authorizedRequest =
                chain.request()
                        .newBuilder()
                        .addHeader(HEADER_AUTHORIZATION, getFirebaseToken())
                        .build();

        return chain.proceed(authorizedRequest);
    }

    /*
     * Performs a synchronous Firebase token retrieval.
     */
    private String getFirebaseToken() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final CountDownLatch tokenRequestComplete = new CountDownLatch(1);
        final StringBuffer tokenBuffer = new StringBuffer();

        if (firebaseUser == null) {
            throw new IllegalStateException("Firebase user is not found");
        }

        firebaseUser
                .getIdToken(true)
                .addOnSuccessListener(
                        getTokenResult -> {
                            tokenBuffer.append(getTokenResult.getToken());
                            tokenRequestComplete.countDown();
                        })
                .addOnFailureListener(
                        e -> {
                            Timber.e(e, FIREBASE_TOKEN_TASK_FAILED);
                            tokenRequestComplete.countDown();
                        });

        try {
            tokenRequestComplete.await(FIREBASE_TOKEN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Timber.e(e, FIREBASE_TOKEN_TASK_FAILED);
        }

        return tokenBuffer.toString();
    }
}
