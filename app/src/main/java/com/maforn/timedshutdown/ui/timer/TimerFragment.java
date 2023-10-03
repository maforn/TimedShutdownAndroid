package com.maforn.timedshutdown.ui.timer;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;

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

    @SuppressLint("DefaultLocale")
    private void setCounter(int seconds, int minutes, int hours) {
        counter = seconds + 60 * minutes + 3600 * hours;
        sP.edit().putInt("lastCounter", counter).apply();
        timerText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    @SuppressLint("DefaultLocale")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTimerBinding.inflate(inflater, container, false);
        sP = requireContext().getSharedPreferences("Timer", MODE_PRIVATE);

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

        if (!AccessibilityService.isAccessibilityServiceEnabled(requireContext(), AccessibilityService.class)) {
            AccessibilityService.requireAccessibility(getContext());
        }

        numberPickerSec = binding.numberPickerSec;
        numberPickerSec.setMinValue(0);
        numberPickerSec.setMaxValue(59);
        numberPickerSec.setWrapSelectorWheel(true);
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

        // get last used timer
        if (sP.contains("lastCounter")) {
            counter = sP.getInt("lastCounter", 0);
            numberPickerHour.setValue(counter / 3600);
            numberPickerMin.setValue((counter % 3600) / 60);
            numberPickerSec.setValue(counter % 60);
            timerText.setText(String.format("%02d:%02d:%02d", counter / 3600, (counter % 3600) / 60, counter % 60));
        }

        binding.buttonStart.setOnClickListener(v -> {
            if (!isTiming) {

                ((Activity) requireContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_FULLSCREEN);

                counter = numberPickerHour.getValue() * 3600 + numberPickerMin.getValue() * 60 + numberPickerSec.getValue();

                isTiming = true;
                countDownTimer = new CountDownTimer(counter * 1000L, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerText.setText(String.format("%02d:%02d:%02d", counter / 3600, (counter % 3600) / 60, counter % 60));
                        counter--;
                    }

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

        binding.buttonStop.setOnClickListener(view -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            isTiming = false;

        });

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