package com.rpol.monitor.network;

import android.util.Log;

import com.rpol.monitor.ActivityMain;
import com.rpol.monitor.helpers.Settings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UpdateScheduler implements Runnable {
    private static UpdateScheduler instance = null;
    private ActivityMain parent;
    private ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> futureUpdate;

    private UpdateScheduler(ActivityMain parent) {
        this.parent = parent;
    }

    // Singleton
    public static UpdateScheduler get(ActivityMain parent) {
        if (instance == null) {
            instance = new UpdateScheduler(parent);
        }
        return instance;
    }

    // Initialises the scheduler and starts it
    public void start() {
        if (Settings.isLoggedIn()) {
            Log.d("XXX", "Starting scheduler");
            futureUpdate = scheduler.scheduleAtFixedRate(this,
                    Settings.getUpdate_interval(),
                    Settings.getUpdate_interval(),
                    TimeUnit.MINUTES);
        }
    }

    // Stops the scheduler (on log out)
    public void stop() {
        if (futureUpdate != null) {
            Log.d("XXX", "Stopping scheduler");
            futureUpdate.cancel(false);
        }
    }

    // Stops the scheduler, clears tasks, then recreates it with a new update interval
    public void update_interval(int new_interval) {
        Log.d("XXX", "Updating scheduler. Old value = " + Settings.getUpdate_interval() + " | New value = " + new_interval);
        if (new_interval != Settings.getUpdate_interval()) {
            stop();
            Settings.setUpdate_interval(new_interval);
            start();
        }
    }

    // Performs the boards update
    public void run() {
        Log.d("XXX", "Running board update from scheduler");
        BoardStatusUpdate status = new BoardStatusUpdate(parent);
        status.execute();
    }
}
