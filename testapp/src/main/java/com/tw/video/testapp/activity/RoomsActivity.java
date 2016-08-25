package com.tw.video.testapp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tw.video.testapp.BuildConfig;
import com.tw.video.testapp.R;
import com.tw.video.testapp.util.SimpleSignalingUtils;
import com.twilio.common.AccessManager;
import com.twilio.video.AudioTrack;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LogLevel;
import com.twilio.video.Media;
import com.twilio.video.Participant;
import com.twilio.video.VideoClient;
import com.twilio.video.Room;
import com.twilio.video.VideoException;
import com.twilio.video.VideoTrack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class RoomsActivity extends AppCompatActivity {

    @BindView(R.id.username_edittext) EditText usernameEditText;
    @BindView(R.id.room_name_edittext) EditText roomEditText;
    @BindView(R.id.registration_button) Button registrationButton;
    @BindView(R.id.version_textview) TextView versionText;
    @BindView(R.id.realm_spinner) Spinner realmSpinner;

    public static final int PERMISSIONS_REQUEST_CODE = 0;

    private ProgressDialog progressDialog;
    private ArrayAdapter<CharSequence> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rooms);
        ButterKnife.bind(this);

        versionText.setText(BuildConfig.VERSION_NAME);

        spinnerAdapter = ArrayAdapter.createFromResource(this,
                        R.array.realm_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        realmSpinner.setAdapter(spinnerAdapter);
        if (!checkPermissions()) {
            requestPermissions();
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

    @OnClick(R.id.registration_button)
    void register(View view) {
        progressDialog = ProgressDialog.show(RoomsActivity.this, null,
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
        obtainCapabilityToken(username,
                RoomsActivity.this.realmSpinner.getSelectedItem()
                        .toString().toLowerCase());
    }

    private void obtainCapabilityToken(final String username, final String realm) {
        SimpleSignalingUtils.getAccessToken(username,
                realm, new Callback<String>() {

            @Override
            public void success(String capabilityToken, Response response) {
                progressDialog.dismiss();
                if (response.getStatus() == 200) {
                    startClient(capabilityToken);
                } else {
                    Snackbar.make(registrationButton,
                            "Registration failed. Status: " + response.getStatus(),
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Snackbar.make(registrationButton,
                        "Registration failed. Error: " + error.getMessage(),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    private void startClient(String capabilityToken) {
        Timber.i("Start VideoClient");

        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra(SimpleSignalingUtils.ROOM_NAME, roomEditText.getText().toString());
        intent.putExtra(SimpleSignalingUtils.CAPABILITY_TOKEN, capabilityToken);
        intent.putExtra(SimpleSignalingUtils.REALM, "prod");
        intent.putExtra(SimpleSignalingUtils.USERNAME, usernameEditText.getText().toString());

        VideoClient.setLogLevel(LogLevel.DEBUG);

        startActivity(intent);
        finish();
    }
}