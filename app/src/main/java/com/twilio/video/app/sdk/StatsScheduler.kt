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
package com.twilio.video.app.sdk

import android.os.Handler
import android.os.HandlerThread
import com.twilio.video.Room
import com.twilio.video.StatsListener
import timber.log.Timber

class StatsScheduler(private val roomManager: RoomManager, private val room: Room) {
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null
    private val statsListener: StatsListener = StatsListener { statsReports ->
        roomManager.sendStatsUpdate(statsReports)
    }
    private val isRunning: Boolean
        get() = handlerThread?.isAlive ?: false

    fun start() {
        if (isRunning) {
            stop()
        }
        val handlerThread = HandlerThread("StatsSchedulerThread")
        this.handlerThread = handlerThread
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        this.handler = handler
        val statsRunner: Runnable = object : Runnable {
            override fun run() {
                room.getStats(statsListener)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(statsRunner)
        Timber.d("Stats scheduler thread started")
    }

    fun stop() {
        if (isRunning) {
            handlerThread?.let { handlerThread ->
                handlerThread.quit()
                this.handlerThread = null
                handler = null
                Timber.d("Stats scheduler thread closed")
            }
        }
    }
}
