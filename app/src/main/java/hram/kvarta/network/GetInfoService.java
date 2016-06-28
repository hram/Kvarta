package hram.kvarta.network;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import java.net.SocketTimeoutException;

import hram.kvarta.BuildConfig;
import hram.kvarta.data.Account;
import hram.kvarta.events.LoadDataErrorEvent;
import hram.kvarta.events.LoadDataStartedEvent;
import hram.kvarta.events.LogInErrorEvent;
import hram.kvarta.events.NetworkErrorEvent;
import hram.kvarta.util.AndroidComponentUtil;
import timber.log.Timber;

/**
 * @author Evgeny Khramov
 */
public class GetInfoService extends IntentService {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, GetInfoService.class);
    }

    public static boolean isRunning(Context context) {
        return AndroidComponentUtil.isServiceRunning(context, GetInfoService.class);
    }

    Account mAccount;
    AccountManager mAccountManager = new AccountManager();
    ValuesManager mValuesManager = new ValuesManager();

    public GetInfoService() {
        super(GetInfoService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.i("Starting sync...");

        EventBus.getDefault().post(new LoadDataStartedEvent());

        if (!NetworkUtil.isNetworkConnected(this)) {
            Timber.i("Sync canceled, connection not available");
            EventBus.getDefault().post(new LoadDataErrorEvent());
            AndroidComponentUtil.toggleComponent(this, SyncOnConnectionAvailable.class, true);
            return;
        }

        mAccount = new Account(this);

        try {
            if (mValuesManager.getValues(mAccount)) {
                EventBus.getDefault().post(mValuesManager.createLoadDataEndedEvent());
                return;
            }

            if (!mAccountManager.logIn(mAccount)) {
                EventBus.getDefault().post(new LogInErrorEvent());
                return;
            }

            if (!mValuesManager.getValues(mAccount)) {
                EventBus.getDefault().post(new LoadDataErrorEvent());
                return;
            }

            EventBus.getDefault().post(mValuesManager.createLoadDataEndedEvent());
        } catch (SocketTimeoutException e) {
            EventBus.getDefault().post(new NetworkErrorEvent());
        }
    }

    public static class SyncOnConnectionAvailable extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtil.isNetworkConnected(context)) {
                if (BuildConfig.DEBUG) {
                    Timber.i("Connection is now available, triggering sync...");
                }
                AndroidComponentUtil.toggleComponent(context, this.getClass(), false);
                context.startService(getStartIntent(context));
            }
        }
    }
}
