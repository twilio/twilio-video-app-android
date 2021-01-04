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
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.SignInButton;
import com.twilio.video.app.databinding.LoginLandingFragmentBinding;
import org.jetbrains.annotations.NotNull;

public class LoginLandingFragment extends Fragment {
    public interface Listener {
        void onSignInWithGoogle();

        void onSignInWithEmail();
    }

    private Listener mListener;

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
            @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoginLandingFragmentBinding binding =
                LoginLandingFragmentBinding.inflate(inflater, container, false);
        binding.googleSignIn.setSize(SignInButton.SIZE_WIDE);
        binding.googleSignIn.setColorScheme(SignInButton.COLOR_LIGHT);
        binding.googleSignIn.setOnClickListener(v -> onGoogleSignInButton());
        binding.emailSignIn.setOnClickListener(v -> onEmailSignInButton());
        return binding.getRoot();
    }

    public void onGoogleSignInButton() {
        if (mListener != null) {
            mListener.onSignInWithGoogle();
        }
    }

    public void onEmailSignInButton() {
        if (mListener != null) {
            mListener.onSignInWithEmail();
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
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
