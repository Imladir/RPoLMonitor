package com.rpol.monitor.helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
public class NotificationsManager {
    private static NotificationsManager instance = null;

    private Context context = null;
    private Map<Integer, BoardItem.Status> active_boards;
    public static final int ACTIVE_BOARD_CHANNEL = 0;

    private NotificationsManager() {
        this.active_boards = new HashMap<>();
        Log.d("RPoLMonitor", "New notification manager");
    }

    public static NotificationsManager get() {
        if (instance == null)
            instance = new NotificationsManager();
        return instance;
    }

    public void check_update(Context context) {
        this.context = context;
        BoardStatusUpdate status = new BoardStatusUpdate(this);
        status.execute();
    }

    // Checks if there is some new (interesting) activity on RPoL
    // Crafts the notification message that should be sent if there's something new
    public void update_notification(List<BoardItem> boards) {
        boolean new_activity = false;
        String message = "";
        for (BoardItem board : boards) {
            // We read the messages here, the board is not new anymore
            if (board.getStatus() == BoardItem.Status.Read && active_boards.containsKey(board.getGid())) {
                active_boards.remove(board.getGid());
            }
            // There's some new acitivity here
            else if (board.getStatus() != BoardItem.Status.Read) {
                if (!active_boards.containsKey(board.getGid())
                        || (active_boards.get(board.getGid()) != board.getStatus())) {
                    new_activity = true;
                    Log.d("RPoLMonitor", "Board " + board.getName() + "is " + board.getStatus());
                    active_boards.put(board.getGid(), board.getStatus());
                }
            }
            // It might not be new since the last notification, but we didn't check it yet
            // so it still needs to appear in the updated notification
            if (board.getStatus() == BoardItem.Status.NewMessage)
                message += "You have new message(s) on " + board.getName() + "\n";
            else if (board.getStatus() == BoardItem.Status.NewPM)
                message += "You have new PM(s) on " + board.getName() + "\n";
            else if (board.getStatus() == BoardItem.Status.NewMessageAndPM)
                message += "You have new Message(s) and PM(s) on " + board.getName() + "\n";
        }
        Log.d("RPoLMonitor", "Detected new activity: " + new_activity);
        if (new_activity)
            send_notification(message);
    }

    // Creates and sends the notifications
    public void send_notification(String message) {
        Log.d("RPoLMonitor", "Sending notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, ActivityMain.class), 0);


        // Create an explicit intent for an Activity in your app
        // Intent intent = new Intent(this, ActivityMain.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

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
