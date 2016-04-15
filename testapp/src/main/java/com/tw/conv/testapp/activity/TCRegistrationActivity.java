package com.tw.conv.testapp.activity;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tw.conv.testapp.BuildConfig;
import com.tw.conv.testapp.R;
import com.tw.conv.testapp.TestAppApplication;
import com.tw.conv.testapp.dialog.Dialog;
import com.tw.conv.testapp.dialog.IceServersDialogFragment;
import com.tw.conv.testapp.provider.TwilioIceServer;
import com.tw.conv.testapp.provider.TwilioIceServers;
import com.tw.conv.testapp.provider.TCCapabilityTokenProvider;
import com.tw.conv.testapp.provider.TCIceServersProvider;
import com.tw.conv.testapp.util.IceOptionsHelper;
import com.twilio.conversations.TwilioConversations;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TCRegistrationActivity extends AppCompatActivity {
    public static final int PERMISSIONS_REQUEST_CODE = 0;

    private static final String ICE_OPTIONS_DIALOG = "IceOptionsDialog";

    private EditText usernameEditText;
    private Button registrationButton;
    private ProgressDialog progressDialog;
    private TextView versionText;
    private Spinner realmSpinner;
    private ProgressDialog iceServerProgressDialog;
    private TwilioIceServers twilioIceServers;
    private List<TwilioIceServer> selectedTwilioIceServers;
    private String iceTransportPolicy = "";
    private Button iceOptionsButton;
    private IceServersDialogFragment iceOptionsDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usernameEditText = (EditText)findViewById(R.id.username_edittext);
        registrationButton = (Button)findViewById(R.id.registration_button);
        versionText = (TextView)findViewById(R.id.version_textview);

        versionText.setText(BuildConfig.VERSION_NAME);

        realmSpinner = (Spinner)findViewById(R.id.realm_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                        R.array.realm_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        realmSpinner.setAdapter(spinnerAdapter);
        iceOptionsButton = (Button)findViewById(R.id.ice_options_button);
        iceOptionsButton.setOnClickListener(iceOptionsButtonClickListener());

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
                    obtainCapabilityToken(username,
                            TCRegistrationActivity.this.realmSpinner.getSelectedItem().toString().toLowerCase());
                }

                @Override
                public void onError(Exception e) {
                    Snackbar.make(registrationButton, "Twilio initialization failed: " + e.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            });
        } else {
            obtainCapabilityToken(username,
                    TCRegistrationActivity.this.realmSpinner.getSelectedItem().toString().toLowerCase());
        }
    }

    private void obtainCapabilityToken(final String username, final String realm) {
        TCCapabilityTokenProvider.obtainTwilioCapabilityToken(username,
                realm, new Callback<String>() {

            @Override
            public void success(String capabilityToken, Response response) {
                if (response.getStatus() == 200) {
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
                if (twilioIceServers != null) {
                    showIceDialog();
                } else {
                    iceServerProgressDialog = ProgressDialog.show(
                            TCRegistrationActivity.this, null, "Obtaining Twilio ICE Servers", true);
                    obtainTwilioIceServers(TCRegistrationActivity.this
                            .realmSpinner.getSelectedItem().toString().toLowerCase());
                }
            }
        };
    }

    private void showIceDialog(){
        if (iceOptionsDialog == null) {
            iceOptionsDialog = Dialog.createIceServersDialog(
                    twilioIceServers.getIceServers(), iceOptionsDialogListener());
        }
        iceOptionsDialog.show(getSupportFragmentManager(), ICE_OPTIONS_DIALOG);
    }

    private IceServersDialogFragment.IceServersDialogListener iceOptionsDialogListener() {
        return new IceServersDialogFragment.IceServersDialogListener() {
            @Override
            public void onIceOptionsSelected(String iceTransportPolicy, List<TwilioIceServer> selectedServers) {
                TCRegistrationActivity.this.iceTransportPolicy = iceTransportPolicy;
                TCRegistrationActivity.this.selectedTwilioIceServers =
                        iceOptionsDialog.getSelectedServers();
            }

            @Override
            public void onIceOptionsCancel() {

            }
        };
    }

    private void obtainTwilioIceServers(final String realm) {
        TCIceServersProvider.obtainTwilioIceServers(realm, new Callback<TwilioIceServers>() {
            @Override
            public void success(TwilioIceServers twilioIceServers, Response response) {
                iceServerProgressDialog.dismiss();
                if (response.getStatus() == 200) {
                    TCRegistrationActivity.this.twilioIceServers = twilioIceServers;
                    showIceDialog();
                } else {
                    Snackbar.make(registrationButton,
                            "ICEretrieval failed. Status: " + response.getStatus(),
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

    private void startClient(String username, String capabilityToken, String realm) {
        Intent intent = new Intent(this, TCClientActivity.class);
        intent.putExtra(TCCapabilityTokenProvider.USERNAME, username);
        intent.putExtra(TCCapabilityTokenProvider.CAPABILITY_TOKEN, capabilityToken);
        intent.putExtra(TCCapabilityTokenProvider.REALM, realm);
        intent.putExtra(TwilioIceServers.ICE_TRANSPORT_POLICY, iceTransportPolicy);
        intent.putExtra(TwilioIceServers.ICE_SELECTED_SERVERS,
                IceOptionsHelper.convertToJson(selectedTwilioIceServers));
        intent.putExtra(TwilioIceServers.ICE_SERVERS,
                IceOptionsHelper.convertToJson(twilioIceServers.getIceServers()));
        startActivity(intent);
        finish();
    }
}
