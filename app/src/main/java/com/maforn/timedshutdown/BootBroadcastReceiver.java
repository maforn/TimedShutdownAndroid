package com.maforn.timedshutdown;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.maforn.timedshutdown.ui.schedule.ScheduleFragment;

import org.json.JSONArray;
import org.json.JSONObject;

public class BootBroadcastReceiver extends BroadcastReceiver {
    /**
     * This function will be receive an intent by the OS when it powers on and, on boot completed,
     * it will get all the scheduled power offs, parse them from the json saved in SharedPreferences
     * and then set up an Alarm that will trigger the power off sequence at the specified time
     *
     * @param context the app context
     * @param intent  an intent containing information on boot status
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences sP = context.getSharedPreferences("Schedule", MODE_PRIVATE);
            if (sP.contains("schedules")) {
                try {
                    JSONObject jsonObject = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                    JSONArray schedules = jsonObject.getJSONArray("schedules");

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    for (int i = 0; i < schedules.length(); i++) {
                        JSONObject element = schedules.getJSONObject(i);
                        // setting up the alarm is delegated to the already existing function in ScheduleFragment
                        ScheduleFragment.setSchedule(element, context, alarmManager);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            NotificationHelper.createShutdownNotification(context);
        }
    }
}