package com.twilio.video.app.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.databinding.FragmentCommunityLoginBinding
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class CommunityLoginFragment: Fragment() {

    private var disposable = CompositeDisposable()
    private lateinit var binding: FragmentCommunityLoginBinding

    @Inject
    lateinit var authenticator: Authenticator

    private val viewModel: CommunityLoginViewModel by viewModels {
        CommunityLoginViewModelFactory(authenticator)
    }

    //    fun onTextChanged(editable: Editable?) {
//        enableLoginButton(!nameEditText.getText().toString().isEmpty())
//    }
//
//    fun onLoginButton(view: View?) {
//        val identity: String = nameEditText.getText().toString()
//        val passcode: String = passcodeEditText.getText().toString()
//        if (areIdentityAndPasscodeValid(identity, passcode)) {
//            login(identity, passcode)
//        }
    // }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCommunityLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if (authenticator.loggedIn()) startLobbyActivity();
    }

    override fun onDestroy() {
        requireActivity().finish()
        super.onDestroy()
    }

//    private fun areIdentityAndPasscodeValid(identity: String, passcode: String): Boolean {
//        return !identity.isEmpty() && !passcode.isEmpty()
//    }
//
//    private fun login(identity: String, passcode: String) {
//        preLoginViewState()
//        disposable.add(
//                authenticator
//                        .login(CommunityLoginEvent(identity, passcode))
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doFinally { postLoginViewState() }
//                        .subscribe(
//                                { loginResult: LoginResult? -> if (loginResult is CommunityLoginSuccessResult) startLobbyActivity() }
//                        ) { exception: Throwable? ->
//                            displayAuthError()
//                            Timber.e(exception)
//                        })
//    }
//
//    private fun preLoginViewState() {
//        InputUtils.hideKeyboard(this)
//        enableLoginButton(false)
//        progressBar.setVisibility(View.VISIBLE)
//    }
//
//    private fun postLoginViewState() {
//        progressBar.setVisibility(View.GONE)
//        enableLoginButton(true)
//    }
//
//    private fun enableLoginButton(isEnabled: Boolean) {
//        if (isEnabled) {
//            loginButton.setTextColor(Color.WHITE)
//            loginButton.setEnabled(true)
//        } else {
//            loginButton.setTextColor(
//                    ResourcesCompat.getColor(resources, R.color.colorButtonText, null))
//            loginButton.setEnabled(false)
//        }
//    }
//
//    private fun startLobbyActivity() {
//        RoomActivity.startActivity(this, getIntent().getData())
//        finish()
//    }
//
//    private fun displayAuthError() {
//        AlertDialog.Builder(this, R.style.AppTheme_Dialog)
//                .setTitle(getString(R.string.login_screen_auth_error_title))
//                .setMessage(getString(R.string.login_screen_auth_error_desc))
//                .setPositiveButton("OK", null)
//                .show()
//    }

}