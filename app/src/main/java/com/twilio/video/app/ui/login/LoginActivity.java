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

package com.twilio.video.app.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twilio.video.app.R;
import com.twilio.video.app.auth.FirebaseAuthenticator;
import com.twilio.video.app.auth.GoogleAuthenticator;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.ui.room.RoomActivity;
import com.twilio.video.app.util.AuthHelper;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class LoginActivity extends BaseActivity
        implements LoginLandingFragment.Listener, ExistingAccountLoginFragment.Listener {

    public static final String EXTRA_SIGN_OUT = "SignOut";
    private static final int GOOGLE_SIGN_IN = 4615;

    @BindView(R.id.login_fragment_container)
    ViewGroup rootView;

    @Inject SharedPreferences sharedPreferences;
    @Inject GoogleAuthenticator googleAuthenticator;
    @Inject FirebaseAuthenticator firebaseAuthenticator;

    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.login_fragment_container, LoginLandingFragment.newInstance())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        firebaseAuthenticator.addAuthStateListener(fbAuthStateListener);
        super.onStart();
    }

    @Override
    protected void onStop() {
        firebaseAuthenticator.removeAuthStateListener(fbAuthStateListener);
        super.onStop();
    }

    private FirebaseAuth.AuthStateListener fbAuthStateListener =
            firebaseAuth -> {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    saveIdentity(user);
                    onSignInSuccess();
                }
            };

    private AuthHelper.ErrorListener errorListener =
            errorCode -> {
                processError(errorCode);
                dismissAuthenticatingDialog();
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = googleAuthenticator.getSignInResultFromIntent(data);
            if (result != null && result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    googleAuthenticator.signInWithGoogle(account, this, errorListener);
                }
                // TODO: failed to sign in with google
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // LoginLandingFragment
    @Override
    public void onSignInWithGoogle() {
        if (googleSignInClient == null) {
            googleSignInClient = googleAuthenticator.googleSignInClient(this);
        }
        Intent intent = googleSignInClient.getSignInIntent();
        showAuthenticatingDialog();
        startActivityForResult(intent, GOOGLE_SIGN_IN);
    }

    // LoginLandingFragment
    @Override
    public void onSignInWithEmail() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.login_fragment_container, ExistingAccountLoginFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    // ExistingAccountLoginFragment
    @Override
    public void onExistingAccountCredentials(String email, String password) {
        showAuthenticatingDialog();
        firebaseAuthenticator.login(email, password, this, errorListener);
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveIdentity(FirebaseUser user) {
        String email = (user.getEmail() != null) ? user.getEmail() : "";

        sharedPreferences
                .edit()
                .putString(Preferences.EMAIL, email)
                .putString(Preferences.DISPLAY_NAME, getDisplayName(user))
                .apply();
    }

    private String getDisplayName(FirebaseUser user) {
        String displayName = "";

        if (user.getDisplayName() != null) {
            displayName = user.getDisplayName();
        } else if (user.getEmail() != null) {
            displayName = user.getEmail().split("@")[0];
        }

        return displayName;
    }

    private void processError(@AuthHelper.Error int errorCode) {
        switch (errorCode) {
            case AuthHelper.ERROR_UNAUTHORIZED_EMAIL:
                Timber.e("Unathorized email.");
                showUnauthorizedEmailDialog();
                break;
            case AuthHelper.ERROR_AUTHENTICATION_FAILED:
            case AuthHelper.ERROR_FAILED_TO_GET_TOKEN:
                Snackbar.make(rootView, "Sign in Failed", Snackbar.LENGTH_LONG).show();
                Timber.e("Authentication error.");
                break;
            case AuthHelper.ERROR_GOOGLE_PLAY_SERVICE_ERROR:
                Snackbar.make(rootView, "Google play service error", Snackbar.LENGTH_LONG).show();
                Timber.e("Google play service error.");
                break;
            case AuthHelper.ERROR_UNKNOWN:
                Snackbar.make(rootView, "Unknown error occurred", Snackbar.LENGTH_LONG).show();
                Timber.e("Unknown error occurred.");
            case AuthHelper.ERROR_GOOGLE_SIGNIN_CANCELED:
                Snackbar.make(rootView, "Sign in canceled", Snackbar.LENGTH_LONG).show();
                Timber.e("Sign in canceled.");
                break;
            case AuthHelper.ERROR_USER_NOT_SIGNED_IN:
                Snackbar.make(rootView, "User not signed in", Snackbar.LENGTH_LONG).show();
                Timber.e("User not signed in.");
                break;
            default:
                // do nothing
                break;
        }
    }

    private void showUnauthorizedEmailDialog() {
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.unauthorized_title))
                .setMessage(getString(R.string.unauthorized_desc))
                .setPositiveButton("OK", null)
                .show();
    }

    private void dismissAuthenticatingDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    void showAuthenticatingDialog() {
        progressDialog = new ProgressDialog(this, R.style.Authenticating);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Authenticating");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    private void onSignInSuccess() {
        dismissAuthenticatingDialog();
        startLobbyActivity();
    }
}
