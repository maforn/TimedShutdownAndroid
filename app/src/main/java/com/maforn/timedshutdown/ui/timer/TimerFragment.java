package com.maforn.timedshutdown.ui.timer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.maforn.timedshutdown.AccessibilityService;
import com.maforn.timedshutdown.databinding.FragmentTimerBinding;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;

    private int counter = 60;

    private boolean isTiming = false;
    Button button;
    TextView timerText;

    CountDownTimer countDownTimer = null;

    NumberPicker numberPickerSec, numberPickerMin;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTimerBinding.inflate(inflater, container, false);

        if (!AccessibilityService.isAccessibilityServiceEnabled(getContext(), AccessibilityService.class)) {
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

        timerText = (TextView) binding.timerText;

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) getContext()).getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

                isTiming = true;

                countDownTimer = new CountDownTimer(counter * 1000L, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerText.setText(String.valueOf(counter));
                        counter--;
                    }

                    public void onFinish() {
                        timerText.setText("...");
                        Intent intent = new Intent(getContext(), AccessibilityService.class);
                        Log.i("ASD", String.valueOf(getContext().startService(intent)));
                        isTiming = false;
                    }
                }.start();
            }
        });

        binding.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                isTiming = false;

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