package com.maforn.timedshutdown;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.maforn.timedshutdown.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "shutdown_channel";

    public NavController navController;


    /**
     * onCreate will setup the app nav bar to swap between fragments views
     *
     * @param savedInstanceState the previous state of the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.maforn.timedshutdown.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_settings, R.id.navigation_timer, R.id.navigation_schedule, R.id.navigation_info)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        CharSequence name = "Shutdown Channel";
        String description = "Channel for scheduled shutdown notifications";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navController.restoreState(savedInstanceState.getBundle("navControllerState"));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("navControllerState", navController.saveState());
    }

}