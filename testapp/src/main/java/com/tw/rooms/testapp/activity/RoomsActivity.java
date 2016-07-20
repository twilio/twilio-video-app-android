package com.tw.rooms.testapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.tw.rooms.testapp.BuildConfig;
import com.tw.rooms.testapp.R;
import com.tw.rooms.testapp.util.SimpleSignalingUtils;
import com.twilio.common.AccessManager;
import com.twilio.rooms.LogLevel;
import com.twilio.rooms.Participant;
import com.twilio.rooms.RoomsClient;
import com.twilio.rooms.Room;
import com.twilio.rooms.RoomsException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class RoomsActivity extends AppCompatActivity {

    @BindView(R.id.username_edittext) EditText usernameEditText;
    @BindView(R.id.registration_button) Button registrationButton;
    @BindView(R.id.version_textview) TextView versionText;
    @BindView(R.id.realm_spinner) Spinner realmSpinner;

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
        Timber.i("Start Client");

        Snackbar.make(registrationButton,
                "Starting the client...",
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        AccessManager accessManager = new AccessManager(this, capabilityToken, null);
        RoomsClient.setLogLevel(LogLevel.DEBUG);
        RoomsClient roomsClient = new RoomsClient(this, accessManager, new RoomsClient.Listener() {
            @Override
            public void onConnected(Room room) {
                Timber.i("onConnected");
            }

            @Override
            public void onConnectFailure(RoomsException error) {
                Timber.i("onConnectFailure");
            }

            @Override
            public void onDisconnected(Room room, RoomsException error) {
                Timber.i("onDisconnected");
            }
        });

        roomsClient.connect(new Room.Listener() {
            @Override
            public void onParticipantConnected(Room room, Participant participant) {

            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {

            }
        });
    }

}