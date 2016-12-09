package com.twilio.video.app.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.twilio.video.app.R;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.util.InputUtils;
import com.twilio.video.app.util.SimplerSignalingUtils;
import com.twilio.video.LogLevel;
import com.twilio.video.VideoClient;
import com.twilio.video.env.Env;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class LoginActivity extends BaseActivity {
    public static final int PERMISSIONS_REQUEST_CODE = 0;

    @BindView(R.id.username_edittext) EditText usernameEditText;
    @BindView(R.id.login_button) Button loginButton;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!checkPermissions()) {
            requestPermissions();
        }
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

    private boolean checkPermissions(){
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int resultStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return ((resultCamera == PackageManager.PERMISSION_GRANTED) &&
                (resultMic == PackageManager.PERMISSION_GRANTED) &&
                (resultStorage == PackageManager.PERMISSION_GRANTED));
    }

    private void requestPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this,
                    "Camera, Microphone, and Writing to External Storage permissions are " +
                            "requested. Please enabled them in App Settings.",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
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

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
        finish();
    }
}
