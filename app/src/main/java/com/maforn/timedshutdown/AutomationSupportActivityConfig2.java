package com.maforn.timedshutdown;

import static com.maforn.timedshutdown.AutomationSupportActivityConfig1.startPowerOff;
import static com.maforn.timedshutdown.ui.settings.SettingsFragment.switchConfig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class AutomationSupportActivityConfig2 extends AppCompatActivity {

    /**
     * This activity is just a support activity for automation apps such as Tasker and Automate,
     * so that when it's called it can automatically start the power off sequence
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPowerOff(getApplicationContext(), "config1", 1);
    }
}