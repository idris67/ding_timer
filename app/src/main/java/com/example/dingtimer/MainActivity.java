package com.example.dingtimer;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Main activity for the Ding Timer app.
 * Allows users to set multiple times for ding sounds to play automatically.
 */
public class MainActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private Button setButton;
    private Button cancelAllButton;
    private LinearLayout scheduledList;
    private TextView emptyMessage;
    private AlarmManager alarmManager;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.timePicker);
        setButton = findViewById(R.id.setButton);
        cancelAllButton = findViewById(R.id.cancelAllButton);
        scheduledList = findViewById(R.id.scheduledList);
        emptyMessage = findViewById(R.id.emptyMessage);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        prefs = getSharedPreferences("DingTimerPrefs", MODE_PRIVATE);

        // Request notification permission on Android 13+
        requestNotificationPermission();

        setButton.setOnClickListener(v -> addAlarm());
        cancelAllButton.setOnClickListener(v -> cancelAllAlarms());

        // Display existing alarms
        updateAlarmList();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void addAlarm() {
        // Check if we have permission to schedule exact alarms (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please allow exact alarm permission in settings", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        
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

        // Generate unique ID based on time
        int alarmId = hour * 100 + minute;
        
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        // Save alarm ID to preferences
        Set<String> alarms = prefs.getStringSet("alarms", new HashSet<>());
        alarms = new HashSet<>(alarms); // Create mutable copy
        alarms.add(String.valueOf(alarmId));
        prefs.edit().putStringSet("alarms", alarms).apply();

        String timeStr = String.format("%02d:%02d", hour, minute);
        
        // Check if it's for tomorrow
        Calendar now = Calendar.getInstance();
        String dayInfo = "";
        if (calendar.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)) {
            dayInfo = " (tomorrow)";
        } else {
            dayInfo = " (today)";
        }
        
        Toast.makeText(this, "Ding added for " + timeStr + dayInfo, Toast.LENGTH_LONG).show();

        updateAlarmList();
    }

    private void cancelAlarm(int alarmId) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);

        // Remove from preferences
        Set<String> alarms = prefs.getStringSet("alarms", new HashSet<>());
        alarms = new HashSet<>(alarms);
        alarms.remove(String.valueOf(alarmId));
        prefs.edit().putStringSet("alarms", alarms).apply();

        updateAlarmList();
        Toast.makeText(this, "Ding cancelled", Toast.LENGTH_SHORT).show();
    }

    private void cancelAllAlarms() {
        Set<String> alarms = prefs.getStringSet("alarms", new HashSet<>());
        
        for (String alarmIdStr : alarms) {
            int alarmId = Integer.parseInt(alarmIdStr);
            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        }

        prefs.edit().putStringSet("alarms", new HashSet<>()).apply();
        updateAlarmList();
        Toast.makeText(this, "All dings cancelled", Toast.LENGTH_SHORT).show();
    }

    private void updateAlarmList() {
        scheduledList.removeAllViews();
        Set<String> alarms = prefs.getStringSet("alarms", new HashSet<>());

        if (alarms.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            
            for (String alarmIdStr : alarms) {
                int alarmId = Integer.parseInt(alarmIdStr);
                int hour = alarmId / 100;
                int minute = alarmId % 100;
                
                String timeStr = String.format("%02d:%02d", hour, minute);
                
                // Create alarm item view
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(20, 15, 20, 15);
                itemLayout.setBackgroundColor(Color.parseColor("#2196F3"));
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 10);
                itemLayout.setLayoutParams(params);
                
                TextView timeText = new TextView(this);
                timeText.setText("⏰ " + timeStr);
                timeText.setTextSize(18);
                timeText.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                );
                timeText.setLayoutParams(textParams);
                
                Button deleteButton = new Button(this);
                deleteButton.setText("Delete");
                deleteButton.setTextColor(Color.WHITE);
                deleteButton.setBackgroundColor(Color.parseColor("#f44336"));
                deleteButton.setOnClickListener(v -> cancelAlarm(alarmId));
                
                itemLayout.addView(timeText);
                itemLayout.addView(deleteButton);
                scheduledList.addView(itemLayout);
            }
        }
    }
}

