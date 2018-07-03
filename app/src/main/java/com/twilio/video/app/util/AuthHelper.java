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

package com.twilio.video.app.util;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.twilio.video.app.R;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class AuthHelper {

    @Retention(SOURCE)
    @IntDef({ERROR_UNAUTHORIZED_EMAIL,
        ERROR_FAILED_TO_GET_TOKEN,
        ERROR_AUTHENTICATION_FAILED,
        ERROR_GOOGLE_SIGNIN_CANCELED,
        ERROR_GOOGLE_PLAY_SERVICE_ERROR,
        ERROR_USER_NOT_SIGNED_IN,
        ERROR_UNKNOWN})
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

    public static void signInWithGoogle(GoogleSignInAccount account,
                                        FragmentActivity activity,
                                        final ErrorListener errorListener) {
        if( account.getEmail().endsWith("@twilio.com") ) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            AuthCredential credential =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (!task.isSuccessful()) {
                        errorListener.onError(ERROR_AUTHENTICATION_FAILED);
                        return;
                    }
                });
        } else {
            errorListener.onError(ERROR_UNAUTHORIZED_EMAIL);
        }
    }

    public static void signInWithEmail(@NonNull String email,
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
            .addOnCompleteListener(activity, task -> {
                if (!task.isSuccessful()) {
                    errorListener.onError(ERROR_AUTHENTICATION_FAILED);
                    return;
                }
            });
    }

    public static void signOut(@NonNull GoogleApiClient googleApiClient,
                               ResultCallback<Status> resultCallback) {
        FirebaseAuth.getInstance().signOut();
        if (googleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(resultCallback);
        }
    }

    public static GoogleApiClient buildGoogleAPIClient(final FragmentActivity activity,
                                                        final ErrorListener errorListener) {
        GoogleApiClient client = new GoogleApiClient.Builder(activity)
            .enableAutoManage(activity,
                    connectionResult -> errorListener.onError(ERROR_GOOGLE_PLAY_SERVICE_ERROR))
            .addApi(Auth.GOOGLE_SIGN_IN_API, buildGoogleSignInOptions(activity))
            .build();
        return client;
    }

    private static GoogleSignInOptions buildGoogleSignInOptions(final FragmentActivity activity) {
        Context context = activity.getBaseContext();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .setHostedDomain("twilio.com")
            .build();
        return gso;
    }
}
