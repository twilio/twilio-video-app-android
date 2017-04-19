package com.twilio.video.app.ui.login;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.util.Patterns;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.twilio.video.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;


public class ExistingAccountLoginFragment extends Fragment {

    @BindView(R.id.email_edittext) EditText emailEditText;
    @BindView(R.id.password_edittext) EditText passwordEditText;
    @BindView(R.id.login_button) Button loginButton;


    private Listener mListener;

    public ExistingAccountLoginFragment() {
    }


    public static ExistingAccountLoginFragment newInstance() {
        ExistingAccountLoginFragment fragment = new ExistingAccountLoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_existing_account_login, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnTextChanged({R.id.email_edittext, R.id.password_edittext})
    public void onTextChanged(Editable editable) {
        if (passwordEditText.length() != 0 &&
            emailEditText.length() != 0 &&
            Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
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
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (email != null && email.length() > 0 &&
            password != null && password.length() > 0 &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches() && (mListener != null)) {
            mListener.onExistingAccountCredentials(email, password);
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            mListener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface Listener {
        void onExistingAccountCredentials(String email, String password);
    }
}
