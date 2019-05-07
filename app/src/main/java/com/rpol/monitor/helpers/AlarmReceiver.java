package com.rpol.monitor.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RPoLMonitor", "Received alarm, creating service");
        Intent notificationChecker = new Intent(context, NotificationsService.class);
        context.startService(notificationChecker);
    }
}
