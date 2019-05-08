package com.rpol.monitor.events;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.rpol.monitor.ActivityMain;
import com.rpol.monitor.R;
import com.rpol.monitor.helpers.BoardStatus;
import com.rpol.monitor.helpers.Settings;
import com.rpol.monitor.network.HomepageParser;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BoardStatusMonitor extends Service implements BoardStatusListener {
    public static final int FOREGROUND_SERVICE = 101;
    private ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> futureUpdate;
    private int currentInterval = -1;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create the scheduler, updates the boards status at regular intervals
        futureUpdate = scheduler.scheduleAtFixedRate(new Runnable() {
                                                         @Override
                                                         public void run() {
                                                             Log.d("RPoLMonitor", "Running board update from service");
                                                             HomepageParser parser = new HomepageParser();
                                                             parser.execute();
                                                         }
                                                     },
                Settings.getUpdate_interval(),
                Settings.getUpdate_interval(),
                TimeUnit.MINUTES);
        currentInterval = Settings.getUpdate_interval();

        BoardStatus.get().addUpdateListener(this);

        Log.d("RPoLMonitor", "Starting service");

        setForegroundNotification(BoardStatus.NOTHING_NEW);

        return START_STICKY;
    }

    private void setForegroundNotification(String message) {
        Notification notif = createNotification(BoardStatus.NOTHING_NEW);
        startForeground(BoardStatusMonitor.FOREGROUND_SERVICE, notif);
    }

    private void updateNotification(boolean stealthyUpdate, String message) {
        if (!stealthyUpdate)
            destroyCurrentNotification();
        Notification notif = createNotification(message);

        Log.d("RPoLMonitor", "Sending new notification");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(FOREGROUND_SERVICE, notif);
    }

    private void destroyCurrentNotification() {
        // Remove previous notification if it exists
        Log.d("RPoLMonitor", "Destroying previous notification");
        stopForeground(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(FOREGROUND_SERVICE);
    }

    private Notification createNotification(String message) {
        // Create the new notification
        Intent notificationIntent = new Intent(this, ActivityMain.class);
        notificationIntent.setAction("MainAndOnlyAction");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Settings.CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("RPoL Monitor")
                .setTicker("RPoL Monitor")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(Settings.CHANNEL_ID);
        }

        return builder.build();
    }

    @Override
    public void onBoardUpdate(boolean newActivity) {
        Log.d("RPoLMonitor", "Received update event in service with newActivity = " + newActivity);
        updateNotification(!newActivity, BoardStatus.get().getMessage());
    }

    // Initialises the scheduler and starts it
    public void start() {
        if (Settings.isLoggedIn()) {
            Log.d("RPoLMonitor", "Starting scheduler");
            futureUpdate = scheduler.scheduleAtFixedRate(new Runnable() {
                                                             @Override
                                                             public void run() {
                                                                 Log.d("RPoLMonitor", "Running board update from service");
                                                                 HomepageParser parser = new HomepageParser();
                                                                 parser.execute();
                                                             }
                                                         },
                    Settings.getUpdate_interval(),
                    Settings.getUpdate_interval(),
                    TimeUnit.MINUTES);
        }
    }

    // Stops the scheduler
    public void stop() {
        if (futureUpdate != null) {
            Log.d("RPoLMonitor", "Stopping service");
            futureUpdate.cancel(false);
        }
    }

    // Stops the scheduler, clears tasks, then recreates it with a new update interval
    public void check_interval() {
        Log.d("RPoLMonitor", "Updating scheduler. Old value = " + currentInterval + " | New value = " +  + Settings.getUpdate_interval());
        if (!Settings.isLoggedIn()) {
            stop();
        } else if (currentInterval != Settings.getUpdate_interval()) {
            // Reset interval
            stop();
            start();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }
}
