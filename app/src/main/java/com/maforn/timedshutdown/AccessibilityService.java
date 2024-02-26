package com.maforn.timedshutdown;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.Path;
import android.os.Build;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import java.util.List;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    SharedPreferences sharedPreferences;

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        if (!(paramIntent.getBooleanExtra("exec_gesture", false) || paramIntent.getBooleanExtra("exec_gesture2", false))) {
            if (!performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)) {
                Toast.makeText(this, R.string.not_performed, Toast.LENGTH_SHORT).show();
            }
        }

        if (paramIntent.getBooleanExtra("exec_gesture", false)) {
            sharedPreferences = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
            float x1 = sharedPreferences.getFloat("X_ABS_false", 100);
            float y1 = sharedPreferences.getFloat("Y_ABS_false", 100);
            float x2 = -1, y2 = -1;
            int duration = 200;

            PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];
            if (power_off_type != PowerOffType.ONECLICK) {
                x2 = sharedPreferences.getFloat("X_ABS_true", 100);
                y2 = sharedPreferences.getFloat("Y_ABS_true", 100);
            }

            if (power_off_type == PowerOffType.LONGPRESS) {
                duration = 5000;
            }
            if (!this.dispatchGesture(createClick(x1, y1, x2, y2, power_off_type == PowerOffType.SWIPE, duration), null, null)) {
                Toast.makeText(this, R.string.not_performed, Toast.LENGTH_SHORT).show();
            }
        }

        if (paramIntent.getBooleanExtra("exec_gesture2", false)) {
            sharedPreferences = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
            float x2 = -1, y2 = -1;
            int duration = 200;

            PowerOffType power_off_type = PowerOffType.values[(sharedPreferences.getInt("power_off_method", PowerOffType.ONECLICK.ordinal()))];            if (power_off_type != PowerOffType.ONECLICK) {
                x2 = sharedPreferences.getFloat("X_ABS_true", 100);
                y2 = sharedPreferences.getFloat("Y_ABS_true", 100);
            }

            // if two clicks were required
            if (power_off_type == PowerOffType.TWOCLICKS) {
                if (!this.dispatchGesture(createClick(x2, y2, x2, y2, false, duration), null, null)) {
                    Toast.makeText(this, R.string.not_performed, Toast.LENGTH_SHORT).show();
                }
            }
        }

        return Service.START_STICKY;
    }

    // (x, y) in screen coordinates
    private static GestureDescription createClick(float x1, float y1, float x2, float y2, boolean swipe, int duration) {

        Path clickPath = new Path();
        clickPath.moveTo(x1, y1);
        // if it's not a swipe it's going to click the single place
        if (swipe)
            clickPath.lineTo(x2, y2);
        GestureDescription.StrokeDescription clickStroke =
                new GestureDescription.StrokeDescription(clickPath, 0, duration);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);

        return clickBuilder.build();
    }

    public static void requireAccessibility(Context context) {
        AlertDialog alertDialog = (new AlertDialog.Builder(context)).create();
        alertDialog.setTitle(context.getString(R.string.alert_permission_title));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            alertDialog.setMessage(context.getString(R.string.alert_permission_text));
        else
            alertDialog.setMessage(context.getString(R.string.alert_permission_text_API33));
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

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends android.accessibilityservice.AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }

        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
