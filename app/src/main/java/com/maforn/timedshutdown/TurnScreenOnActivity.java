package com.maforn.timedshutdown;

import android.app.Activity;
import android.os.Handler;

public class TurnScreenOnActivity extends Activity {
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn on the screen and show the activity on top of the lock screen
        setTurnScreenOn(true);
        setShowWhenLocked(true);
        if (getIntent().getBooleanExtra("isPortait", true)) {
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // Post a delayed runnable to the main thread
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Call finish() after a delay
                finish();
            }
        }, getIntent().getLongExtra("time", 500)); // Delay in milliseconds
    }
}
