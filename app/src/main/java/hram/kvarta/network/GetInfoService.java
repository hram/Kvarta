package hram.kvarta.network;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;

import hram.kvarta.Account;
import hram.kvarta.BuildConfig;
import hram.kvarta.events.BusProvider;
import hram.kvarta.events.LoadDataErrorEvent;
import hram.kvarta.events.LoadDataStartedEvent;
import hram.kvarta.events.LogInErrorEvent;
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
    Bus bus = BusProvider.getInstance();

    public GetInfoService() {
        super(GetInfoService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.i("Starting sync...");

        bus.post(new LoadDataStartedEvent());

        if (!NetworkUtil.isNetworkConnected(this)) {
            Timber.i("Sync canceled, connection not available");
            bus.post(new LoadDataErrorEvent());
            AndroidComponentUtil.toggleComponent(this, SyncOnConnectionAvailable.class, true);
            return;
        }

        mAccount = new Account(this);

        if (mValuesManager.getValues(mAccount)) {
            bus.post(mValuesManager.createLoadDataEndedEvent());
            return;
        }

        if(!mAccountManager.logIn(mAccount)){
            bus.post(new LogInErrorEvent());
            return;
        }

        if(!mValuesManager.getValues(mAccount)){
            bus.post(new LoadDataErrorEvent());
            return;
        }

        bus.post(mValuesManager.createLoadDataEndedEvent());
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
