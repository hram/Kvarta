package hram.kvarta;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import droidkit.util.Sequence;

/**
 * @author Evgeny Khramov
 */
public class AlarmService extends IntentService {

    public static final int NOTIFICATION_ID = Sequence.get().nextInt();
    private AlarmManager mAlarmManager;

    public AlarmService() {
        super(AlarmService.class.getName());
        mAlarmManager = new AlarmManager(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mAlarmManager.setAlarm(mAlarmManager.getAlarmDate().getTimeInMillis());
        showNotification();
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification))
                .setTicker(getString(R.string.notification));

        builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;

        NotificationManager ntfMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        ntfMan.notify(NOTIFICATION_ID, notification);
    }
}
