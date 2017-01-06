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
    public void scheduleStatsGathering(final @NonNull Room room,
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

        final Runnable statsRunner = new Runnable() {
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
