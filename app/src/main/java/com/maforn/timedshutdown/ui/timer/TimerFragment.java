package com.maforn.timedshutdown.ui.timer;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
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
import com.maforn.timedshutdown.R;
import com.maforn.timedshutdown.databinding.FragmentTimerBinding;

import java.util.Calendar;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;

    private int counter = 60;

    private boolean isTiming = false;
    TextView timerText;

    CountDownTimer countDownTimer = null;

    NumberPicker numberPickerSec, numberPickerMin, numberPickerHour;

    @SuppressLint("DefaultLocale")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTimerBinding.inflate(inflater, container, false);

        SharedPreferences sP = requireContext().getSharedPreferences("Timer", MODE_PRIVATE);
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
                SharedPreferences.Editor editor = sP.edit();
                editor.putBoolean("firstTime", false);
                editor.apply();
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
                counter = newVal + 60 * numberPickerMin.getValue() + 3600 * numberPickerHour.getValue();
                timerText.setText(String.format("%02d:%02d:%02d", numberPickerHour.getValue(), numberPickerMin.getValue(), newVal));
            }
        });

        numberPickerMin = binding.numberPickerMin;
        numberPickerMin.setMinValue(0);
        numberPickerMin.setMaxValue(59);
        numberPickerMin.setWrapSelectorWheel(true);
        numberPickerMin.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (!isTiming) {
                counter = 3600 * numberPickerHour.getValue() + newVal * 60 + numberPickerSec.getValue();
                timerText.setText(String.format("%02d:%02d:%02d", numberPickerHour.getValue(), newVal, numberPickerSec.getValue()));
            }
        });

        numberPickerHour = binding.numberPickerHour;
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);
        numberPickerHour.setWrapSelectorWheel(true);
        numberPickerHour.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (!isTiming) {
                counter = newVal * 3600 + numberPickerMin.getValue() * 60 + numberPickerSec.getValue();
                timerText.setText(String.format("%02d:%02d:%02d", newVal, numberPickerMin.getValue(), numberPickerSec.getValue()));
            }
        });

        timerText = binding.timerText;

        binding.buttonStart.setOnClickListener(v -> {
            if (!isTiming) {

                ((Activity) requireContext()).getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
                            Intent intent = new Intent(getContext(), AccessibilityService.class);
                            intent.putExtra("exec_gesture", true);
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

        binding.buttonSelect.setOnClickListener(view -> {
            if (!isTiming) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
                    counter = (selectedHour - hour) * 3600 + (selectedMinute - minute) * 60;
                    if (counter < 0) {
                        counter += 3600 * 24;
                    }
                    timerText.setText(String.format("%02d:%02d:00", counter / 3600, (counter % 3600) / 60));
                    numberPickerMin.setValue((counter % 3600) / 60);
                    numberPickerHour.setValue(counter / 3600);
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

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