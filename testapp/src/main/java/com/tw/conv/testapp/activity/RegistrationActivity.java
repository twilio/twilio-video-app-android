package com.tw.conv.testapp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tw.conv.testapp.BuildConfig;
import com.tw.conv.testapp.R;
import com.tw.conv.testapp.TestAppApplication;
import com.tw.conv.testapp.dialog.Dialog;
import com.tw.conv.testapp.dialog.IceServersDialogFragment;
import com.tw.conv.testapp.model.TwilioIceResponse;
import com.tw.conv.testapp.model.TwilioIceServer;
import com.tw.conv.testapp.util.IceOptionsHelper;
import com.tw.conv.testapp.util.SimpleSignalingUtils;
import com.twilio.conversations.LogLevel;
import com.twilio.conversations.TwilioConversationsClient;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RegistrationActivity extends AppCompatActivity {
    public static final int PERMISSIONS_REQUEST_CODE = 0;

    public static final String OPTION_LOGGED_OUT_KEY = "loggedOut";

    private static final String USERNAME_KEY = "username";
    private static final String REALM_KEY = "realm";
    private static final String PREFER_H264_KEY = "preferH264";
    public static final String AUTO_ACCEPT_KEY = "autoAccept";
    public static final String USE_HEADSET_KEY = "startAudioUsingHeadset";
    public static final String AUTO_REGISTER_KEY = "autoRegister";
    private static final String ICE_OPTIONS_DIALOG = "IceOptionsDialog";

    private SharedPreferences sharedPreferences;
    private EditText usernameEditText;
    private Button registrationButton;
    private ProgressDialog progressDialog;
    private TextView versionText;
    private Spinner realmSpinner;
    private ArrayAdapter<CharSequence> spinnerAdapter;
    private CheckBox preferH264Checkbox;
    private CheckBox autoAcceptCheckbox;
    private CheckBox autoRegisterCheckbox;
    private CheckBox useHeadsetCheckbox;
    private ProgressDialog iceServerProgressDialog;
    private TwilioIceResponse twilioIceResponse;
    private List<TwilioIceServer> selectedTwilioIceServers;
    private String iceTransportPolicy = "";
    private Button iceOptionsButton;
    private IceServersDialogFragment iceOptionsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        usernameEditText = (EditText)findViewById(R.id.username_edittext);
        registrationButton = (Button)findViewById(R.id.registration_button);
        versionText = (TextView)findViewById(R.id.version_textview);

        versionText.setText(BuildConfig.VERSION_NAME);

        realmSpinner = (Spinner)findViewById(R.id.realm_spinner);
        preferH264Checkbox = (CheckBox) findViewById(R.id.prefer_h264_checkbox);
        autoAcceptCheckbox = (CheckBox) findViewById(R.id.auto_accept_checkbox);
        autoRegisterCheckbox = (CheckBox) findViewById(R.id.auto_register_checkbox);
        useHeadsetCheckbox = (CheckBox) findViewById(R.id.use_headset_checkbox);
        spinnerAdapter = ArrayAdapter.createFromResource(this,
                        R.array.realm_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        realmSpinner.setAdapter(spinnerAdapter);
        iceOptionsButton = (Button)findViewById(R.id.ice_options_button);
        iceOptionsButton.setOnClickListener(iceOptionsButtonClickListener());

        registrationButton.setOnClickListener(registrationClickListener());

        if (!checkPermissions()) {
            requestPermissions();
        }

        if (!BuildConfig.DEBUG) {
            UpdateManager.register(this, TestAppApplication.HOCKEY_APP_ID);
        }

        // Only restoring last registration if this is first launch
        if (savedInstanceState == null) {
            restoreLastSuccessfulRegistration();
        }

        boolean loggedOut = getIntent().getBooleanExtra(OPTION_LOGGED_OUT_KEY, false);
        if(!loggedOut &&
                !TextUtils.isEmpty(usernameEditText.getText()) &&
                autoRegisterCheckbox.isChecked()) {
            registrationButton.performClick();
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_DENIED ||
                grantResults[1] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this,
                    "Camera and Microphone permissions are required to have a Conversation.",
                    Toast.LENGTH_LONG).show();
        }
        if(grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            if (!BuildConfig.DEBUG) {
                // Perform registration once more when the external storage permission is granted
                UpdateManager.unregister();
                UpdateManager.register(this, TestAppApplication.HOCKEY_APP_ID);
            }
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

        if (!BuildConfig.DEBUG) {
            UpdateManager.unregister();
        }
    }

    private View.OnClickListener registrationClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(RegistrationActivity.this, null,
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
        };
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
        TwilioConversationsClient.setLogLevel(LogLevel.DEBUG);

        // We need to initialize here in case user logs out and completely tears down sdk
        if(!TwilioConversationsClient.isInitialized()) {
            TwilioConversationsClient.initialize(getApplicationContext());
            obtainCapabilityToken(username,
                    RegistrationActivity.this.realmSpinner.getSelectedItem()
                            .toString().toLowerCase());
        } else {
            obtainCapabilityToken(username,
                    RegistrationActivity.this.realmSpinner.getSelectedItem()
                            .toString().toLowerCase());
        }
    }

    private void obtainCapabilityToken(final String username, final String realm) {
        SimpleSignalingUtils.getAccessToken(username,
                realm, new Callback<String>() {

            @Override
            public void success(String capabilityToken, Response response) {
                if (response.getStatus() == 200) {
                    storeSuccessfulRegistration(username, realm);
                    startClient(username, capabilityToken, realm);
                } else {
                    progressDialog.dismiss();
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

    private View.OnClickListener iceOptionsButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (twilioIceResponse != null) {
                    showIceDialog();
                } else {
                    iceServerProgressDialog = ProgressDialog.show(
                            RegistrationActivity.this, null,
                            "Obtaining Twilio ICE Servers", true);
                    obtainTwilioIceServers(RegistrationActivity.this
                            .realmSpinner.getSelectedItem().toString().toLowerCase());
                }
            }
        };
    }

    private void showIceDialog(){
        if (iceOptionsDialog == null) {
            iceOptionsDialog = Dialog.createIceServersDialog(
                    twilioIceResponse.getIceServers(), iceOptionsDialogListener());
        }
        iceOptionsDialog.show(getSupportFragmentManager(), ICE_OPTIONS_DIALOG);
    }

    private IceServersDialogFragment.IceServersDialogListener iceOptionsDialogListener() {
        return new IceServersDialogFragment.IceServersDialogListener() {
            @Override
            public void onIceOptionsSelected(String iceTransportPolicy,
                                             List<TwilioIceServer> selectedServers) {
                RegistrationActivity.this.iceTransportPolicy = iceTransportPolicy;
                RegistrationActivity.this.selectedTwilioIceServers =
                        iceOptionsDialog.getSelectedServers();
            }

            @Override
            public void onIceOptionsCancel() {

            }
        };
    }

    private void obtainTwilioIceServers(final String realm) {
        SimpleSignalingUtils.getIceServers(realm, new Callback<TwilioIceResponse>() {
            @Override
            public void success(TwilioIceResponse twilioIceResponse, Response response) {
                iceServerProgressDialog.dismiss();
                if (response.getStatus() == 200) {
                    RegistrationActivity.this.twilioIceResponse = twilioIceResponse;
                    showIceDialog();
                } else {
                    Snackbar.make(registrationButton,
                            "ICE retrieval failed. Status: " + response.getStatus(),
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                iceServerProgressDialog.dismiss();
                Snackbar.make(registrationButton,
                        "ICE retrieval failed. Error: " + error.getMessage(),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void restoreLastSuccessfulRegistration() {
        usernameEditText.setText(sharedPreferences.getString(USERNAME_KEY, null));
        if(usernameEditText.getText() != null) {
            usernameEditText.setSelection(usernameEditText.getText().length());
        }
        Integer lastRealmPosition = getRealmPosition(sharedPreferences.getString(REALM_KEY, null));
        if (lastRealmPosition != null) {
            realmSpinner.setSelection(lastRealmPosition);
        }
        preferH264Checkbox.setChecked(sharedPreferences.getBoolean(PREFER_H264_KEY, false));
        autoAcceptCheckbox.setChecked(sharedPreferences.getBoolean(AUTO_ACCEPT_KEY, false));
        autoRegisterCheckbox.setChecked(sharedPreferences.getBoolean(AUTO_REGISTER_KEY, false));
        useHeadsetCheckbox.setChecked(sharedPreferences.getBoolean(USE_HEADSET_KEY, false));
    }

    private Integer getRealmPosition(String realm) {
        for (int i = 0 ; i < spinnerAdapter.getCount() ; i++) {
            String currentRealm = (String) spinnerAdapter.getItem(i);
            if (currentRealm.equalsIgnoreCase(realm)) {
                return i;
            }
        }

        return null;
    }

    private void storeSuccessfulRegistration(String username, String realm) {
        sharedPreferences.edit().putString(USERNAME_KEY, username).apply();
        sharedPreferences.edit().putString(REALM_KEY, realm.toLowerCase()).apply();
        sharedPreferences.edit().putBoolean(PREFER_H264_KEY, preferH264Checkbox.isChecked())
                .apply();
        sharedPreferences.edit().putBoolean(AUTO_ACCEPT_KEY, autoAcceptCheckbox.isChecked())
                .apply();
        sharedPreferences.edit().putBoolean(AUTO_REGISTER_KEY, autoRegisterCheckbox.isChecked())
                .apply();
        sharedPreferences.edit().putBoolean(USE_HEADSET_KEY, useHeadsetCheckbox.isChecked())
                .apply();
    }

    private void startClient(String username, String capabilityToken, String realm) {
        Intent intent = new Intent(this, ClientActivity.class);
        intent.putExtra(SimpleSignalingUtils.USERNAME, username);
        intent.putExtra(SimpleSignalingUtils.CAPABILITY_TOKEN, capabilityToken);
        intent.putExtra(SimpleSignalingUtils.REALM, realm);
        intent.putExtra(TwilioIceResponse.ICE_TRANSPORT_POLICY, iceTransportPolicy);
        intent.putExtra(ClientActivity.OPTION_AUTO_ACCEPT_KEY, autoAcceptCheckbox.isChecked());
        intent.putExtra(ClientActivity.OPTION_USE_HEADSET_KEY, useHeadsetCheckbox.isChecked());
        intent.putExtra(ClientActivity.OPTION_PREFER_H264_KEY, preferH264Checkbox.isChecked());
        if (selectedTwilioIceServers != null) {
            intent.putExtra(TwilioIceResponse.ICE_SELECTED_SERVERS,
                    IceOptionsHelper.convertToJson(selectedTwilioIceServers));
        }
        if (twilioIceResponse != null && twilioIceResponse.getIceServers() != null) {
            intent.putExtra(TwilioIceResponse.ICE_SERVERS,
                    IceOptionsHelper.convertToJson(twilioIceResponse.getIceServers()));
        }
        startActivity(intent);
        finish();
    }
}
