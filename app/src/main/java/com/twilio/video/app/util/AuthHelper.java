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

package com.twilio.video.app.util;

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.twilio.video.app.R;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

// TODO Remove this class as part of this ticket https://issues.corp.twilio.com/browse/AHOYAPPS-63
public class AuthHelper {

    @Retention(SOURCE)
    @IntDef({
        ERROR_UNAUTHORIZED_EMAIL,
        ERROR_FAILED_TO_GET_TOKEN,
        ERROR_AUTHENTICATION_FAILED,
        ERROR_GOOGLE_SIGNIN_CANCELED,
        ERROR_GOOGLE_PLAY_SERVICE_ERROR,
        ERROR_USER_NOT_SIGNED_IN,
        ERROR_UNKNOWN
    })
    public @interface Error {}

    public static final int ERROR_UNAUTHORIZED_EMAIL = 0;
    public static final int ERROR_FAILED_TO_GET_TOKEN = 1;
    public static final int ERROR_AUTHENTICATION_FAILED = 2;
    public static final int ERROR_GOOGLE_SIGNIN_CANCELED = 3;
    public static final int ERROR_GOOGLE_PLAY_SERVICE_ERROR = 4;
    public static final int ERROR_USER_NOT_SIGNED_IN = 5;
    public static final int ERROR_UNKNOWN = 6;

    public interface ErrorListener {
        void onError(@AuthHelper.Error int errorCode);
    }

    public static void signInWithEmail(
            @NonNull String email,
            @NonNull String password,
            FragmentActivity activity,
            final ErrorListener errorListener) {
        if (email == null || email.length() == 0) {
            throw new IllegalArgumentException("Email can't be empty");
        }
        if (password == null || password.length() == 0) {
            throw new IllegalArgumentException("Password can't be empty");
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        activity,
                        task -> {
                            if (!task.isSuccessful()) {
                                errorListener.onError(ERROR_AUTHENTICATION_FAILED);
                            }
                        });
    }

    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
