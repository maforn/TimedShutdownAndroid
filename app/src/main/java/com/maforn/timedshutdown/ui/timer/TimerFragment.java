package com.maforn.timedshutdown.ui.timer;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.POWER_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
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
                paramDialogInterface.dismiss();
            });
            alertDialog.setButton(-2, getString(R.string.title_info), (paramDialogInterface, paramInt) -> {
                try {
                    Navigation.findNavController(container).navigate(R.id.action_timerFragment_to_infoFragment);
                } catch (Exception ignored) {
                }
                paramDialogInterface.dismiss();
            });
            alertDialog.setButton(-1, getString(R.string.alert_permission_cancel), (paramDialogInterface, paramInt) -> paramDialogInterface.dismiss());
            alertDialog.setOnDismissListener(dialogInterface -> {
                sP.edit().putBoolean("firstTime", false).apply();
            });
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
        binding.buttonStart.setOnClickListener(v -> {
            if (!isTiming) {

                // try to keep the screen on and permission to show when locked, it will not work otherwise
                ((Activity) requireContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_FULLSCREEN);

                counter = numberPickerHour.getValue() * 3600 + numberPickerMin.getValue() * 60 + numberPickerSec.getValue();

                isTiming = true;
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
                        }
                    }
                }.start();
            }
        });

        // the stop button will block the countdown timer
        binding.buttonStop.setOnClickListener(view -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            isTiming = false;

        });

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}