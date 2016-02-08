package com.twilio.rtc.conversations.testapp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.twilio.rtc.conversations.testapp.BuildConfig;
import com.twilio.rtc.conversations.testapp.R;
import com.twilio.rtc.conversations.testapp.TestAppApplication;
import com.twilio.rtc.conversations.testapp.provider.TCCapabilityTokenProvider;
import com.twilio.conversations.TwilioConversations;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TCRegistrationActivity extends AppCompatActivity {
    public static final int PERMISSIONS_REQUEST_CODE = 0;

    private EditText usernameEditText;
    private Button registrationButton;
    private ProgressDialog progressDialog;
    private TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usernameEditText = (EditText)findViewById(R.id.username_edittext);
        registrationButton = (Button)findViewById(R.id.registration_button);
        versionText = (TextView)findViewById(R.id.version_textview);

        versionText.setText(BuildConfig.VERSION_NAME);

        registrationButton.setOnClickListener(registrationClickListener());

        if (!checkPermissions()) {
            requestPermissions();
        }

        UpdateManager.register(this, TestAppApplication.HOCKEY_APP_ID);
    }

    public boolean checkPermissions(){
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return ((resultCamera == PackageManager.PERMISSION_GRANTED) &&
                (resultMic == PackageManager.PERMISSION_GRANTED) &&
                (resultStorage == PackageManager.PERMISSION_GRANTED));
    }

    public void requestPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this,
                    "Camera, Microphone, and Writing to External Storage permissions are requested. Please enabled them in App Settings.",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_DENIED ||
                grantResults[1] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this,
                    "Camera and Microphone permissions are required to have a Conversation.",
                    Toast.LENGTH_LONG).show();
        }
        if(grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            // Perform registration once more when the external storage permission is granted
            UpdateManager.unregister();
            UpdateManager.register(this, TestAppApplication.HOCKEY_APP_ID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CrashManager.register(this, TestAppApplication.HOCKEY_APP_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UpdateManager.unregister();
    }

    private View.OnClickListener registrationClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(TCRegistrationActivity.this, null, "Registering with Twilio", true);
                String username = usernameEditText.getText().toString();
                if(username != null && username.length() != 0) {
                    hideKeyboard();
                    registerUser(username);
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    progressDialog.dismiss();
                }
            }
        };
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void registerUser(final String username) {

        TwilioConversations.setLogLevel(TwilioConversations.LogLevel.DEBUG);

        if(!TwilioConversations.isInitialized()) {
            TwilioConversations.initialize(getApplicationContext(), new TwilioConversations.InitListener() {
                @Override
                public void onInitialized() {
                    obtainCapabilityToken(username);
                }

                @Override
                public void onError(Exception e) {
                    Snackbar.make(registrationButton, "Twilio initialization failed: " + e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            });
        } else {
            obtainCapabilityToken(username);
        }
    }

    private void obtainCapabilityToken(final String username) {
        TCCapabilityTokenProvider.obtainTwilioCapabilityToken(username, new Callback<String>() {

            @Override
            public void success(String capabilityToken, Response response) {
                if (response.getStatus() == 200) {
                    startClient(username, capabilityToken);
                } else {
                    progressDialog.dismiss();
                    Snackbar.make(registrationButton, "Registration failed. Status: " + response.getStatus(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Snackbar.make(registrationButton, "Registration failed. Error: " + error.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    private void startClient(String username, String capabilityToken) {
        Intent intent = new Intent(this, TCClientActivity.class);
        intent.putExtra(TCCapabilityTokenProvider.USERNAME, username);
        intent.putExtra(TCCapabilityTokenProvider.CAPABILITY_TOKEN, capabilityToken);
        startActivity(intent);
        finish();
    }

}
