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

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.core.content.res.ResourcesCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.twilio.video.app.R;
import com.twilio.video.app.auth.Authenticator;
import com.twilio.video.app.auth.LoginEvent.CommunityLoginEvent;
import com.twilio.video.app.auth.LoginResult.CommunityLoginSuccessResult;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.ui.room.RoomActivity;

import javax.inject.Inject;
import timber.log.Timber;

// TODO Remove as part of https://issues.corp.twilio.com/browse/AHOYAPPS-93
public class CommunityLoginActivity extends BaseActivity {

    @Inject Authenticator authenticator;

    @BindView(R.id.name_edittext)
    EditText nameEditText;

    @BindView(R.id.login_button)
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.development_activity_login);
        ButterKnife.bind(this);
        if (authenticator.loggedIn()) startLobbyActivity();
    }

    @OnTextChanged(R.id.name_edittext)
    public void onTextChanged(Editable editable) {
        if (nameEditText.length() != 0) {
            loginButton.setTextColor(Color.WHITE);
            loginButton.setEnabled(true);
        } else {
            loginButton.setTextColor(
                    ResourcesCompat.getColor(getResources(), R.color.colorButtonText, null));
            loginButton.setEnabled(false);
        }
    }

    @OnClick(R.id.login_button)
    public void onLoginButton(View view) {
        String name = nameEditText.getText().toString();
        if (name.length() > 0) {
            saveIdentity(name);
        }
    }

    private void saveIdentity(String displayName) {
        authenticator
                .login(new CommunityLoginEvent(displayName))
                .subscribe(
                        loginResult -> {
                            if (loginResult instanceof CommunityLoginSuccessResult)
                                startLobbyActivity();
                        },
                        exception -> Timber.e(exception));
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
        finish();
    }
}
