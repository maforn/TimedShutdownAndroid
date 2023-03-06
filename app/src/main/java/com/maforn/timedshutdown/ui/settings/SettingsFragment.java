package com.maforn.timedshutdown.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.maforn.timedshutdown.AccessibilityService;
import com.maforn.timedshutdown.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    View draggableOne, draggableTwo;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedPreferences = getContext().getSharedPreferences("Settings", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        int powerOffType = sharedPreferences.getInt("power_off_method", 0);

        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            editor.putInt(
                    "power_off_method",
                    checkedId == binding.radioClick.getId() ? 0 : checkedId == binding.radioTwoClick.getId() ? 1 : 2
                    );
            editor.apply();
            displayDraggables();
        });

        // if it's not the default check the one that is required
        if (powerOffType != 0) {
            binding.radioGroup.check(powerOffType == 1 ? binding.radioTwoClick.getId() : binding.radioSwipe.getId());
            displayDraggables();
        }

        binding.buttonPowerDialog.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AccessibilityService.class);
            getContext().startService(intent);
        });

        binding.buttonReset.setOnClickListener(view -> {
            binding.radioGroup.check(binding.radioClick.getId());
            editor.clear();
            editor.apply();
            getActivity().recreate();
        });

        binding.buttonHelp.setOnClickListener(view -> {
            //TODO: either a modal or navigate to info fragment
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


    View.OnTouchListener drag = new View.OnTouchListener() {
        float dX, dY;

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

                    /*Log.d("X_FALSE", String.valueOf(dX + event.getRawX()));
                    Log.d("Y_FALSE", String.valueOf(dY + event.getRawY()));
                    Log.d("X_FALSE_ABS", String.valueOf(event.getRawX()));
                    Log.d("Y_FALSE_ABS", String.valueOf(event.getRawY()));*/

            default:
                return false;
            }
        return true;
        }
    };

    private void displayDraggables(){
        if(sharedPreferences.getInt("power_off_method",0) != 0){
            binding.draggableTwo.setVisibility(View.VISIBLE);
        }else{
            binding.draggableTwo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        binding=null;
    }
}