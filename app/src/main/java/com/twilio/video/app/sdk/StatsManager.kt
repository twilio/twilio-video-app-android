package com.twilio.video.app.sdk

import com.twilio.video.StatsListener
import com.twilio.video.StatsReport
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.ui.room.RoomActivity
import com.twilio.video.app.util.StatsScheduler

class StatsManager(private val roomManager: RoomManager) {

    private val statsScheduler: StatsScheduler = StatsScheduler()

    // TODO Update stats

//    private fun updateStats() {
//        if (statsScheduler.isRunning) {
//            statsScheduler.cancelStatsGathering()
//        }
//        val enableStats = sharedPreferences.getBoolean(
//                Preferences.ENABLE_STATS, Preferences.ENABLE_STATS_DEFAULT)
//        if (enableStats && room != null && room!!.state == Room.State.CONNECTED) {
//            statsScheduler.scheduleStatsGathering(room!!, statsListener(), RoomActivity.STATS_DELAY.toLong())
//        }
//        updateStatsUI(enableStats)
//    }
//
//    private fun statsListener(): StatsListener {
//        return StatsListener { statsReports: List<StatsReport> ->
//            // Running on StatsScheduler thread
//            room?.let { room ->
//                statsListAdapter.updateStatsData(statsReports, room.remoteParticipants,
//                        localVideoTrackNames)
//            }
//        }
//    }
}