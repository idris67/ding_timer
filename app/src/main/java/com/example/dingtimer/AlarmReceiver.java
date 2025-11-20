package com.example.dingtimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

/**
 * BroadcastReceiver that triggers when the scheduled alarm time arrives.
 * Starts the DingService to play the sound and reschedules the alarm for tomorrow.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the service to play the ding sound
        Intent serviceIntent = new Intent(context, DingService.class);
        context.startService(serviceIntent);
        
        // Get the alarm ID to reschedule for tomorrow
        int alarmId = intent.getIntExtra("alarmId", -1);
        if (alarmId != -1) {
            rescheduleAlarm(context, alarmId);
        }
    }
    
    private void rescheduleAlarm(Context context, int alarmId) {
        int hour = alarmId / 100;
        int minute = alarmId % 100;
        
        // Schedule for tomorrow at the same time
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmId", alarmId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // Use setExactAndAllowWhileIdle for more reliable alarms even in doze mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}

