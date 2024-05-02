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
