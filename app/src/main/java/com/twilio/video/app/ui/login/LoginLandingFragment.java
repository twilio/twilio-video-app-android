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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.common.SignInButton;
import com.twilio.video.app.R;

public class LoginLandingFragment extends Fragment {

    public interface Listener {
        void onSignInWithGoogle();

        void onSignInWithEmail();
    }

    private Listener mListener;

    @BindView(R.id.google_sign_in_button)
    SignInButton googleSignInButton;

    @BindView(R.id.email_sign_in_button)
    Button emailSignInButton;

    public LoginLandingFragment() {}

    public static LoginLandingFragment newInstance() {
        return new LoginLandingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_landing, container, false);
        ButterKnife.bind(this, view);
        googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        googleSignInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        return view;
    }

    @OnClick(R.id.google_sign_in_button)
    public void onGoogleSignInButton() {
        if (mListener != null) {
            mListener.onSignInWithGoogle();
        }
    }

    @OnClick(R.id.email_sign_in_button)
    public void onEmailSignInButton() {
        if (mListener != null) {
            mListener.onSignInWithEmail();
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
}
