package hram.kvarta.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;

import java.util.Calendar;

import hram.kvarta.BuildConfig;
import io.fabric.sdk.android.Fabric;

/**
 * @author Evgeny Khramov
 */

public class CrashlyticsUtil {

    public static final String LOGIN_PASSWORD = "PASSWORD";

    public static void init(@NonNull Context context) {
        if (!isEnabled()) {
            return;
        }

        Fabric.with(context.getApplicationContext(), new Crashlytics());
    }

    private static boolean isEnabled() {
        return BuildConfig.crashlyticsEnabled;
    }

    public static void logException(@NonNull Throwable throwable) {
        if (!isEnabled()) {
            return;
        }

        Crashlytics.logException(throwable);
    }

    public static void log(@NonNull String msg) {
        if (!isEnabled()) {
            return;
        }

        Crashlytics.log(msg);
    }

    public static void logLogin(@NonNull String method, boolean success) {
        if (!isEnabled()) {
            return;
        }

        Answers.getInstance().logLogin(new LoginEvent().putMethod(method).putSuccess(success));
    }

    public static void getValues() {
        if (!isEnabled()) {
            return;
        }

        Answers.getInstance().logCustom(new CustomEvent("Get Values")
                .putCustomAttribute("Day of month", "" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
    }

    public static void saveValues() {
        if (!isEnabled()) {
            return;
        }

        Answers.getInstance().logCustom(new CustomEvent("Save Values")
                .putCustomAttribute("Day of month", "" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
    }
}
