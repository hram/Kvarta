package hram.kvarta;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import droidkit.content.TypedPrefs;
import hram.kvarta.di.Injector;
import hram.kvarta.di.NetworkModule;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * @author Evgeny Khramov
 */
public class KvartaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        if (BuildConfig.crashlyticsEnabled) {
            Fabric.with(this, new Crashlytics());
        }
        TypedPrefs.setupDefaults(this, Settings.class);
        Injector.init(new NetworkModule(this));
    }
}
