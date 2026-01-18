package com.maforn.timedshutdown.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.maforn.timedshutdown.AccessibilityService;
import com.maforn.timedshutdown.PowerOffType;
import com.maforn.timedshutdown.R;
import com.maforn.timedshutdown.databinding.FragmentSettingsBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsFragment extends Fragment {

    public static final int DEFAULT_POINT_VALUE = 100;
    public static final String DEFAULT_JSON_CONFIG = "{power_off_method:0,initial_delay:2000,first_delay:2500,second_delay:2500,third_delay:2500,fourth_delay:2500,fifth_delay:2500,sixth_delay:2500,X_false:100,Y_false:100,X_true:100,Y_true:100,X_three:100,Y_three:100,X_four:100,Y_four:100,X_five:100,Y_five:100,X_six:100,Y_six:100,X_ABS_false:100,Y_ABS_false:100,X_ABS_true:100,Y_ABS_true:100,X_ABS_three:100,Y_ABS_three:100,X_ABS_four:100,Y_ABS_four:100,X_ABS_five:100,Y_ABS_five:100,X_ABS_six:100,Y_ABS_six:100}";
    private FragmentSettingsBinding binding;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    View draggableOne, draggableTwo, draggableThree, draggableFour, draggableFive, draggableSix;
    RadioGroup radioGroup, radioGroupConfig;
    LinearLayout linearLayout;
    ImageButton buttonToggleClicks, buttonToggleOptions, buttonRotate;

    int idRadioClick, idRadioLongPress, idRadioTwoClick, idRadioThreeClick, idRadioFourClick, idRadioFiveClick, idRadioSixClick, idRadioSwipe;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // bind the fragment to the main navigation activity
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        radioGroup = binding.radioGroup;
        radioGroupConfig = binding.radioGroupConfig;
        linearLayout = binding.linearSettings;
        buttonToggleClicks = binding.buttonToggleClicks;
        buttonToggleOptions = binding.buttonToggleOptions;

        // set the on click event on the toggle views button
        buttonToggleClicks.setOnClickListener(v -> toggleVisibility(radioGroup, v));
        buttonToggleOptions.setOnClickListener(v -> toggleVisibility(linearLayout, v));

        sharedPreferences = requireContext().getSharedPreferences("Settings", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isPortrait", true)) {
            requireActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            requireActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        editor = sharedPreferences.edit();

        // get the gesture power off type specified by the user
        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];

        ((RadioButton) radioGroupConfig.getChildAt(sharedPreferences.getInt("chosenConfig", 0))).setChecked(true);
        radioGroupConfig.setOnCheckedChangeListener((group, checkedId) -> {
            int configNumber = checkedId == R.id.radioConfig1 ? 0 : 1;
            editor.putInt("chosenConfig", configNumber);
            try {
                JSONObject jsonObject = new JSONObject(sharedPreferences.getString("config" + configNumber, DEFAULT_JSON_CONFIG));
                Log.d("CONFIG status json", jsonObject.toString());
                RadioButton radioButton = binding.getRoot().findViewById(getRadioIdFromOrdinal(jsonObject.getInt("power_off_method")));
                if (radioButton != null) {
                    radioButton.setChecked(true);
                }
                switchConfig(editor, jsonObject);
                // reload page to update the values
                requireActivity().recreate();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            Log.d("CONFIG chosenConfig", String.valueOf(configNumber));
            editor.commit();
            Log.d("CONFIG Y_true", String.valueOf(sharedPreferences.getFloat("Y_false", DEFAULT_POINT_VALUE)));
        });

        binding.saveConfig.setOnClickListener(v -> {
            int configNumber = sharedPreferences.getInt("chosenConfig", 0);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("power_off_method", sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()));
                jsonObject.put("initial_delay", sharedPreferences.getInt("initial_delay", 2000));
                jsonObject.put("first_delay", sharedPreferences.getInt("first_delay", 2500));
                jsonObject.put("second_delay", sharedPreferences.getInt("second_delay", 2500));
                jsonObject.put("third_delay", sharedPreferences.getInt("third_delay", 2500));
                jsonObject.put("fourth_delay", sharedPreferences.getInt("fourth_delay", 2500));
                jsonObject.put("fifth_delay", sharedPreferences.getInt("fifth_delay", 2500));
                jsonObject.put("sixth_delay", sharedPreferences.getInt("sixth_delay", 2500));
                jsonObject.put("X_false", sharedPreferences.getFloat("X_false", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_false", sharedPreferences.getFloat("Y_false", DEFAULT_POINT_VALUE));
                jsonObject.put("X_true", sharedPreferences.getFloat("X_true", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_true", sharedPreferences.getFloat("Y_true", DEFAULT_POINT_VALUE));
                jsonObject.put("X_three", sharedPreferences.getFloat("X_three", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_three", sharedPreferences.getFloat("Y_three", DEFAULT_POINT_VALUE));
                jsonObject.put("X_four", sharedPreferences.getFloat("X_four", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_four", sharedPreferences.getFloat("Y_four", DEFAULT_POINT_VALUE));
                jsonObject.put("X_five", sharedPreferences.getFloat("X_five", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_five", sharedPreferences.getFloat("Y_five", DEFAULT_POINT_VALUE));
                jsonObject.put("X_six", sharedPreferences.getFloat("X_six", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_six", sharedPreferences.getFloat("Y_six", DEFAULT_POINT_VALUE));
                jsonObject.put("X_ABS_false", sharedPreferences.getFloat("X_ABS_false", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_ABS_false", sharedPreferences.getFloat("Y_ABS_false", DEFAULT_POINT_VALUE));
                jsonObject.put("X_ABS_true", sharedPreferences.getFloat("X_ABS_true", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_ABS_true", sharedPreferences.getFloat("Y_ABS_true", DEFAULT_POINT_VALUE));
                jsonObject.put("X_ABS_three", sharedPreferences.getFloat("X_ABS_three", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_ABS_three", sharedPreferences.getFloat("Y_ABS_three", DEFAULT_POINT_VALUE));
                jsonObject.put("X_ABS_four", sharedPreferences.getFloat("X_ABS_four", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_ABS_four", sharedPreferences.getFloat("Y_ABS_four", DEFAULT_POINT_VALUE));
                jsonObject.put("X_ABS_five", sharedPreferences.getFloat("X_ABS_five", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_ABS_five", sharedPreferences.getFloat("Y_ABS_five", DEFAULT_POINT_VALUE));
                jsonObject.put("X_ABS_six", sharedPreferences.getFloat("X_ABS_six", DEFAULT_POINT_VALUE));
                jsonObject.put("Y_ABS_six", sharedPreferences.getFloat("Y_ABS_six", DEFAULT_POINT_VALUE));
                editor.putString("config" + configNumber, jsonObject.toString());
                editor.apply();
                Toast.makeText(requireContext(), "Saved Config " + (configNumber + 1), Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        idRadioClick = binding.radioClick.getId();
        idRadioLongPress = binding.radioLongPress.getId();
        idRadioTwoClick = binding.radioTwoClick.getId();
        idRadioThreeClick = binding.radioThreeClick.getId();
        idRadioFourClick = binding.radioFourClick.getId();
        idRadioFiveClick = binding.radioFiveClick.getId();
        idRadioSixClick = binding.radioSixClick.getId();
        idRadioSwipe = binding.radioSwipe.getId();

        // set up the listener to set the power off method on click
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            editor.putInt(
                    "power_off_method",
                    getChecked(checkedId)
            );
            editor.apply();
            displaySecondDraggable();
            displayThirdDraggable();
            displayFourthDraggable();
            displayFifthDraggable();
            displaySixthDraggable();
        });

        // if it's not the default power off type, check the selected radio group
        if (power_off_type != PowerOffType.ONECLICK) {
            radioGroup.check(power_off_type == PowerOffType.LONGPRESS ? idRadioLongPress
                    : (power_off_type == PowerOffType.TWOCLICKS ? idRadioTwoClick
                    : (power_off_type == PowerOffType.THREECLICKS ? idRadioThreeClick
                    : (power_off_type == PowerOffType.FOURCLICKS ? idRadioFourClick
                    : (power_off_type == PowerOffType.FIVECLICKS ? idRadioFiveClick
                    : (power_off_type == PowerOffType.SIXCLICKS ? idRadioSixClick
                    : idRadioSwipe))))));
            displaySecondDraggable();
            displayThirdDraggable();
            displayFourthDraggable();
            displayFifthDraggable();
            displaySixthDraggable();
        }

        // set the on click event on the power dialog button
        binding.buttonPowerDialog.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AccessibilityService.class);
            requireContext().startService(intent);
        });

        // set the on click event on the reset button: reset the draggables position
        binding.buttonReset.setOnClickListener(view -> {
            editor.clear();
            radioGroup.check(binding.radioClick.getId());
            radioGroupConfig.check(binding.radioConfig1.getId());
            editor.apply();
            requireActivity().recreate();
        });

        // on info button click navigate to the info activity
        binding.buttonHelp.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_infoFragment));

        // on delay button click open a dialog to allow the user to set personalised delays between events
        binding.buttonSetDelay.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Set Clicks Delays");

            // get custom view to inflate into the dialog
            View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_delay, (ViewGroup) getView(), false);

            // get and set the input fields
            final EditText inputInitialDelay = viewInflated.findViewById(R.id.inputInitialDelay);
            final EditText inputDelayFirstAction = viewInflated.findViewById(R.id.inputDelayFirstAction);
            final EditText inputDelaySecondAction = viewInflated.findViewById(R.id.inputDelaySecondAction);
            final EditText inputDelayThirdAction = viewInflated.findViewById(R.id.inputDelayThirdAction);
            final EditText inputDelayFourthAction = viewInflated.findViewById(R.id.inputDelayFourthAction);
            final EditText inputDelayFifthAction = viewInflated.findViewById(R.id.inputDelayFifthAction);
            final EditText inputDelaySixthAction = viewInflated.findViewById(R.id.inputDelaySixthAction);

            inputInitialDelay.setText(String.valueOf(sharedPreferences.getInt("initial_delay", 2000)));
            inputDelayFirstAction.setText(String.valueOf(sharedPreferences.getInt("first_delay", 2500)));
            inputDelaySecondAction.setText(String.valueOf(sharedPreferences.getInt("second_delay", 2500)));
            inputDelayThirdAction.setText(String.valueOf(sharedPreferences.getInt("third_delay", 2500)));
            inputDelayFourthAction.setText(String.valueOf(sharedPreferences.getInt("fourth_delay", 2500)));
            inputDelayFifthAction.setText(String.valueOf(sharedPreferences.getInt("fifth_delay", 2500)));
            inputDelaySixthAction.setText(String.valueOf(sharedPreferences.getInt("sixth_delay", 2500)));

            builder.setView(viewInflated);

            // on OK button click set the new delays
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                // using a try is necessary for the case of an empty string (""), because parseInt
                // cannot handle it and throws a NumberFormatException
                try {
                    // set the new values with 50 milliseconds as the min and 60000 as the max
                    editor.putInt("initial_delay", Math.min(Math.max(50, Integer.parseInt(inputInitialDelay.getText().toString())), 60000));
                    editor.putInt("first_delay", Math.min(Math.max(50, Integer.parseInt(inputDelayFirstAction.getText().toString())), 60000));
                    editor.putInt("second_delay", Math.min(Math.max(50, Integer.parseInt(inputDelaySecondAction.getText().toString())), 60000));
                    editor.putInt("third_delay", Math.min(Math.max(50, Integer.parseInt(inputDelayThirdAction.getText().toString())), 60000));
                    editor.putInt("fourth_delay", Math.min(Math.max(50, Integer.parseInt(inputDelayFourthAction.getText().toString())), 60000));
                    editor.putInt("fifth_delay", Math.min(Math.max(50, Integer.parseInt(inputDelayFifthAction.getText().toString())), 60000));
                    editor.putInt("sixth_delay", Math.min(Math.max(50, Integer.parseInt(inputDelaySixthAction.getText().toString())), 60000));
                    editor.apply();
                } catch (Exception ignored) {
                }
                dialog.dismiss();
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

            builder.show();
        });

        // get the draggables circles and set the touch listener
        draggableOne = binding.draggableOne;
        draggableTwo = binding.draggableTwo;
        draggableThree = binding.draggableThree;
        draggableFour = binding.draggableFour;
        draggableFive = binding.draggableFive;
        draggableSix = binding.draggableSix;

        draggableOne.setOnTouchListener(drag);
        draggableTwo.setOnTouchListener(drag);
        draggableThree.setOnTouchListener(drag);
        draggableFour.setOnTouchListener(drag);
        draggableFive.setOnTouchListener(drag);
        draggableSix.setOnTouchListener(drag);

        // on reopening set the values as they were set
        draggableOne.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (sharedPreferences.contains("X_false")) {
                draggableOne.setX(sharedPreferences.getFloat("X_false", DEFAULT_POINT_VALUE));
                draggableOne.setY(sharedPreferences.getFloat("Y_false", DEFAULT_POINT_VALUE));
            }
            if (sharedPreferences.contains("X_true")) {
                draggableTwo.setX(sharedPreferences.getFloat("X_true", DEFAULT_POINT_VALUE));
                draggableTwo.setY(sharedPreferences.getFloat("Y_true", DEFAULT_POINT_VALUE));
            }
            if (sharedPreferences.contains("X_three")) {
                draggableThree.setX(sharedPreferences.getFloat("X_three", DEFAULT_POINT_VALUE));
                draggableThree.setY(sharedPreferences.getFloat("Y_three", DEFAULT_POINT_VALUE));
            }
            if (sharedPreferences.contains("X_four")) {
                draggableFour.setX(sharedPreferences.getFloat("X_four", DEFAULT_POINT_VALUE));
                draggableFour.setY(sharedPreferences.getFloat("Y_four", DEFAULT_POINT_VALUE));
            }
            if (sharedPreferences.contains("X_five")) {
                draggableFive.setX(sharedPreferences.getFloat("X_five", DEFAULT_POINT_VALUE));
                draggableFive.setY(sharedPreferences.getFloat("Y_five", DEFAULT_POINT_VALUE));
            }
            if (sharedPreferences.contains("X_six")) {
                draggableSix.setX(sharedPreferences.getFloat("X_six", DEFAULT_POINT_VALUE));
                draggableSix.setY(sharedPreferences.getFloat("Y_six", DEFAULT_POINT_VALUE));
            }
        });


        buttonRotate = binding.rotateButton;
        buttonRotate.setOnClickListener(v -> {
            // set shared preference and rotate the screen between vertical and horizontal
            boolean isPortrait = sharedPreferences.getBoolean("isPortrait", true);
            editor.putBoolean("isPortrait", !isPortrait);
            editor.apply();
            requireActivity().recreate();
        });

        return root;
    }

    public static void switchConfig(SharedPreferences.Editor editor, JSONObject jsonObject) throws JSONException {
        editor.putInt("power_off_method", jsonObject.getInt("power_off_method"));
        editor.putInt("initial_delay", jsonObject.getInt("initial_delay"));
        editor.putInt("first_delay", jsonObject.getInt("first_delay"));
        editor.putInt("second_delay", jsonObject.getInt("second_delay"));
        editor.putInt("third_delay", jsonObject.getInt("third_delay"));
        editor.putInt("fourth_delay", jsonObject.getInt("fourth_delay"));
        editor.putInt("fifth_delay", jsonObject.optInt("fifth_delay", 2500));
        editor.putInt("sixth_delay", jsonObject.optInt("sixth_delay", 2500));
        editor.putFloat("X_false", (float) jsonObject.optDouble("X_false", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_false", (float) jsonObject.optDouble("Y_false", DEFAULT_POINT_VALUE));
        editor.putFloat("X_true", (float) jsonObject.optDouble("X_true", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_true", (float) jsonObject.optDouble("Y_true", DEFAULT_POINT_VALUE));
        editor.putFloat("X_three", (float) jsonObject.optDouble("X_three", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_three", (float) jsonObject.optDouble("Y_three", DEFAULT_POINT_VALUE));
        editor.putFloat("X_four", (float) jsonObject.optDouble("X_four", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_four", (float) jsonObject.optDouble("Y_four", DEFAULT_POINT_VALUE));
        editor.putFloat("X_five", (float) jsonObject.optDouble("X_five", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_five", (float) jsonObject.optDouble("Y_five", DEFAULT_POINT_VALUE));
        editor.putFloat("X_six", (float) jsonObject.optDouble("X_six", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_six", (float) jsonObject.optDouble("Y_six", DEFAULT_POINT_VALUE));
        editor.putFloat("X_ABS_false", (float) jsonObject.optDouble("X_ABS_false", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_ABS_false", (float) jsonObject.optDouble("Y_ABS_false", DEFAULT_POINT_VALUE));
        editor.putFloat("X_ABS_true", (float) jsonObject.optDouble("X_ABS_true", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_ABS_true", (float) jsonObject.optDouble("Y_ABS_true", DEFAULT_POINT_VALUE));
        editor.putFloat("X_ABS_three", (float) jsonObject.optDouble("X_ABS_three", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_ABS_three", (float) jsonObject.optDouble("Y_ABS_three", DEFAULT_POINT_VALUE));
        editor.putFloat("X_ABS_four", (float) jsonObject.optDouble("X_ABS_four", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_ABS_four", (float) jsonObject.optDouble("Y_ABS_four", DEFAULT_POINT_VALUE));
        editor.putFloat("X_ABS_five", (float) jsonObject.optDouble("X_ABS_five", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_ABS_five", (float) jsonObject.optDouble("Y_ABS_five", DEFAULT_POINT_VALUE));
        editor.putFloat("X_ABS_six", (float) jsonObject.optDouble("X_ABS_six", DEFAULT_POINT_VALUE));
        editor.putFloat("Y_ABS_six", (float) jsonObject.optDouble("Y_ABS_six", DEFAULT_POINT_VALUE));
    }

    /**
     * Toggle the visibility of the element and animate the button rotation
     *
     * @param element the element to hide/show
     * @param button  the button to animate
     */
    private void toggleVisibility(View element, View button) {
        int visibility = element.getVisibility();
        float rotationAngle = 0f;
        if (visibility == View.VISIBLE) {
            element.setVisibility(View.GONE);
            rotationAngle = 180f;
        } else {
            element.setVisibility(View.VISIBLE);
        }
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(button, "rotation", rotationAngle);
        rotateAnimator.setDuration(300);
        rotateAnimator.start();
    }

    /**
     * Get back the number that corresponds to the respective power off type
     *
     * @param checkedId the id of the clicked radio group
     * @return the number value that corresponds to the ENUM type
     */
    private int getChecked(int checkedId) {
        if (checkedId == idRadioClick) {
            return PowerOffType.ONECLICK.ordinal();
        } else if (checkedId == idRadioLongPress) {
            return PowerOffType.LONGPRESS.ordinal();
        } else if (checkedId == idRadioTwoClick) {
            return PowerOffType.TWOCLICKS.ordinal();
        } else if (checkedId == idRadioThreeClick) {
            return PowerOffType.THREECLICKS.ordinal();
        } else if (checkedId == idRadioFourClick) {
            return PowerOffType.FOURCLICKS.ordinal();
        } else if (checkedId == idRadioFiveClick) {
            return PowerOffType.FIVECLICKS.ordinal();
        } else if (checkedId == idRadioSixClick) {
            return PowerOffType.SIXCLICKS.ordinal();
        } else { // idRadioSwipe
            return PowerOffType.SWIPE.ordinal();
        }
    }


    /**
     * Get back the id of the radio button that corresponds to the respective power off type
     *
     * @param ordinal the ordinal of the ENUM type
     * @return the id of the clicked radio group
     */
    private int getRadioIdFromOrdinal(int ordinal) {
        if (ordinal == PowerOffType.ONECLICK.ordinal()) return idRadioClick;
        if (ordinal == PowerOffType.LONGPRESS.ordinal()) return idRadioLongPress;
        if (ordinal == PowerOffType.TWOCLICKS.ordinal()) return idRadioTwoClick;
        if (ordinal == PowerOffType.THREECLICKS.ordinal()) return idRadioThreeClick;
        if (ordinal == PowerOffType.FOURCLICKS.ordinal()) return idRadioFourClick;
        if (ordinal == PowerOffType.FIVECLICKS.ordinal()) return idRadioFiveClick;
        if (ordinal == PowerOffType.SIXCLICKS.ordinal()) return idRadioSixClick;
        if (ordinal == PowerOffType.SWIPE.ordinal()) return idRadioSwipe;
        throw new IllegalArgumentException("Unknown ordinal: " + ordinal);
    }

    /**
     * Set an listener for the draggables so their position is saved and set as the user requires
     */
    View.OnTouchListener drag = new View.OnTouchListener() {
        float dX, dY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {

                // get the first position on touch
                case MotionEvent.ACTION_DOWN:
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();

                    break;

                // store the position and animate the movement
                case MotionEvent.ACTION_MOVE:

                    if (event.getRawX() + dX > 0 && event.getRawY() + dY > 0 && event.getRawX() + dX < binding.getRoot().getWidth() && event.getRawY() + dY < binding.getRoot().getHeight()) {
                        view.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                    }
                    break;

                // save values only on release
                case MotionEvent.ACTION_UP:
                    String viewType = view.getId() == draggableTwo.getId() ? "true" : (view.getId() == draggableThree.getId() ? "three" : (view.getId() == draggableFour.getId() ? "four" : (view.getId() == draggableFive.getId() ? "five" : (view.getId() == draggableSix.getId() ? "six" : "false"))));
                    editor.putFloat("X_" + viewType, dX + event.getRawX());
                    editor.putFloat("X_ABS_" + viewType, event.getRawX());
                    editor.putFloat("Y_" + viewType, dY + event.getRawY());
                    editor.putFloat("Y_ABS_" + viewType, event.getRawY());
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

    /**
     * This function will display the second draggable circle if the power off method requires
     * two of them (swipe or two clicks)
     */
    private void displaySecondDraggable() {
        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];
        if (power_off_type == PowerOffType.SWIPE || power_off_type == PowerOffType.TWOCLICKS || power_off_type == PowerOffType.THREECLICKS || power_off_type == PowerOffType.FOURCLICKS || power_off_type == PowerOffType.FIVECLICKS || power_off_type == PowerOffType.SIXCLICKS) {
            binding.draggableTwo.setVisibility(View.VISIBLE);
        } else {
            binding.draggableTwo.setVisibility(View.GONE);
        }
    }

    /**
     * This function will display the second draggable circle if the power off method requires
     * three of them
     */
    private void displayThirdDraggable() {
        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];
        if (power_off_type == PowerOffType.THREECLICKS || power_off_type == PowerOffType.FOURCLICKS || power_off_type == PowerOffType.FIVECLICKS || power_off_type == PowerOffType.SIXCLICKS) {
            binding.draggableThree.setVisibility(View.VISIBLE);
        } else {
            binding.draggableThree.setVisibility(View.GONE);
        }
    }

    /**
     * This function will display the fourth draggable circle if the power off method requires
     * four of them
     */
    private void displayFourthDraggable() {
        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];
        if (power_off_type == PowerOffType.FOURCLICKS || power_off_type == PowerOffType.FIVECLICKS || power_off_type == PowerOffType.SIXCLICKS) {
            binding.draggableFour.setVisibility(View.VISIBLE);
        } else {
            binding.draggableFour.setVisibility(View.GONE);
        }
    }

    /**
     * This function will display the fifth draggable circle if the power off method requires
     * five of them
     */
    private void displayFifthDraggable() {
        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];
        if (power_off_type == PowerOffType.FIVECLICKS || power_off_type == PowerOffType.SIXCLICKS) {
            binding.draggableFive.setVisibility(View.VISIBLE);
        } else {
            binding.draggableFive.setVisibility(View.GONE);
        }
    }

    /**
     * This function will display the sixth draggable circle if the power off method requires
     * six of them
     */
    private void displaySixthDraggable() {
        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];
        if (power_off_type == PowerOffType.SIXCLICKS) {
            binding.draggableSix.setVisibility(View.VISIBLE);
        } else {
            binding.draggableSix.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onDestroyView();
        binding = null;
    }
}