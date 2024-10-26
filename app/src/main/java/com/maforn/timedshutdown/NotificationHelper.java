package com.maforn.timedshutdown;

import static android.content.Context.MODE_PRIVATE;
import static com.maforn.timedshutdown.ui.schedule.ScheduleFragment.JSonArray2IntArray;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationHelper {
    private static final String CHANNEL_ID = "shutdown_channel";

    public static void showShutdownNotification(Context context, Calendar alarmCalendar) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Scheduled Shutdown")
                .setContentText("A shutdown is scheduled for " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(alarmCalendar.getTime()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true); // Makes the notification permanent

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    public static void cancelShutdownNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    public static void createShutdownNotification(Context context) {
        cancelShutdownNotification(context);

        SharedPreferences sP = context.getSharedPreferences("Schedule", MODE_PRIVATE);
        if (sP.contains("schedules") && sP.getBoolean("notifications", false)) {
            try {
                JSONObject jsonObject = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                JSONArray schedules = jsonObject.getJSONArray("schedules");

                Calendar closestAlarm = null;

                for (int i = 0; i < schedules.length(); i++) {
                    JSONObject element = schedules.getJSONObject(i);
                    boolean checked = element.getBoolean("active");

                    if (checked) {
                        int hour = element.getInt("hour");
                        int minute = element.getInt("minute");
                        int[] repeating = JSonArray2IntArray(element.getJSONArray("repeating"));

                        Calendar alarmCalendar = Calendar.getInstance();
                        alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
                        alarmCalendar.set(Calendar.MINUTE, minute);
                        alarmCalendar.set(Calendar.SECOND, 0);
                        alarmCalendar.set(Calendar.MILLISECOND, 0);
                        // if the date has already passed set it for tomorrow
                        if (alarmCalendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
                            alarmCalendar.add(Calendar.HOUR_OF_DAY, 24);

                        if (repeating.length != 0) {
                            for (int day : repeating) {
                                Calendar tempCalendar = (Calendar) alarmCalendar.clone();
                                tempCalendar.set(Calendar.DAY_OF_WEEK, day);
                                if (tempCalendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
                                    tempCalendar.add(Calendar.HOUR_OF_DAY, 24 * 7);
                                if (closestAlarm == null || tempCalendar.getTimeInMillis() < closestAlarm.getTimeInMillis()) {
                                    closestAlarm = tempCalendar;
                                }
                            }
                        } else {
                            if (closestAlarm == null || alarmCalendar.getTimeInMillis() < closestAlarm.getTimeInMillis()) {
                                closestAlarm = alarmCalendar;
                            }
                        }
                    }
                }

                if (closestAlarm != null) {
                    showShutdownNotification(context, closestAlarm);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
