package com.maforn.timedshutdown;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.maforn.timedshutdown.ui.schedule.ScheduleFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccessibilitySupportService extends Service {

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        // start the power off service

        // if it was called by a single time alarm, deactivate it
        if (paramIntent.getIntExtra("id", -1) != -1) {
            int id = paramIntent.getIntExtra("id", 0);
            SharedPreferences sP = getApplicationContext().getSharedPreferences("Schedule", MODE_PRIVATE);
            JSONObject jO;
            JSONArray arr;
            try {
                jO = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                arr = jO.getJSONArray("schedules");
                int index = ScheduleFragment.getIdIndex(arr, id);
                arr.put(index, arr.getJSONObject(index).put("active", false));
                sP.edit().putString("schedules", jO.toString()).apply();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        // acquire wake lock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "timed-shutdown:wl");

        wakeLock.acquire(10 * 1000L /*10 seconds*/);

        Toast.makeText(this, "Executing actions to shut down the phone in 2 sec...", Toast.LENGTH_SHORT).show();

        Handler baseHandler = new Handler();
        baseHandler.postDelayed(() -> {
            // call the power off function
            Intent intent = new Intent(getApplicationContext(), AccessibilityService.class);
            getApplicationContext().startService(intent);
            // use an handler to wait 2.5 sec and then start the power off sequence
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent intent12 = new Intent(getApplicationContext(), AccessibilityService.class);
                intent12.putExtra("exec_gesture", true);
                getApplicationContext().startService(intent12);
                // handler added for the second click option
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
                int power_off_type = sharedPreferences.getInt("power_off_method", 0);
                if (power_off_type == 2) {
                    Handler handler1 = new Handler();
                    handler1.postDelayed(() -> {
                        Intent intent1 = new Intent(getApplicationContext(), AccessibilityService.class);
                        intent1.putExtra("exec_gesture2", true);
                        getApplicationContext().startService(intent1);
                    }, 2500);
                }
            }, 2500);
        }, 2000);

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
