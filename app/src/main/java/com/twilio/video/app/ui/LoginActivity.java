package com.twilio.video.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.twilio.video.app.R;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.util.InputUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.username_edittext) EditText usernameEditText;
    @BindView(R.id.login_button) Button loginButton;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @OnTextChanged(R.id.username_edittext)
    public void onTextChanged(CharSequence username, int start, int count, int after) {
        if(!TextUtils.isEmpty(username)) {
            loginButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            loginButton.setEnabled(true);
        } else {
            loginButton.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.white));
            loginButton.setTextColor(ContextCompat.getColor(this, R.color.colorButtonText));
            loginButton.setEnabled(false);
        }
    }

    @OnClick(R.id.login_button)
    void login(View view) {
        String username = usernameEditText.getText().toString();
        if(username != null && username.length() != 0) {
            sharedPreferences.edit().putString(Preferences.IDENTITY, username).apply();
            InputUtils.hideKeyboard(this);
            startLobbyActivity();
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
        finish();
    }
}
