package hram.kvarta.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.squareup.otto.Bus;

import droidkit.content.StringValue;
import droidkit.content.TypedBundle;
import droidkit.content.Value;
import hram.kvarta.data.Account;
import hram.kvarta.events.BusProvider;
import hram.kvarta.events.SaveDataErrorEvent;
import hram.kvarta.events.SaveDataStartedEvent;
import hram.kvarta.util.AndroidComponentUtil;
import timber.log.Timber;

/**
 * @author Evgeny Khramov
 */
public class SaveInfoService extends IntentService {

    public static Intent getStartIntent(Context context, String newValueHot, String newValueCold) {
        Bundle bundle = new Bundle();
        Args args = TypedBundle.from(bundle, Args.class);
        args.newValueCold().set(newValueCold);
        args.newValueHot().set(newValueHot);
        Intent intent = new Intent(context, SaveInfoService.class);
        intent.putExtras(bundle);
        return intent;
    }

    public static boolean isRunning(Context context) {
        return AndroidComponentUtil.isServiceRunning(context, SaveInfoService.class);
    }

    private ValuesManager mValuesManager = new ValuesManager(2);
    private Bus bus = BusProvider.getInstance();

    public SaveInfoService() {
        super(SaveInfoService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Старт сервиса сохранеия значений");

        bus.post(new SaveDataStartedEvent());

        Args args = TypedBundle.from(intent.getExtras(), Args.class);
        if (!NetworkUtil.isNetworkConnected(this)) {
            Timber.i("Sync canceled, connection not available");
            bus.post(new SaveDataErrorEvent());
            return;
        }

        bus.post(mValuesManager.saveValues(new Account(this), args.newValueHot().get(), args.newValueCold().get()) ? mValuesManager.createLoadDataEndedEvent() : new SaveDataErrorEvent());
    }

    interface Args {
        @Value
        StringValue newValueHot();

        @Value
        StringValue newValueCold();
    }
}
