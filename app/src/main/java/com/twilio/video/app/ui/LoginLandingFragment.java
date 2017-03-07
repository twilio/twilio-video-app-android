package com.twilio.video.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;
import com.twilio.video.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginLandingFragment extends Fragment {

    public interface Listener {
        void onSignInWithGoogle();
        void onSignInWithEmail();
    }

    private Listener mListener;

    @BindView(R.id.google_sign_in_button) SignInButton googleSignInButton;
    @BindView(R.id.email_sign_in_button) Button emailSignInButton;

    public LoginLandingFragment() {
    }

    public static LoginLandingFragment newInstance() {
        LoginLandingFragment fragment = new LoginLandingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
