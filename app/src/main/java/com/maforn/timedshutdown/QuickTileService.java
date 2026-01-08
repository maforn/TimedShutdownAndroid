package com.maforn.timedshutdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.maforn.timedshutdown.ui.timer.TimerFragment;

public class QuickTileService extends TileService {

    SharedPreferences sP;

    // Called when your app can update your tile.
    @Override
    public void onStartListening() {
        super.onStartListening();
        sP = getSharedPreferences("Timer", MODE_PRIVATE);
        boolean isActive = sP.getBoolean("timerActive", false);

        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(isActive ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        Tile tile = getQsTile();

        if (tile.getState() == Tile.STATE_INACTIVE) {
            // Start the timer
            tile.setState(Tile.STATE_ACTIVE);
            sendBroadcast(new Intent(TimerFragment.ACTION_START_TIMER));
            Toast.makeText(this, R.string.quick_tile_start, Toast.LENGTH_SHORT).show();
        } else {
            // Stop the timer
            tile.setState(Tile.STATE_INACTIVE);
            sendBroadcast(new Intent(TimerFragment.ACTION_STOP_TIMER));
            Toast.makeText(this, R.string.quick_tile_stop, Toast.LENGTH_SHORT).show();
        }

        tile.updateTile();
        super.onClick();
    }
}
