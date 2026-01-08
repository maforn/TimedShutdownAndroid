package com.maforn.timedshutdown.ui.timer;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.POWER_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.maforn.timedshutdown.AccessibilityService;
import com.maforn.timedshutdown.AccessibilitySupportService;
import com.maforn.timedshutdown.FullscreenActivity;
import com.maforn.timedshutdown.QuickTileService;
import com.maforn.timedshutdown.R;
import com.maforn.timedshutdown.databinding.FragmentTimerBinding;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;

    private int counter = 60;

    private boolean isTiming = false;
    TextView timerText;

    CountDownTimer countDownTimer = null;

    NumberPicker numberPickerSec, numberPickerMin, numberPickerHour;

    SharedPreferences sP;

    public static final String ACTION_START_TIMER = "com.maforn.timedshutdown.action.START_TIMER";
    public static final String ACTION_STOP_TIMER = "com.maforn.timedshutdown.action.STOP_TIMER";


    /**
     * This function will start/stop the timer mechanism from an external call (TileService)
     */
    private final BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_START_TIMER.equals(intent.getAction())) {
                if (!isTiming) {
                    // Recover last timer value for remote start
                    counter = sP.getInt("lastCounter", 60);
                    timerText.setText(String.format("%02d:%02d:%02d", counter / 3600, (counter % 3600) / 60, counter % 60));
                    numberPickerSec.setValue(counter % 60);
                    numberPickerMin.setValue((counter % 3600) / 60);
                    numberPickerHour.setValue(counter / 3600);
                    startTimerMechanism();
                }
            } else if (ACTION_STOP_TIMER.equals(intent.getAction())) {
                stopTimerMechanism();
            }
        }
    };

    /**
     * This function will set the global counter variable, update the db value and set the starting
     * text on the displayed textView
     * @param seconds for the counter
     * @param minutes for the counter
     * @param hours for the counter
     */
    @SuppressLint("DefaultLocale")
    private void setCounter(int seconds, int minutes, int hours) {
        counter = seconds + 60 * minutes + 3600 * hours;
        sP.edit().putInt("lastCounter", counter).apply();
        timerText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    @SuppressLint("DefaultLocale")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // bind the fragment to the main navigation activity
        binding = FragmentTimerBinding.inflate(inflater, container, false);
        sP = requireContext().getSharedPreferences("Timer", MODE_PRIVATE);

        // add intent filter for messages from tile service for quick start/stop
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_START_TIMER);
        filter.addAction(ACTION_STOP_TIMER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(timerReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(timerReceiver, filter);
        }

        // if it's the first time show the initial dialog: set the gesture in the settings tab
        if (!sP.contains("firstTime")) {
            AlertDialog alertDialog = (new AlertDialog.Builder(getContext())).create();
            alertDialog.setTitle(getString(R.string.title_settings));
            alertDialog.setMessage(getString(R.string.alert_settings_text));
            alertDialog.setButton(-3, getString(R.string.title_settings), (paramDialogInterface, paramInt) -> {
                try {
                    Navigation.findNavController(container).navigate(R.id.action_timerFragment_to_settingsFragment);
                } catch (Exception ignored) {
                }
                sP.edit().putBoolean("firstTime", false).apply();
            });
            alertDialog.setButton(-2, "VIDEO", (paramDialogInterface, paramInt) -> {
                try {
                    String videoPath = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.screen_record;
                    Intent intent = new Intent(getContext(), FullscreenActivity.class);
                    intent.putExtra("videoPath", videoPath);
                    startActivity(intent);
                } catch (Exception ignored) {
                }
            });
            alertDialog.setButton(-1, getString(R.string.title_info), (paramDialogInterface, paramInt) -> {
                try {
                    Navigation.findNavController(container).navigate(R.id.action_timerFragment_to_infoFragment);
                } catch (Exception ignored) {
                }
                sP.edit().putBoolean("firstTime", false).apply();
            });
            alertDialog.setOnDismissListener(dialog -> sP.edit().putBoolean("firstTime", false).apply());
            alertDialog.show();
        }

        if (!sP.contains("firstTimeSinceUpdate2.85")) {
            AlertDialog alertDialog = (new AlertDialog.Builder(getContext())).create();
            alertDialog.setTitle("Update Notice");
            alertDialog.setMessage("This update changed the way configurations are stored, please reset configs and set them again in the settings tab if you used them. Also, the automation activities have been expanded.");
            alertDialog.setButton(-3, getString(R.string.title_settings), (paramDialogInterface, paramInt) -> {
                try {
                    Navigation.findNavController(container).navigate(R.id.action_timerFragment_to_settingsFragment);
                } catch (Exception ignored) {
                }
                sP.edit().putBoolean("firstTimeSinceUpdate2.85", false).apply();
            });
            alertDialog.setButton(-1, getString(R.string.title_info), (paramDialogInterface, paramInt) -> {
                try {
                    Navigation.findNavController(container).navigate(R.id.action_timerFragment_to_infoFragment);
                } catch (Exception ignored) {
                }
                sP.edit().putBoolean("firstTimeSinceUpdate2.85", false).apply();
            });
            alertDialog.setOnDismissListener(dialog -> sP.edit().putBoolean("firstTimeSinceUpdate2.85", false).apply());
            alertDialog.show();
        }

        // if the accessibility permission is not given require it
        if (!AccessibilityService.isAccessibilityServiceEnabled(requireContext(), AccessibilityService.class)) {
            AccessibilityService.requireAccessibility(getContext());
        }

        // check for battery optimization options, in case it is needed require it
        String packageName = requireContext().getPackageName();
        PowerManager pm = (PowerManager) requireContext().getSystemService(POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            AlertDialog alertDialog = (new AlertDialog.Builder(getContext())).create();
            alertDialog.setTitle(getString(R.string.battery_optimization));
            alertDialog.setMessage(getString(R.string.battery_optimization_toast));
            alertDialog.setButton(-3, getString(R.string.title_settings), (paramDialogInterface, paramInt) -> {
                try {
                    Intent intent = new Intent();
                    Toast.makeText(requireContext(), "Battery optimization -> All apps -> " + getString(R.string.app_name) + " -> Don't optimize", Toast.LENGTH_LONG).show();
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                } catch (Exception ignored) {
                }
                paramDialogInterface.dismiss();
            });
            alertDialog.setButton(-1, getString(R.string.alert_permission_cancel), (paramDialogInterface, paramInt) -> paramDialogInterface.dismiss());
            alertDialog.show();
        }

        // set up all three number pickers with min and max values, as well as a change listener
        numberPickerSec = binding.numberPickerSec;
        numberPickerSec.setMinValue(0);
        numberPickerSec.setMaxValue(59);
        numberPickerSec.setWrapSelectorWheel(true);
        // if the timer is not already ticking on value changed set the new counter
        numberPickerSec.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (!isTiming) {
                setCounter(newVal, numberPickerMin.getValue(), numberPickerHour.getValue());
            }
        });

        numberPickerMin = binding.numberPickerMin;
        numberPickerMin.setMinValue(0);
        numberPickerMin.setMaxValue(59);
        numberPickerMin.setWrapSelectorWheel(true);
        numberPickerMin.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (!isTiming) {
                setCounter(numberPickerSec.getValue(), newVal, numberPickerHour.getValue());
            }
        });

        numberPickerHour = binding.numberPickerHour;
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);
        numberPickerHour.setWrapSelectorWheel(true);
        numberPickerHour.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (!isTiming) {
                setCounter(numberPickerSec.getValue(), numberPickerMin.getValue(), newVal);
            }
        });

        timerText = binding.timerText;

        // on start button click
        binding.buttonStart.setOnClickListener(v -> startTimerMechanism());

        // the stop button will block the countdown timer
        binding.buttonStop.setOnClickListener(v -> stopTimerMechanism());

        // the last button will recover the last timer set by the user
        binding.buttonLastTimer.setOnClickListener(view -> {
            if (!isTiming) {
                counter = sP.getInt("lastCounter", 0);
                timerText.setText(String.format("%02d:%02d:%02d", counter / 3600, (counter % 3600) / 60, counter % 60));
                numberPickerSec.setValue(counter % 60);
                numberPickerMin.setValue((counter % 3600) / 60);
                numberPickerHour.setValue(counter / 3600);
            }
        });

        return binding.getRoot();
    }

    /**
     * Start the timer mechanism if not already running
     */
    private void startTimerMechanism() {
        if (!isTiming && getContext() != null) {
            // try to keep the screen on and permission to show when locked, it will not work otherwise
            ((Activity) requireContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);

            isTiming = true;
            updateTileState(true);

            // start the countdown timer
            countDownTimer = new CountDownTimer(counter * 1000L, 1000) {
                // decrease on each tick
                public void onTick(long millisUntilFinished) {
                    timerText.setText(String.format("%02d:%02d:%02d", counter / 3600, (counter % 3600) / 60, counter % 60));
                    counter--;
                }

                /**
                 * On finish even call the shutdown process
                 */
                @SuppressLint("SetTextI18n")
                public void onFinish() {
                    // if the app was not forcefully terminated and the context still exists
                    if (getContext() != null) {
                        timerText.setText("00:00:00");
                        // call the power off service
                        Intent intent = new Intent(getContext(), AccessibilitySupportService.class);
                        requireContext().startService(intent);

                        isTiming = false;
                        updateTileState(false);
                    }
                }
            }.start();
        }
    }

    /**
     * Stop the timer mechanism if running
     */
    private void stopTimerMechanism() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isTiming = false;
        updateTileState(false);
    }

    /**
     * Update the tile state in the quick settings panel
     * @param active boolean to set the tile as active or inactive
     */
    private void updateTileState(boolean active) {
        if (getContext() != null) {
            sP.edit().putBoolean("timerActive", active).apply();
            TileService.requestListeningState(requireContext(), new ComponentName(requireContext(), QuickTileService.class));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}