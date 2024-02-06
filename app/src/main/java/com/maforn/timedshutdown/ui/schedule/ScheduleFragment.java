package com.maforn.timedshutdown.ui.schedule;

import static android.content.Context.MODE_PRIVATE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.maforn.timedshutdown.AccessibilitySupportService;
import com.maforn.timedshutdown.R;
import com.maforn.timedshutdown.databinding.FragmentScheduleBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.stream.IntStream;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;

    private SharedPreferences sP;

    private AlarmManager alarmManager;

    private boolean is24Hour;

    private int getDay(int id, @NonNull View addView) {
        if (id == addView.findViewById(R.id.sunday).getId())
            return Calendar.SUNDAY;
        if (id == addView.findViewById(R.id.monday).getId())
            return Calendar.MONDAY;
        if (id == addView.findViewById(R.id.tuesday).getId())
            return Calendar.TUESDAY;
        if (id == addView.findViewById(R.id.wednesday).getId())
            return Calendar.WEDNESDAY;
        if (id == addView.findViewById(R.id.thursday).getId())
            return Calendar.THURSDAY;
        if (id == addView.findViewById(R.id.friday).getId())
            return Calendar.FRIDAY;
        return Calendar.SATURDAY;
    }

    public static int getIdIndex(@NonNull JSONArray arr, int id) throws JSONException {
        int index = -1;
        for (int e = 0; e < arr.length(); e++) {
            if (arr.getJSONObject(e).getInt("id") == id)
                index = e;
        }
        return index;
    }

    private int getIndex(@NonNull JSONArray arr, int id) throws JSONException {
        int index = -1;
        for (int e = 0; e < arr.length(); e++) {
            if (arr.getInt(e) == id)
                index = e;
        }
        return index;
    }

    @NonNull
    public static int[] JSonArray2IntArray(@NonNull JSONArray jsonArray) {
        int[] intArray = new int[jsonArray.length()];
        for (int i = 0; i < intArray.length; ++i) {
            intArray[i] = jsonArray.optInt(i);
        }
        return intArray;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentScheduleBinding.inflate(inflater, container, false);

        sP = requireContext().getSharedPreferences("Schedule", MODE_PRIVATE);

        is24Hour = sP.getBoolean("is_24_hour", true);

        binding.buttonTimeFormat.setOnClickListener(view -> {
            AlertDialog alertDialog = (new AlertDialog.Builder(getContext())).create();
            alertDialog.setTitle(getString(R.string.changeTimeFormat));
            alertDialog.setMessage(getString(R.string.alert_settings_hours_format));
            alertDialog.setButton(-3, getString(R.string.H12), (paramDialogInterface, paramInt) -> {
                sP.edit().putBoolean("is_24_hour", false).apply();
                is24Hour = false;
                recreateView();
                paramDialogInterface.dismiss();
            });
            alertDialog.setButton(-1, getString(R.string.H24), (paramDialogInterface, paramInt) -> {
                sP.edit().putBoolean("is_24_hour", true).apply();
                is24Hour = true;
                recreateView();
                paramDialogInterface.dismiss();
            });
            alertDialog.show();
        });

        // set up AlarmManager and check if the permission was granted
        alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(requireContext(), "The alarm/scheduling permission is required to set up schedules", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                requireContext().startActivity(intent);
            }
        }

        if (!sP.contains("firstTime")) {
            AlertDialog alertDialog = (new AlertDialog.Builder(getContext())).create();
            alertDialog.setTitle(getString(R.string.title_info));
            alertDialog.setMessage(getString(R.string.schedule_disclaimer));
            alertDialog.setButton(-2, getString(R.string.title_info), (paramDialogInterface, paramInt) -> {
                try {
                    Navigation.findNavController(container).navigate(R.id.action_scheduleFragment_to_infoFragment);
                } catch (Exception ignored) {
                }
                paramDialogInterface.dismiss();
            });
            alertDialog.setButton(-1, getString(R.string.alert_permission_ok), (paramDialogInterface, paramInt) -> paramDialogInterface.dismiss());
            alertDialog.setOnDismissListener(dialogInterface -> sP.edit().putBoolean("firstTime", false).apply());
            alertDialog.show();
        }

        // setup add schedule FAB
        binding.addSchedule.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            // create an entry for the current time
            JSONObject jsonObject;
            JSONArray arr;
            try {
                jsonObject = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                arr = jsonObject.getJSONArray("schedules");
                if (arr.length() > 9) {
                    Toast.makeText(requireContext(), "Maximum number of possible schedules reached (10)", Toast.LENGTH_SHORT).show();
                    return;
                }
                int newId = 0;
                if (arr.length() != 0)
                    newId = arr.getJSONObject(arr.length() - 1).getInt("id") + 1;

                arr.put(new JSONObject(String.format("{'id':%d,'hour':%d,'minute':%d,'repeating':[],'active':false}", newId, hour, minute)));
                sP.edit().putString("schedules", jsonObject.toString()).apply();

                addElement(arr.getJSONObject(arr.length() - 1));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        return binding.getRoot();
    }

    private static void cancelSchedules(int id, Context context, AlarmManager alarmManager) {
        Intent alarmIntent = new Intent(context, AccessibilitySupportService.class);

        alarmIntent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getService(context, id * 10, alarmIntent, PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);

        alarmIntent = new Intent(context, AccessibilitySupportService.class);
        for (int i = 0; i < 7; i++) {
            try {
                pendingIntent = PendingIntent.getService(context, id * 10 + i, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
                alarmManager.cancel(pendingIntent);
            } catch (Exception ignored) {
            }
        }
    }

    public static void setSchedule(JSONObject jsonObject, Context context, AlarmManager alarmManager) {
        try {
            int id = jsonObject.getInt("id");
            int hour = jsonObject.getInt("hour");
            int minute = jsonObject.getInt("minute");
            int[] repeating = JSonArray2IntArray(jsonObject.getJSONArray("repeating"));
            boolean checked = jsonObject.getBoolean("active");

            cancelSchedules(id, context, alarmManager);

            Intent alarmIntent = new Intent(context, AccessibilitySupportService.class);
            if (checked) {
                Calendar alarmCalendar = Calendar.getInstance();
                alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
                alarmCalendar.set(Calendar.MINUTE, minute);
                alarmCalendar.set(Calendar.SECOND, 0);
                alarmCalendar.set(Calendar.MILLISECOND, 0);
                // if the date has already passed set it for tomorrow
                if (alarmCalendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
                    alarmCalendar.add(Calendar.HOUR_OF_DAY, 24);

                if (repeating.length == 0) {
                    alarmIntent.putExtra("id", id);

                    PendingIntent pendingIntent = PendingIntent.getService(context, id * 10, alarmIntent, PendingIntent.FLAG_MUTABLE);
                    alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(alarmCalendar.getTimeInMillis(), pendingIntent), pendingIntent);
                } else {
                    for (int i = 0; i < repeating.length; i++) {
                        PendingIntent pendingIntent = PendingIntent.getService(context, id * 10 + i, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
                        alarmCalendar.set(Calendar.DAY_OF_WEEK, repeating[i]);
                        // create a new instance for each day so that the main is not influenced
                        Calendar dayCalendar = Calendar.getInstance();
                        dayCalendar.setTimeInMillis(alarmCalendar.getTimeInMillis());

                        if (dayCalendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
                            dayCalendar.add(Calendar.HOUR_OF_DAY, 24 * 7);
                        long alarmTime = dayCalendar.getTimeInMillis();

                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 * 7, pendingIntent);
                    }

                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("DefaultLocale")
    private void setHoursText(int hour, int minute, TextView text) {
        if (is24Hour) {
            text.setText(String.format("%02d:%02d", hour, minute));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            text.setText(String.format("%02d:%02d %s", cal.get(Calendar.HOUR), minute, cal.get(Calendar.AM_PM) == Calendar.PM ? "PM" : "AM"));
        }
    }

    @SuppressLint("DefaultLocale")
    private void addElement(@NonNull JSONObject jsonObject) throws JSONException {
        int id = jsonObject.getInt("id");
        int hour = jsonObject.getInt("hour");
        int minute = jsonObject.getInt("minute");
        int[] repeating = JSonArray2IntArray(jsonObject.getJSONArray("repeating"));
        boolean checked = jsonObject.getBoolean("active");

        // get the layout and set the margins
        @SuppressLint("InflateParams") View addView = getLayoutInflater().inflate(R.layout.schedule, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        addView.setLayoutParams(params);

        // set up the main time text
        TextView mainText = addView.findViewById(R.id.mainText);
        setHoursText(hour, minute, mainText);

        // add a general on click listener to change the time when the schedule will go off
        addView.setOnClickListener(v -> {
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
                JSONObject jO;
                JSONArray arr;
                try {
                    jO = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                    arr = jO.getJSONArray("schedules");
                    int index = getIdIndex(arr, id);
                    arr.put(index, jsonObject.put("hour", selectedHour).put("minute", selectedMinute));
                    sP.edit().putString("schedules", jO.toString()).apply();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                setSchedule(jsonObject, requireContext(), alarmManager);

                setHoursText(selectedHour, selectedMinute, mainText);
            }, hour, minute, is24Hour);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        // set up the repeating days
        LinearLayout days = addView.findViewById(R.id.days);
        for (int i = 0; i < 7; i++) {
            int finalI = i + 1;
            // check if it's the day
            if (IntStream.of(repeating).anyMatch(x -> finalI == x))
                ((ToggleButton) days.getChildAt(i)).setChecked(true);

            days.getChildAt(i).setOnClickListener(v -> {
                ToggleButton day = (ToggleButton) v;
                JSONObject jO;
                JSONArray arr;
                try {
                    jO = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                    arr = jO.getJSONArray("schedules");
                    int index = getIdIndex(arr, id);
                    JSONArray rep = arr.getJSONObject(index).getJSONArray("repeating");

                    int dayAsNumber = getDay(day.getId(), addView);
                    if (day.isChecked()) {
                        rep.put(dayAsNumber);
                    } else {
                        rep.remove(getIndex(rep, dayAsNumber));
                    }

                    arr.put(index, jsonObject.put("repeating", rep));
                    sP.edit().putString("schedules", jO.toString()).apply();

                    setSchedule(jsonObject, requireContext(), alarmManager);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        SwitchCompat toggleSwitch = addView.findViewById(R.id.toggleSwitch);
        toggleSwitch.setChecked(checked);
        toggleSwitch.setOnClickListener(v1 -> {
            JSONObject jO;
            JSONArray arr;
            try {
                jO = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                arr = jO.getJSONArray("schedules");
                int index = getIdIndex(arr, id);
                arr.put(index, jsonObject.put("active", ((SwitchCompat) v1).isChecked()));
                sP.edit().putString("schedules", jO.toString()).apply();

                setSchedule(jsonObject, requireContext(), alarmManager);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        // set up remove button
        addView.findViewById(R.id.remove).setOnClickListener(v2 -> {
            JSONObject jO;
            JSONArray arr;
            try {
                jO = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                arr = jO.getJSONArray("schedules");
                int index = getIdIndex(arr, id);
                arr.remove(index);
                sP.edit().putString("schedules", jO.toString()).apply();

                cancelSchedules(id, requireContext(), alarmManager);

                // Start the animation
                addView.animate()
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                binding.mainLayout.removeView(addView);
                            }
                        });
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        // add the inflated layout
        addView.setAlpha(0.0f);
        binding.mainLayout.addView(addView);
        addView.animate().alpha(1.0f);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Needs to be here because switch.setChecked() does not work within OnCreate if it's not the first time
        recreateView();
    }

    private void recreateView() {
        binding.mainLayout.removeAllViews();
        if (sP.contains("schedules")) {
            try {
                JSONObject jsonObject = new JSONObject(sP.getString("schedules", "{'schedules':[]}"));
                JSONArray schedules = jsonObject.getJSONArray("schedules");

                for (int i = 0; i < schedules.length(); i++) {
                    // debug
                    // Log.i("JSON", schedules.get(i).toString());
                    JSONObject element = schedules.getJSONObject(i);
                    addElement(element);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}