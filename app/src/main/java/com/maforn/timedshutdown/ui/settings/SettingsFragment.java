package com.maforn.timedshutdown.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.maforn.timedshutdown.AccessibilityService;
import com.maforn.timedshutdown.R;
import com.maforn.timedshutdown.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    View draggableOne, draggableTwo;

    int idRadioClick, idRadioLongPress, idRadioTwoClick, idRadioSwipe;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedPreferences = requireContext().getSharedPreferences("Settings", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        int powerOffType = sharedPreferences.getInt("power_off_method", 0);
        /*
        * powerOffType:
        *   0 - one click
        *   1 - long press
        *   2 - two clicks
        *   3 - swipe
         */
        idRadioClick = binding.radioClick.getId();
        idRadioLongPress = binding.radioLongPress.getId();
        idRadioTwoClick = binding.radioTwoClick.getId();
        idRadioSwipe = binding.radioSwipe.getId();

        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            editor.putInt(
                    "power_off_method",
                    getChecked(checkedId)
            );
            editor.apply();
            displaySecondDraggable();
        });

        // if it's not the default check the one that is required
        if (powerOffType != 0) {
            binding.radioGroup.check(powerOffType == 1 ? idRadioLongPress : (powerOffType == 2 ? idRadioTwoClick : idRadioSwipe));
            displaySecondDraggable();
        }

        binding.buttonPowerDialog.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AccessibilityService.class);
            requireContext().startService(intent);
        });

        binding.buttonReset.setOnClickListener(view -> {
            binding.radioGroup.check(binding.radioClick.getId());
            editor.clear();
            editor.apply();
            requireActivity().recreate();
        });

        binding.buttonHelp.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_infoFragment));

        draggableOne = binding.draggableOne;
        draggableTwo = binding.draggableTwo;

        draggableOne.setOnTouchListener(drag);
        draggableTwo.setOnTouchListener(drag);

        draggableOne.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (sharedPreferences.contains("X_false")) {
                draggableOne.setX(sharedPreferences.getFloat("X_false", 100));
                draggableOne.setY(sharedPreferences.getFloat("Y_false", 100));
            }
            if (sharedPreferences.contains("X_true")) {
                draggableTwo.setX(sharedPreferences.getFloat("X_true", 100));
                draggableTwo.setY(sharedPreferences.getFloat("Y_true", 100));
            }
        });

        return root;
    }

    private int getChecked(int checkedId) {
        if (checkedId == idRadioClick) {
            return 0;
        } else if (checkedId == idRadioLongPress) {
            return 1;
        } else if (checkedId == idRadioTwoClick) {
            return 2;
        } else { // idRadioSwipe
            return 3;
        }
    }


    View.OnTouchListener drag = new View.OnTouchListener() {
        float dX, dY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();

                    break;

                case MotionEvent.ACTION_MOVE:

                    if (event.getRawX() + dX > 0 && event.getRawY() + dY > 0 && event.getRawX() + dX < binding.getRoot().getWidth() && event.getRawY() + dY < binding.getRoot().getHeight()) {
                        view.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    editor.putFloat("X_" + (view.getId() == draggableTwo.getId()), dX + event.getRawX());
                    editor.putFloat("X_ABS_" + (view.getId() == draggableTwo.getId()), event.getRawX());
                    editor.putFloat("Y_" + (view.getId() == draggableTwo.getId()), dY + event.getRawY());
                    editor.putFloat("Y_ABS_" + (view.getId() == draggableTwo.getId()), event.getRawY());
                    editor.apply();

                    /*DEBUG Log.d("X_FALSE", String.valueOf(dX + event.getRawX()));
                    Log.d("Y_FALSE", String.valueOf(dY + event.getRawY()));
                    Log.d("X_FALSE_ABS", String.valueOf(event.getRawX()));
                    Log.d("Y_FALSE_ABS", String.valueOf(event.getRawY()));*/

                default:
                    return false;
            }
            return true;
        }
    };

    private void displaySecondDraggable() {
        if (sharedPreferences.getInt("power_off_method", 0) > 1) {
            binding.draggableTwo.setVisibility(View.VISIBLE);
        } else {
            binding.draggableTwo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}