package com.maforn.timedshutdown;

import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Build;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    SharedPreferences sharedPreferences;

    /**
     * This function will execute the required power off gestures based on the extra params
     * that are passed through the Intent. If nothing is passed then it will call the power off
     * dialog, if exec_gesture is passed then it will execute the first gesture and if exec_gesture2
     * is passed it will execute the second gesture (the second click)
     *
     * @param paramIntent the Intent that started this Service
     */
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        // nothing is passed: call the power off dialog
        if (!(paramIntent.getBooleanExtra("exec_gesture", false) || paramIntent.getBooleanExtra("exec_gesture2", false) || paramIntent.getBooleanExtra("exec_gesture3", false) || paramIntent.getBooleanExtra("exec_gesture4", false) || paramIntent.getBooleanExtra("exec_gesture5", false) || paramIntent.getBooleanExtra("exec_gesture6", false))) {
            if (!performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)) {
                // action failed: accessibility permission is missing
                Toast.makeText(this, R.string.not_performed, Toast.LENGTH_SHORT).show();
            }
        }


        sharedPreferences = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];

        // first gesture is required
        if (paramIntent.getBooleanExtra("exec_gesture", false)) {
            // get type of gesture and coordinates
            float x1 = sharedPreferences.getFloat("X_ABS_false", 100);
            float y1 = sharedPreferences.getFloat("Y_ABS_false", 100);
            float x2 = -1, y2 = -1;
            int duration = 200;

            // if it's a swipe set up the second point to swipe to
            if (power_off_type == PowerOffType.SWIPE) {
                x2 = sharedPreferences.getFloat("X_ABS_true", 100);
                y2 = sharedPreferences.getFloat("Y_ABS_true", 100);
            }

            // if it is a long press augment the duration
            if (power_off_type == PowerOffType.LONGPRESS) {
                duration = 5000;
            }
            // dispatch the power off gesture
            if (!this.dispatchGesture(createGesture(x1, y1, x2, y2, power_off_type == PowerOffType.SWIPE, duration), null, null)) {
                // dispathGesture failed: accessibility permission is missing
                Toast.makeText(this, R.string.not_performed, Toast.LENGTH_SHORT).show();
            }
        }

        // the second gesture is required
        if (paramIntent.getBooleanExtra("exec_gesture2", false) || paramIntent.getBooleanExtra("exec_gesture3", false) || paramIntent.getBooleanExtra("exec_gesture4", false) || paramIntent.getBooleanExtra("exec_gesture5", false) || paramIntent.getBooleanExtra("exec_gesture6", false)) {
            sharedPreferences = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
            float x2 = -1, y2 = -1;
            int duration = 200;

            String clickType = paramIntent.getBooleanExtra("exec_gesture2", false) ? "true" :  paramIntent.getBooleanExtra("exec_gesture3", false) ? "three" : paramIntent.getBooleanExtra("exec_gesture4", false) ? "four" :  paramIntent.getBooleanExtra("exec_gesture5", false) ? "five" : "six";
            // set up the coordinates for the second gestures
            if (power_off_type != PowerOffType.ONECLICK) {
                x2 = sharedPreferences.getFloat("X_ABS_" + clickType, 100);
                y2 = sharedPreferences.getFloat("Y_ABS_" + clickType, 100);
            }

            // if two clicks were required
            if (power_off_type == PowerOffType.TWOCLICKS || power_off_type == PowerOffType.THREECLICKS || power_off_type == PowerOffType.FOURCLICKS || power_off_type == PowerOffType.FIVECLICKS || power_off_type == PowerOffType.SIXCLICKS) {
                if (!this.dispatchGesture(createGesture(x2, y2, x2, y2, false, duration), null, null)) {
                    // dispathGesture failed: accessibility permission is missing
                    Toast.makeText(this, R.string.not_performed, Toast.LENGTH_SHORT).show();
                }
            }
        }

        return Service.START_STICKY;
    }

    /**
     * This function will create the gesture description for the accessibility service's function
     * dispatchGesture. If it is a swipe it will create a line and transform it to a gesture, else
     * it will just create a point (a click)
     *
     * @param x1       x screen coordinate of point 1
     * @param y1       y screen coordinate of point 1
     * @param x2       x screen coordinate of point 2
     * @param y2       y screen coordinate of point 2
     * @param swipe    boolean: if it is a swipe (true) or just a click (false)
     * @param duration the duration of the specified gesture
     * @return the built GestureDescription from the Stroke
     */
    private static GestureDescription createGesture(float x1, float y1, float x2, float y2, boolean swipe, int duration) {
        Path clickPath = new Path();
        // (x, y) are screen coordinates
        clickPath.moveTo(x1, y1);
        // if it's a swipe the path is going to be a line from the first to the second point
        if (swipe)
            clickPath.lineTo(x2, y2);
        // else it's just a click on the point, create a Gesture Stroke from the Path
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, duration);
        // create a gesture from the stroke
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(clickStroke);
        return gestureBuilder.build();
    }

    /**
     * This function will require the Accessibility permission from the user with a AlertDialog.
     * Based on the version of Android the required actions will slightly differ, so we have two
     * different possible toasts.
     */
    public static void requireAccessibility(Context context) {
        AlertDialog alertDialog = (new AlertDialog.Builder(context)).create();
        alertDialog.setTitle(context.getString(R.string.alert_permission_title));
        // switch text based on android version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            alertDialog.setMessage(context.getString(R.string.alert_permission_text));
        else
            alertDialog.setMessage(context.getString(R.string.alert_permission_text_API33));
        // open the settings at the accessibility tab
        alertDialog.setButton(-1, context.getString(R.string.alert_permission_SETTINGS), (paramDialogInterface, paramInt) -> {
            try {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                context.startActivity(intent);
            } catch (Exception ignored) {
            }
            paramDialogInterface.dismiss();
        });
        alertDialog.setButton(-2, context.getString(R.string.alert_permission_cancel), (paramDialogInterface, paramInt) -> paramDialogInterface.dismiss());
        alertDialog.show();
    }

    /**
     * A function that returns true if this app has the accessibility service permission enabled
     *
     * @param context the application context
     * @param service type of service to check
     * @return a boolean: is accessibility enabled? -> true or false
     */
    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends android.accessibilityservice.AccessibilityService> service) {
        // Get the list of enabled accessibility services from Settings.Secure
        String enabledServicesSetting = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        // Check to see if the enabled services list contains the sample service
        return enabledServicesSetting != null && enabledServicesSetting.contains(context.getPackageName() + "/" + service.getName());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
