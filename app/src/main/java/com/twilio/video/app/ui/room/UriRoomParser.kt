package com.twilio.video.app.ui.room

class UriRoomParser(private val uri: UriWrapper) {

    fun parseRoom(): String? =
            uri.pathSegments?.let {
                if (it.size >= 2) {
                    it[1]
                } else null
            }
}
