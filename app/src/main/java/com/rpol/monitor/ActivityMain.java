package com.rpol.monitor;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rpol.monitor.helpers.AlarmReceiver;
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
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager = null;
    private UpdateScheduler updateScheduler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RPoLMonitor", "Creating the activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        updateScheduler = UpdateScheduler.get(this);
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        // Configure refresh
        final SwipeRefreshLayout srlRefreshBoard = findViewById(R.id.srlRefreshBoard);
        srlRefreshBoard.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                trigger_update();
                srlRefreshBoard.setRefreshing(false);
            }
        });

        // Go to login screen if we're not authenticated, updates the boards otherwise
        if (Settings.isLoggedIn()) {
            trigger_update();
        } else {
            Intent myIntent = new Intent(this, ActivitySettings.class);
            startActivity(myIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("RPoLMonitor", "Called pause on ActivityMain");
        updateScheduler.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("RPoLMonitor", "Called resume on ActivityMain");
        if (Settings.isLoggedIn()) {
            startAlarm();
            trigger_update();
            updateScheduler = UpdateScheduler.get(this);
            updateScheduler.start();
        } else {
            cancelAlarm();
        }
    }

    public void startAlarm() {
        if (Settings.isLoggedIn()) {
            // Initialise the scheduler
            Log.d("RPoLMonitor", "Starting alarm");
            int interval = Settings.getUpdate_interval() * 60 * 1000;

            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        }
    }

    public void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("RPoLMonitor", "Stopped alarm");
        }
    }

    public void resetAlarm() {
        cancelAlarm();
        startAlarm();
    }

    public void trigger_update() {
        BoardStatusUpdate status = new BoardStatusUpdate(this);
        status.execute();
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
        Log.d("RPoLMonitor", "Recreating table");

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

        Log.d("RPoLMonitor", "Finished updating boards");
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
}
