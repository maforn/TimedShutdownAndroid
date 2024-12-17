package com.maforn.timedshutdown;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.maforn.timedshutdown.ui.schedule.ScheduleFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccessibilitySupportService extends Service {

    /**
     * This Service will start the power off of the device by checking the required power off method,
     * acquiring a wakelock and sending intent with the required delays to the AccessibilityService.
     * It will also deactivate the schedule if it was not a repeating type.
     *
     * @param paramIntent the Intent that started this Service
     */
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        // start the power off service

        // if it was called by a single time alarm, deactivate it
        if (paramIntent.hasExtra("id")) {
            int id = paramIntent.getIntExtra("id", 0);
            SharedPreferences sP = getApplicationContext().getSharedPreferences("Schedule", MODE_PRIVATE);
            JSONObject jO;
            JSONArray arr;
            // get the alarm from schedules array and put its active status from true to false
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

        // get all the delays
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        Log.d("CONFIG AccessibilitySupportService", "onStartCommand: " + sharedPreferences.getAll());
        int initialDelay = sharedPreferences.getInt("initial_delay", 2000);
        int firstDelay = sharedPreferences.getInt("first_delay", 2500);
        int secondDelay = sharedPreferences.getInt("second_delay", 2500);
        int thirdDelay = sharedPreferences.getInt("third_delay", 2500);
        int fourthDelay = sharedPreferences.getInt("fourth_delay", 2500);

        // start the TurnScreenOnActivity to wake the device
        Intent wakeIntent = new Intent(getApplicationContext(), TurnScreenOnActivity.class);
        wakeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // add the time to wait before finishing the wake activity
        wakeIntent.putExtra("time", 1000L + initialDelay + firstDelay + secondDelay);
        getApplicationContext().startActivity(wakeIntent);

        // send a Toast to the user to let him know the shutdown is happening
        Toast.makeText(this, "Shutting down in " + initialDelay + " milli sec...", Toast.LENGTH_SHORT).show();

        // set off a series of Handler to delay gestures as requested
        Handler baseHandler = new Handler();
        baseHandler.postDelayed(() -> {
            // call the power off display
            Intent intent = new Intent(getApplicationContext(), AccessibilityService.class);
            getApplicationContext().startService(intent);
            // use an handler to wait the required time and then start the power off sequence
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                // call the first gesture
                Intent intent12 = new Intent(getApplicationContext(), AccessibilityService.class);
                intent12.putExtra("exec_gesture", true);
                getApplicationContext().startService(intent12);
                // handler added for the second click option
                PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];
                if (power_off_type == PowerOffType.TWOCLICKS || power_off_type == PowerOffType.THREECLICKS || power_off_type == PowerOffType.FOURCLICKS) {
                    Handler handler1 = new Handler();
                    handler1.postDelayed(() -> {
                        // call for the second click
                        Intent intent1 = new Intent(getApplicationContext(), AccessibilityService.class);
                        intent1.putExtra("exec_gesture2", true);
                        getApplicationContext().startService(intent1);

                        if (power_off_type == PowerOffType.THREECLICKS || power_off_type == PowerOffType.FOURCLICKS) {
                            Handler handler2 = new Handler();
                            handler2.postDelayed(() -> {
                                // call for the second click
                                Intent intent2 = new Intent(getApplicationContext(), AccessibilityService.class);
                                intent2.putExtra("exec_gesture3", true);
                                getApplicationContext().startService(intent2);

                                if (power_off_type == PowerOffType.FOURCLICKS) {
                                    Handler handler3 = new Handler();
                                    handler3.postDelayed(() -> {
                                        // call for the second click
                                        Intent intent3 = new Intent(getApplicationContext(), AccessibilityService.class);
                                        intent3.putExtra("exec_gesture4", true);
                                        getApplicationContext().startService(intent3);
                                    }, fourthDelay);
                                }
                            }, thirdDelay);
                        }
                    }, secondDelay);
                }

                NotificationHelper.createShutdownNotification(getApplicationContext());
            }, firstDelay);
        }, initialDelay);

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
