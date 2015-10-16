package hram.kvarta;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import droidkit.content.TypedPrefs;

/**
 * @author Evgeny Hramov
 */
class AlarmManager {
    //private final static String TAG = "kvarta";

    public static void setAlarm(Context context) {
        Settings settings = TypedPrefs.from(context, Settings.class);
        android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent intent = new Intent(context, AlarmService.class);

        if(settings.enableRemind().get()) {
            Calendar calendar = getAlarmDate(settings);
            //SimpleDateFormat format = new SimpleDateFormat("dd.MM hh.mm");
            //Log.d(TAG, "New time " + format.format(calendar.getTime()));

            am.set(android.app.AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        } else {
            am.cancel(PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        }
    }

    private static Calendar getAlarmDate(Settings settings){
        Calendar calendar = Calendar.getInstance();
        if(calendar.get(Calendar.DAY_OF_MONTH) >= settings.remindDate().get()){
            calendar.add(Calendar.MONTH, 1);
        }
        calendar.set(Calendar.DAY_OF_MONTH, settings.remindDate().get());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        return calendar;
    }
}
