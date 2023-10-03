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
                        ScheduleFragment.setSchedule(element, context, alarmManager);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}