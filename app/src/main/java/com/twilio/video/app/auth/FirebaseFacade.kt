package com.twilio.video.app.auth

import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.twilio.video.app.util.AuthHelper
import com.twilio.video.app.util.AuthHelper.ERROR_AUTHENTICATION_FAILED

class FirebaseFacade {

    fun signInWithEmail(
            email: String,
            password: String,
            activity: FragmentActivity,
            errorListener: AuthHelper.ErrorListener) {
        require(!(email == null || email.length == 0)) { "Email can't be empty" }
        require(!(password == null || password.length == 0)) { "Password can't be empty" }
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        activity
                ) { task ->
                    if (!task.isSuccessful) {
                        errorListener.onError(ERROR_AUTHENTICATION_FAILED)
                    }
                }
    }
}