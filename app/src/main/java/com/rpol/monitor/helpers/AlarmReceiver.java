package com.rpol.monitor.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        Log.d("RPoLMonitor", "Received alarm, checking if needs to send notification");
        NotificationsManager.get().check_update(arg0);
    }
}
