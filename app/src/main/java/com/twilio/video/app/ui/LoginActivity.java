package com.twilio.video.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twilio.video.app.R;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.util.AuthHelper;

import javax.inject.Inject;

import butterknife.ButterKnife;
import timber.log.Timber;

public class LoginActivity extends BaseActivity
        implements LoginLandingFragment.Listener, ExistingAccountLoginFragment.Listener {

    public static final String EXTRA_SIGN_OUT = "SignOut";
    private static final int GOOGLE_SIGN_IN = 4615;

    @Inject SharedPreferences sharedPreferences;

    private ProgressDialog progressDialog;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.login_fragment_container, LoginLandingFragment.newInstance())
                .commit();
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        if (intent.getBooleanExtra(EXTRA_SIGN_OUT, false)) {
            if (googleApiClient == null) {
                googleApiClient = AuthHelper.buildGoogleAPIClient(this, null);
            }
            AuthHelper.signOut(googleApiClient, new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    // TODO: stop spinning
                }
            });
        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(fbAuthStateListener);
        super.onStart();
    }

    @Override
    protected void onStop() {
        FirebaseAuth.getInstance().removeAuthStateListener(fbAuthStateListener);
        super.onStop();
    }

    private FirebaseAuth.AuthStateListener fbAuthStateListener =
        new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    saveIdentity(user);
                    onSignInSuccess();
                }
            }
        };

    private AuthHelper.ErrorListener errorListener = new AuthHelper.ErrorListener() {
        @Override
        public void onError(@AuthHelper.Error int errorCode) {
            processError(errorCode);
            dismissAuthenticatingDialog();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                AuthHelper.signInWithGoogle(account, this, errorListener);
            } else {
                // TODO: failed to sign in with google
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // LoginLandingFragment
    @Override
    public void onSignInWithGoogle() {
        if (googleApiClient == null) {
            googleApiClient =
                AuthHelper.buildGoogleAPIClient(this, errorListener);
        }
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        showAuthenticatingDialog();
        startActivityForResult(intent, GOOGLE_SIGN_IN);
    }

    // LoginLandingFragment
    @Override
    public void onSignInWithEmail() {
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.login_fragment_container, ExistingAccountLoginFragment.newInstance())
            .addToBackStack(null)
            .commit();
    }

    // ExistingAccountLoginFragment
    @Override
    public void onExistingAccountCredentials(String email, String password) {
        showAuthenticatingDialog();
        AuthHelper.signInWithEmail(email, password, this, errorListener);
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveIdentity(FirebaseUser user) {

        String email = (user.getEmail() != null) ? user.getEmail() : "";

        String displayName = "";
        if (user.getDisplayName() != null) {
            displayName = user.getDisplayName();
        } else if (user.getEmail() != null) {
            displayName = user.getEmail().split("@")[0];
        }

        sharedPreferences.edit()
            .putString(Preferences.EMAIL, email)
            .putString(Preferences.DISPLAY_NAME, displayName)
            .apply();
    }

    private void processError(@AuthHelper.Error int errorCode) {
        switch (errorCode) {
            case AuthHelper.ERROR_UNAUTHORIZED_EMAIL :
                Timber.e("Unathorized email.");
                showUnauthorizedEmailDialog();
                break;
            case AuthHelper.ERROR_AUTHENTICATION_FAILED:
            case AuthHelper.ERROR_FAILED_TO_GET_TOKEN:
                Toast.makeText(this, "SignIn Failed.", Toast.LENGTH_LONG).show();
                Timber.e("Authentication error.");
                break;
            case AuthHelper.ERROR_GOOGLE_PLAY_SERVICE_ERROR:
                Toast.makeText(this, "Google play service error.", Toast.LENGTH_LONG).show();
                Timber.e("Google play service error.");
                break;
            case AuthHelper.ERROR_UNKNOWN:
                Toast.makeText(this, "Unknown error occurred.", Toast.LENGTH_LONG).show();
                Timber.e("Unknown error occurred.");
            default:
                // do nothing
                break;
        }
    }

    private void showUnauthorizedEmailDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
            .setTitle(getString(R.string.unauthorized_title))
            .setMessage(getString(R.string.unauthorized_desc))
            .setPositiveButton("OK", null)
            .show();
    }

    private void dismissAuthenticatingDialog() {
        if( progressDialog != null ) {
            progressDialog.dismiss();
        }
    }

    private void showAuthenticatingDialog() {
        progressDialog = new ProgressDialog(this, R.style.Authenticating);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Authenticating");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    public void onSignInSuccess() {
        dismissAuthenticatingDialog();
        startLobbyActivity();
    }
}
