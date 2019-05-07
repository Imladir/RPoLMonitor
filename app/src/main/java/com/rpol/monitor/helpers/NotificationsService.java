package com.rpol.monitor.helpers;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.rpol.monitor.ActivityMain;
import com.rpol.monitor.R;
import com.rpol.monitor.network.BoardStatusUpdate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Handles notifications
public class NotificationsService extends IntentService {
    public static final int ACTIVE_BOARD_CHANNEL = 0;

    public NotificationsService() {
        super("RPoLManager Update Service");
        Log.d("XXX", "Creating service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("PRPoLMonitor", "Received alarm, checking for new activity");
        if (Settings.isLoggedIn()) {
            BoardStatusUpdate status = new BoardStatusUpdate(this);
            status.execute();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void update_notification(List<BoardItem> boards) {
        BoardStatus boardsStatus = BoardStatus.get();
        boardsStatus.update_status(boards);

        if (boardsStatus.hasNew_activity())
            send_notification(boardsStatus.getMessage());
    }

    // Creates and sends the notifications
    public void send_notification(String message) {
        Log.d("RPoLMonitor", "Sending notification");
        Context context = getApplicationContext();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, ActivityMain.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Settings.CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("New Activity on RPoL")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(ACTIVE_BOARD_CHANNEL, builder.build());

    }
}
