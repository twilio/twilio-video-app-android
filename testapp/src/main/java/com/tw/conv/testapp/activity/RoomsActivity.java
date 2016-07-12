package com.tw.conv.testapp.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.tw.conv.testapp.R;
import com.tw.conv.testapp.util.SimpleSignalingUtils;
import com.twilio.common.AccessManager;
import com.twilio.conversations.Client;
import com.twilio.conversations.Participant;
import com.twilio.conversations.Room;
import com.twilio.conversations.RoomsException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class RoomsActivity extends AppCompatActivity {

    @BindView(R.id.username_edittext) EditText usernameEditText;
    @BindView(R.id.registration_button) Button registrationButton;
    @BindView(R.id.version_textview) TextView versionText;
    @BindView(R.id.realm_spinner) Spinner realmSpinner;
    @BindView(R.id.prefer_h264_checkbox) CheckBox preferH264Checkbox;
    @BindView(R.id.auto_accept_checkbox) CheckBox autoAcceptCheckbox;
    @BindView(R.id.auto_register_checkbox) CheckBox autoRegisterCheckbox;
    @BindView(R.id.use_headset_checkbox) CheckBox useHeadsetCheckbox;
    @BindView(R.id.logout_when_conv_ends_checkbox) CheckBox logoutWhenConvEndsCheckbox;
    @BindView(R.id.ice_options_button) Button iceOptionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rooms);
        ButterKnife.bind(this);

        obtainCapabilityToken("ird", "prod");
    }


    private void obtainCapabilityToken(final String username, final String realm) {
        SimpleSignalingUtils.getAccessToken(username,
                realm, new Callback<String>() {

            @Override
            public void success(String capabilityToken, Response response) {
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
                Snackbar.make(registrationButton,
                        "Registration failed. Error: " + error.getMessage(),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    private void startClient(String capabilityToken) {
        Timber.i("Start Client");
        AccessManager accessManager = new AccessManager(this, capabilityToken, null);
        Client client = new Client(this, accessManager, new Client.Listener() {
            @Override
            public void onConnected(Room room) {
                Timber.i("Connected to room");
            }

            @Override
            public void onConnectFailure(RoomsException error) {
                Timber.i("Connect Failure");
            }

            @Override
            public void onDisconnected(Room room, RoomsException error) {
                Timber.i("Disconnected from room");
            }
        });

        client.connect(new Room.Listener() {
            @Override
            public void onParticipantConnected(Room room, Participant participant) {

            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {

            }
        });
    }

}