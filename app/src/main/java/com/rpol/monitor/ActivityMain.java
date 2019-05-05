package com.rpol.monitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.rpol.monitor.helpers.BoardItem;
import com.rpol.monitor.helpers.NotificationsManager;
import com.rpol.monitor.helpers.Settings;
import com.rpol.monitor.ui.BoardViewAdapter;
import com.rpol.monitor.network.BoardStatusUpdate;
import com.rpol.monitor.network.UpdateScheduler;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ActivityMain extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private UpdateScheduler updateBoards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("YYY", "Creating the activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        // Initialise the scheduler
        SharedPreferences sp = getSharedPreferences(Settings.PREFS, MODE_PRIVATE);
        Settings.setUpdate_interval(sp.getInt(Settings.PREFS_UPDATE_INTERVAL, 1));

        // Go to login screen if we're not authenticated, updates the boards otherwise
        if (Settings.isLoggedIn()) {
            BoardStatusUpdate status = new BoardStatusUpdate(this);
            status.execute();
        } else {
            Intent myIntent = new Intent(this, ActivitySettings.class);
            startActivity(myIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("XXX", "Called resume on ActivityMain");
        BoardStatusUpdate status = new BoardStatusUpdate(this);
        status.execute();
        updateBoards = UpdateScheduler.get(this);
    }

    // She the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // Decides what to do when the menu is clicked on
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent myIntent = new Intent(this, ActivitySettings.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Updates the display of the boards, checks for new notification
    public void update_boards(List<BoardItem> boards) {
        Log.d("XXX", "Recreating table");

        String msg = MessageFormat.format("Welcome {0}. Here''s your RPoL status.",
                Settings.getNickname());
        ((TextView) findViewById(R.id.tvWelcome)).setText(msg);


        // Update the boards display
        recyclerView = (RecyclerView) findViewById(R.id.rvBoard);
        // use this setting to
        // improve performance if you know that changes
        // in content do not change the layout size
        // of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new BoardViewAdapter(boards);
        recyclerView.setAdapter(mAdapter);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        ((TextView)findViewById(R.id.tvLastUpdate)).setText("Last update:\n" + strDate);

        // Check for new notifications
        Log.d("XXX", "Updating notification manager");
        if (NotificationsManager.get().update_notification(boards)) {
            send_notification(NotificationsManager.ACTIVE_BOARD_CHANNEL,
                              NotificationsManager.get().get_message());
        }

    }

    // Defines how notifications should be sent on the phone by the application
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Settings.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Creates and sends the notifications
    public void send_notification(int notification_id, String msg) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Settings.CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("New Activity on RPoL")
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(notification_id, builder.build());

    }
}
