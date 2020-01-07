/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.app.ui.login;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.twilio.video.app.R;

public class ExistingAccountLoginFragment extends Fragment {

    @BindView(R.id.email_edittext)
    EditText emailEditText;

    @BindView(R.id.password_edittext)
    EditText passwordEditText;

    @BindView(R.id.login_button)
    Button loginButton;

    private Listener mListener;

    public ExistingAccountLoginFragment() {}

    public static ExistingAccountLoginFragment newInstance() {

        return new ExistingAccountLoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_existing_account_login, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnTextChanged({R.id.email_edittext, R.id.password_edittext})
    public void onTextChanged(Editable editable) {
        if (passwordEditText.length() != 0
                && emailEditText.length() != 0
                && Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
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
        if (email.length() > 0
                && password.length() > 0
                && Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && (mListener != null)) {
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
            throw new RuntimeException(context.toString() + " must implement Listener");
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
