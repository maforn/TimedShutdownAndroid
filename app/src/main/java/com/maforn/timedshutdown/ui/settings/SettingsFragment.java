package com.maforn.timedshutdown.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.maforn.timedshutdown.AccessibilityService;
import com.maforn.timedshutdown.PowerOffType;
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

        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];

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
        if (power_off_type != PowerOffType.ONECLICK) {
            binding.radioGroup.check(power_off_type == PowerOffType.LONGPRESS ? idRadioLongPress : (power_off_type == PowerOffType.TWOCLICKS ? idRadioTwoClick : idRadioSwipe));
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

        binding.buttonSetDelay.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Set Clicks Delays");

            View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_delay, (ViewGroup) getView(), false);

            final EditText inputInitialDelay = (EditText) viewInflated.findViewById(R.id.inputInitialDelay);
            final EditText inputDelayFirstAction = (EditText) viewInflated.findViewById(R.id.inputDelayFirstAction);
            final EditText inputDelaySecondAction = (EditText) viewInflated.findViewById(R.id.inputDelaySecondAction);
            inputInitialDelay.setText(String.valueOf(sharedPreferences.getInt("initial_delay", 2000)));
            inputDelayFirstAction.setText(String.valueOf(sharedPreferences.getInt("first_delay", 2500)));
            inputDelaySecondAction.setText(String.valueOf(sharedPreferences.getInt("second_delay", 2500)));

            builder.setView(viewInflated);

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                editor.putInt("initial_delay", Math.max(50, Integer.parseInt(inputInitialDelay.getText().toString())));
                editor.putInt("first_delay", Math.max(50, Integer.parseInt(inputDelayFirstAction.getText().toString())));
                editor.putInt("second_delay", Math.max(50, Integer.parseInt(inputDelaySecondAction.getText().toString())));
                editor.apply();
                dialog.dismiss();
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

            builder.show();
        });

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
            return PowerOffType.ONECLICK.ordinal();
        } else if (checkedId == idRadioLongPress) {
            return PowerOffType.LONGPRESS.ordinal();
        } else if (checkedId == idRadioTwoClick) {
            return PowerOffType.TWOCLICKS.ordinal();
        } else { // idRadioSwipe
            return PowerOffType.SWIPE.ordinal();
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
        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];
        if (power_off_type == PowerOffType.SWIPE || power_off_type == PowerOffType.TWOCLICKS) {
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