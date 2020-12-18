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
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import com.twilio.video.app.R;
import com.twilio.video.app.databinding.ExistingAccountLoginFragmentBinding;
import org.jetbrains.annotations.NotNull;

public class ExistingAccountLoginFragment extends Fragment {
    private ExistingAccountLoginFragmentBinding binding;

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
            @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ExistingAccountLoginFragmentBinding.inflate(inflater, container, false);
        TextWatcher textWatcher =
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        credentialsChanged(s);
                    }
                };
        binding.email.addTextChangedListener(textWatcher);
        binding.password.addTextChangedListener(textWatcher);
        binding.login.setOnClickListener(this::loginClicked);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void credentialsChanged(Editable editable) {
        if (binding.password.length() != 0
                && binding.email.length() != 0
                && Patterns.EMAIL_ADDRESS.matcher(binding.email.getText()).matches()) {
            binding.login.setTextColor(Color.WHITE);
            binding.login.setEnabled(true);
        } else {
            binding.login.setTextColor(
                    ResourcesCompat.getColor(getResources(), R.color.colorButtonText, null));
            binding.login.setEnabled(false);
        }
    }

    public void loginClicked(View view) {
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();
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

    public interface Listener {
        void onExistingAccountCredentials(String email, String password);
    }
}
