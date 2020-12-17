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

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.core.content.res.ResourcesCompat;
import com.twilio.video.app.R;
import com.twilio.video.app.auth.Authenticator;
import com.twilio.video.app.auth.CommunityLoginResult.CommunityLoginFailureResult;
import com.twilio.video.app.auth.CommunityLoginResult.CommunityLoginSuccessResult;
import com.twilio.video.app.auth.LoginEvent.CommunityLoginEvent;
import com.twilio.video.app.auth.LoginResult;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.api.AuthServiceError;
import com.twilio.video.app.databinding.DevelopmentActivityLoginBinding;
import com.twilio.video.app.ui.room.RoomActivity;
import com.twilio.video.app.util.InputUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import javax.inject.Inject;
import timber.log.Timber;

// TODO Create view model and fragment for this screen
public class CommunityLoginActivity extends BaseActivity {
    private DevelopmentActivityLoginBinding binding;

    @Inject Authenticator authenticator;

    CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DevelopmentActivityLoginBinding.inflate(getLayoutInflater());
        binding.communityLoginScreenPasscodeEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                onPasscodeTextChanged(s);
            }
        });
        binding.communityLoginScreenLoginButton.setOnClickListener(this::onLoginButton);

        setContentView(binding.getRoot());

        if (authenticator.loggedIn()) startLobbyActivity();
    }

    public void onPasscodeTextChanged(Editable editable) {
        enableLoginButton(isInputValid());
    }

    public void onLoginButton(View view) {
        String identity = binding.communityLoginScreenNameEdittext.getText().toString();
        String passcode = binding.communityLoginScreenPasscodeEdittext.getText().toString();
        login(identity, passcode);
    }

    private void login(String identity, String passcode) {
        preLoginViewState();

        disposable.add(
                authenticator
                        .login(new CommunityLoginEvent(identity, passcode))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(this::postLoginViewState)
                        .subscribe(
                                loginResult -> {
                                    if (loginResult instanceof CommunityLoginSuccessResult)
                                        startLobbyActivity();
                                    else {
                                        handleAuthError(loginResult);
                                    }
                                },
                                exception -> {
                                    handleAuthError(null);
                                    Timber.e(exception);
                                }));
    }

    private void handleAuthError(LoginResult loginResult) {

        if (loginResult instanceof CommunityLoginFailureResult) {
            String errorMessage;
            AuthServiceError error = ((CommunityLoginFailureResult) loginResult).getError();
            switch (error) {
                case INVALID_PASSCODE_ERROR:
                    errorMessage = getString(R.string.login_screen_invalid_passcode_error);
                    binding.communityLoginScreenPasscode.setError(errorMessage);
                    binding.communityLoginScreenPasscode.setErrorEnabled(true);
                    return;
                case EXPIRED_PASSCODE_ERROR:
                    errorMessage = getString(R.string.login_screen_expired_passcode_error);
                    binding.communityLoginScreenPasscode.setError(errorMessage);
                    binding.communityLoginScreenPasscode.setErrorEnabled(true);
                    return;
            }
        }

        displayAuthError();
    }

    private void preLoginViewState() {
        InputUtils.hideKeyboard(this);
        enableLoginButton(false);
        binding.communityLoginScreenProgressbar.setVisibility(View.VISIBLE);
        binding.communityLoginScreenPasscode.setErrorEnabled(false);
    }

    private void postLoginViewState() {
        binding.communityLoginScreenProgressbar.setVisibility(View.GONE);
        enableLoginButton(true);
    }

    private boolean isInputValid() {
        Editable nameEditable = binding.communityLoginScreenNameEdittext.getText();
        Editable passcodeEditable = binding.communityLoginScreenPasscodeEdittext.getText();

        if (nameEditable != null
                && passcodeEditable != null
                && !nameEditable.toString().isEmpty()
                && !passcodeEditable.toString().isEmpty()) {
            return true;
        }
        return false;
    }

    private void enableLoginButton(boolean isEnabled) {
        if (isEnabled) {
            binding.communityLoginScreenLoginButton.setTextColor(Color.WHITE);
            binding.communityLoginScreenLoginButton.setEnabled(true);
        } else {
            binding.communityLoginScreenLoginButton.setTextColor(
                    ResourcesCompat.getColor(getResources(), R.color.colorButtonText, null));
            binding.communityLoginScreenLoginButton.setEnabled(false);
        }
    }

    private void startLobbyActivity() {
        RoomActivity.Companion.startActivity(this, getIntent().getData());
        finish();
    }

    private void displayAuthError() {
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.login_screen_error_title))
                .setMessage(getString(R.string.login_screen_auth_error_desc))
                .setPositiveButton("OK", null)
                .show();
    }
}
