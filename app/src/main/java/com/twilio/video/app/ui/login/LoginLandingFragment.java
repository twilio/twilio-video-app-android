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
import com.twilio.video.app.databinding.FragmentLoginLandingBinding;

import org.jetbrains.annotations.NotNull;

public class LoginLandingFragment extends Fragment {
    private FragmentLoginLandingBinding binding;

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
        binding = FragmentLoginLandingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        binding.googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        binding.googleSignInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        binding.googleSignInButton.setOnClickListener(v -> onGoogleSignInButton());
        binding.emailSignInButton.setOnClickListener(v -> onEmailSignInButton());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
