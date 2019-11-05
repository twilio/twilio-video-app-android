package com.twilio.video.app.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.ui.room.RoomActivity;
import javax.inject.Inject;

public class CommunityLoginActivity extends BaseActivity {
    @Inject SharedPreferences sharedPreferences;

    @BindView(R.id.name_edittext)
    EditText nameEditText;

    @BindView(R.id.login_button)
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.development_activity_login);
        ButterKnife.bind(this);
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
        if (name != null && name.length() > 0) {
            saveIdentity(name);
            startLobbyActivity();
        }
    }

    private void saveIdentity(String name) {
        sharedPreferences.edit().putString(Preferences.DISPLAY_NAME, name).apply();
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
        finish();
    }
}
