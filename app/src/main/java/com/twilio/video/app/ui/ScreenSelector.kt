package com.twilio.video.app.ui

import androidx.appcompat.app.AppCompatActivity

interface ScreenSelector {

    val loginScreen: Class<out AppCompatActivity>
}