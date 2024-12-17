package com.maforn.timedshutdown;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AutomationSupportActivityLastUsed extends AppCompatActivity {

    /**
     * This activity is just a support activity for automation apps such as Tasker and Automate,
     * so that when it's called it can automatically start the power off sequence
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getApplicationContext(), AccessibilitySupportService.class);
        getApplicationContext().startService(intent);
    }
}