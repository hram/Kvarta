package hram.kvarta;

import android.app.Application;

import droidkit.content.TypedPrefs;
import hram.kvarta.di.Injector;
import hram.kvarta.di.NetworkModule;

/**
 * @author Evgeny Khramov
 */
public class KvartaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TypedPrefs.setupDefaults(this, Settings.class);
        Injector.init(new NetworkModule(this));//, new AccountModule());
    }
}
