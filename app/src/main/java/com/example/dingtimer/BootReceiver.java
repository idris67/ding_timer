package com.example.dingtimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Receiver that runs when the device boots up.
 * Reschedules all saved alarms.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAlarms(context);
        }
    }

    private void rescheduleAlarms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("DingTimerPrefs", Context.MODE_PRIVATE);
        Set<String> alarms = prefs.getStringSet("alarms", new HashSet<>());
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        for (String alarmIdStr : alarms) {
            int alarmId = Integer.parseInt(alarmIdStr);
            int hour = alarmId / 100;
            int minute = alarmId % 100;
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            // Add 60 seconds buffer to ensure alarm is always in the future
            long currentTime = System.currentTimeMillis();
            long alarmTime = calendar.getTimeInMillis();
            
            // If time has passed or is less than 60 seconds away, set for tomorrow
            if (alarmTime <= currentTime + 60000) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}

