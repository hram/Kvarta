package hram.kvarta.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import droidkit.content.TypedPrefs;
import hram.kvarta.alarm.AlarmManager;
import hram.kvarta.data.Settings;

/**
 * @author Evgeny Khramov
 */
public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            TypedPrefs.from(context, Settings.class).lastRebootTime().set(System.currentTimeMillis());
            new AlarmManager(context.getApplicationContext()).setAlarm();
        }
    }
}
