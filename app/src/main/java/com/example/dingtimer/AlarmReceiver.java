package com.example.dingtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver that triggers when the scheduled alarm time arrives.
 * Starts the DingService to play the sound.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the service to play the ding sound
        Intent serviceIntent = new Intent(context, DingService.class);
        context.startService(serviceIntent);
    }
}

