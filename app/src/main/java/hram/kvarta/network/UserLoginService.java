package hram.kvarta.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import droidkit.content.BoolValue;
import droidkit.content.StringValue;
import droidkit.content.TypedBundle;
import droidkit.content.Value;
import hram.kvarta.data.Account;
import hram.kvarta.events.UserLoginEndedEvent;
import hram.kvarta.events.UserLoginErrorEvent;
import hram.kvarta.events.UserLoginStartedEvent;
import hram.kvarta.util.CrashlyticsUtil;
import timber.log.Timber;

/**
 * @author Evgeny Khramov
 */
public class UserLoginService extends IntentService {

    public static Intent getStartIntent(Context context, String tsgID, String accountID, String password, boolean demo) {
        Bundle bundle = new Bundle();
        Args args = TypedBundle.from(bundle, Args.class);
        args.tsgID().set(tsgID);
        args.accountID().set(accountID);
        args.password().set(password);
        args.demo().set(demo);
        Intent intent = new Intent(context, UserLoginService.class);
        intent.putExtras(bundle);
        return intent;
    }

    public static Intent getStartDemoIntent(Context context) {
        Bundle bundle = new Bundle();
        Args args = TypedBundle.from(bundle, Args.class);
        args.tsgID().set("000000000");
        args.accountID().set("000000000");
        args.password().set("демо");
        args.demo().set(true);
        Intent intent = new Intent(context, UserLoginService.class);
        intent.putExtras(bundle);
        return intent;
    }

    public UserLoginService() {
        super(UserLoginService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.i("Старт авторизации");

        EventBus.getDefault().post(new UserLoginStartedEvent());
/*
        if (mArgs.toMockNetwork().get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            return mArgs.networkResult().get();
        }
*/
        Args args = TypedBundle.from(intent.getExtras(), Args.class);
        if (!NetworkUtil.isNetworkConnected(this)) {
            Timber.i("Login canceled, connection not available");
            EventBus.getDefault().post(new UserLoginErrorEvent());
            return;
        }

        if (!new AccountManager().logIn(args.tsgID().get(), args.accountID().get(), args.password().get(), args.demo().get())) {
            CrashlyticsUtil.logLogin(CrashlyticsUtil.LOGIN_PASSWORD, false);
            EventBus.getDefault().post(new UserLoginErrorEvent());
            return;
        }

        new Account.Builder()
                .accountId(args.accountID().get())
                .tsgId(args.tsgID().get())
                .password(args.password().get())
                .demo(args.demo().get())
                .build(getApplicationContext());

        CrashlyticsUtil.logLogin(CrashlyticsUtil.LOGIN_PASSWORD, true);
        EventBus.getDefault().post(new UserLoginEndedEvent());
    }

    interface Args {
        @Value
        StringValue tsgID();

        @Value
        StringValue accountID();

        @Value
        StringValue password();

        @Value
        BoolValue demo();
    }
}
