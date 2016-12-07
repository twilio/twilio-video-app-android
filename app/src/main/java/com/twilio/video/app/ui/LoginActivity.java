package com.twilio.video.app.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.R;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.util.SimpleSignalingUtils;
import com.twilio.video.LogLevel;
import com.twilio.video.VideoClient;
import com.twilio.video.env.Env;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

import static com.twilio.video.app.util.SimpleSignalingUtils.P2P;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.username_edittext) EditText usernameEditText;
    @BindView(R.id.login_button) Button loginButton;
    @BindView(R.id.realm_spinner) Spinner realmSpinner;
    @BindView(R.id.topology_spinner) Spinner topologySpinner;

    public static final int PERMISSIONS_REQUEST_CODE = 0;
    public static final String TWILIO_ENV_KEY = "TWILIO_ENVIRONMENT";

    private ProgressDialog progressDialog;
    private String realm;
    private String topology = P2P;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        realmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                realm = SimpleSignalingUtils.REALMS.get(position);
                Env.set(LoginActivity.this, TWILIO_ENV_KEY, getResources().getStringArray(R.array.realm_array)[position], true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        topologySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                topology = getResources().getStringArray(R.array.topology_array)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (!checkPermissions()) {
            requestPermissions();
        }

    }

    @OnTextChanged(R.id.username_edittext)
    public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        if(after == 0) {
            loginButton.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.colorButtonText));
        } else {
            loginButton.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.white));
        }
    }

    public boolean checkPermissions(){
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int resultStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return ((resultCamera == PackageManager.PERMISSION_GRANTED) &&
                (resultMic == PackageManager.PERMISSION_GRANTED) &&
                (resultStorage == PackageManager.PERMISSION_GRANTED));
    }

    public void requestPermissions(){
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
        progressDialog = ProgressDialog.show(LoginActivity.this, null,
                "Registering with Twilio", true);
        String username = usernameEditText.getText().toString();
        if(username != null && username.length() != 0) {
            hideKeyboard();
            registerUser(username);
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            progressDialog.dismiss();
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

    private void registerUser(final String username) {
        obtainCapabilityToken(username, realm);
    }

    private void obtainCapabilityToken(final String username, final String realm) {
        SimpleSignalingUtils.getAccessToken(username,
                realm, topology, new Callback<String>() {

            @Override
            public void success(String capabilityToken, Response response) {
                progressDialog.dismiss();
                if (response.getStatus() == 200) {
                    startClient(capabilityToken);
                } else {
                    Snackbar.make(loginButton,
                            "Registration failed. Status: " + response.getStatus(),
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Snackbar.make(loginButton,
                        "Registration failed. Error: " + error.getMessage(),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    private void startClient(String capabilityToken) {
        Timber.i("Start VideoClient");

        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra(SimpleSignalingUtils.CAPABILITY_TOKEN, capabilityToken);
        intent.putExtra(SimpleSignalingUtils.REALM, realm);
        intent.putExtra(SimpleSignalingUtils.TOPOLOGY, topology);
        intent.putExtra(SimpleSignalingUtils.USERNAME, usernameEditText.getText().toString());

        VideoClient.setLogLevel(LogLevel.DEBUG);

        startActivity(intent);
        finish();
    }
}