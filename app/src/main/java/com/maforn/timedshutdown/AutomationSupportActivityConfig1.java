package com.maforn.timedshutdown;

import static com.maforn.timedshutdown.ui.settings.SettingsFragment.switchConfig;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class AutomationSupportActivityConfig1 extends AppCompatActivity {

    /**
     * This activity is just a support activity for automation apps such as Tasker and Automate,
     * so that when it's called it can automatically start the power off sequence
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPowerOff(getApplicationContext(), "config0", 0);
    }

    public static void startPowerOff(Context context, String configName, int configNumber) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("chosenConfig", configNumber);
        try {
            JSONObject jsonObject = new JSONObject(sharedPreferences.getString(configName, "{power_off_method:0,initial_delay:2000,first_delay:2500,second_delay:2500,third_delay:2500,fourth_delay:2500,X_false:100,Y_false:100,X_true:100,Y_true:100,X_three:100,Y_three:100,X_four:100,Y_four:100,X_ABS_false:100,Y_ABS_false:100,X_ABS_true:100,Y_ABS_true:100,X_ABS_three:100,Y_ABS_three:100,X_ABS_four:100,Y_ABS_four:100}"));
            switchConfig(editor, jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        editor.apply();
        Intent intent = new Intent(context, AccessibilitySupportService.class);
        context.startService(intent);
    }
}