package hram.kvarta.alarm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import droidkit.content.TypedPrefs;
import hram.kvarta.data.Settings;
import timber.log.Timber;

/**
 * @author Evgeny Hramov
 */
public class AlarmManager {
    private Context mContext;
    private Settings mSettings;

    public AlarmManager(Context context) {
        mContext = context;
        mSettings = TypedPrefs.from(context, Settings.class);
    }

    public boolean setAlarm() {
        return setAlarm(getAlarmDate().getTimeInMillis());
    }

    public boolean setAlarm(long triggerAtMillis) {
        android.app.AlarmManager am = (android.app.AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        final Intent intent = new Intent(mContext, AlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Timber.d("Enable remind: %s", mSettings.enableRemind().get());
        Timber.d("Remind date: %s", mSettings.remindDate().get());
        Timber.d("New alarm time: %s", new SimpleDateFormat("dd.MM hh.mm", Locale.US).format(triggerAtMillis));

        if (mSettings.enableRemind().get()) {
            am.set(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            am.cancel(pendingIntent);
        }

        return mSettings.enableRemind().get();
    }

    public Calendar getAlarmDate() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_MONTH) >= mSettings.remindDate().get()) {
            calendar.add(Calendar.MONTH, 1);
        }
        calendar.set(Calendar.DAY_OF_MONTH, mSettings.remindDate().get());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        return (Calendar) calendar.clone();
    }

    private Calendar getAlarmDateTest() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        return calendar;
    }
}
