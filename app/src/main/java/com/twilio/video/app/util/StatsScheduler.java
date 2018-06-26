/*
 * Copyright (C) 2017 Twilio, Inc.
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

package com.twilio.video.app.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import com.twilio.video.Room;
import com.twilio.video.StatsListener;

public class StatsScheduler {
  private HandlerThread handlerThread;
  private Handler handler;

  public StatsScheduler() {}

  // Listener will be called from scheduler thread
  public void scheduleStatsGathering(
      final @NonNull Room room,
      final @NonNull StatsListener listener,
      final long delayInMilliseconds) {
    if (room == null) {
      throw new NullPointerException("Room must not be null");
    }
    if (listener == null) {
      throw new NullPointerException("StatsListener must not be null");
    }
    if (isRunning()) {
      cancelStatsGathering();
    }
    this.handlerThread = new HandlerThread("StatsSchedulerThread");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());

    final Runnable statsRunner =
        new Runnable() {
          @Override
          public void run() {
            room.getStats(listener);
            handler.postDelayed(this, delayInMilliseconds);
          }
        };
    handler.post(statsRunner);
  }

  public boolean isRunning() {
    return (handlerThread != null && handlerThread.isAlive());
  }

  public void cancelStatsGathering() {
    if (handlerThread != null && handlerThread.isAlive()) {
      handlerThread.quit();
      handlerThread = null;
    }
    handler = null;
  }
}
