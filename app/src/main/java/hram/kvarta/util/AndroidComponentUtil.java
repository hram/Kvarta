package hram.kvarta.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import hram.kvarta.BuildConfig;
import timber.log.Timber;

/**
 * @author Evgeny Khramov
 */
public class AndroidComponentUtil {

    private AndroidComponentUtil() {
    }

    public static void toggleComponent(Context context, Class componentClass, boolean enable) {
        if (BuildConfig.DEBUG) {
            Timber.i((enable ? "Enabling " : "Disabling ") + componentClass.getSimpleName());
        }
        ComponentName componentName = new ComponentName(context, componentClass);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName, enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static boolean isServiceRunning(Context context, Class serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
