package com.twilio.video.app.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.twilio.video.app.R

private const val EMAIL_KEY = "EMAIL_KEY"

class SignedInActivity : AppCompatActivity() {

    private lateinit var signedInTextView: TextView

    companion object {

        fun createIntent(email: String): Intent {
            val bundle = Bundle().apply { putString(EMAIL_KEY, email) }
            return Intent().putExtras(bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signed_in)

        intent.extras?.let {
            val email = it.getString(EMAIL_KEY)
            findViewById<TextView>(R.id.signedInText).text = email
        }
    }
}