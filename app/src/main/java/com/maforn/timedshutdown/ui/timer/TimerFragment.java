package com.maforn.timedshutdown.ui.timer;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

import java.util.Objects;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;

    private int counter = 60;

    private boolean isTiming = false;
    TextView timerText;

    CountDownTimer countDownTimer = null;

    NumberPicker numberPickerSec, numberPickerMin;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTimerBinding.inflate(inflater, container, false);

        SharedPreferences sP = Objects.requireNonNull(getContext()).getSharedPreferences("Timer", MODE_PRIVATE);
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

        if (!AccessibilityService.isAccessibilityServiceEnabled(Objects.requireNonNull(getContext()), AccessibilityService.class)) {
            AccessibilityService.requireAccessibility(getContext());
        }

        numberPickerSec = binding.numberPickerSec;
        numberPickerSec.setMinValue(0);
        numberPickerSec.setMaxValue(59);
        numberPickerSec.setWrapSelectorWheel(true);
        numberPickerSec.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (!isTiming) {
                counter = newVal + 60 * numberPickerMin.getValue();
                timerText.setText(String.valueOf(counter));
            }
        });

        numberPickerMin = binding.numberPickerMin;
        numberPickerMin.setMinValue(0);
        numberPickerMin.setMaxValue(59);
        numberPickerMin.setWrapSelectorWheel(true);
        numberPickerMin.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (!isTiming) {
                counter = newVal * 60 + numberPickerSec.getValue();
                timerText.setText(String.valueOf(counter));
            }
        });

        timerText = binding.timerText;

        binding.buttonStart.setOnClickListener(v -> {
            if (!isTiming) {

                ((Activity) getContext()).getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

                counter = numberPickerMin.getValue() * 60 + numberPickerSec.getValue();

                isTiming = true;
                countDownTimer = new CountDownTimer(counter * 1000L, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerText.setText(String.valueOf(counter));
                        counter--;
                    }

                    public void onFinish() {
                        timerText.setText("...");
                        Intent intent = new Intent(getContext(), AccessibilityService.class);
                        intent.putExtra("exec_gesture", true);
                        Log.d("ASD", String.valueOf(Objects.requireNonNull(getContext()).startService(intent)));
                        isTiming = false;
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

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}