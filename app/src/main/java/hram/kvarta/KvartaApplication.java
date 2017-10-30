package hram.kvarta;

import android.app.Application;

import com.facebook.stetho.Stetho;

import java.net.CookieManager;
import java.net.CookiePolicy;

import droidkit.content.TypedPrefs;
import hram.kvarta.data.Settings;
import hram.kvarta.di.Injector;
import hram.kvarta.di.NetworkModule;
import hram.kvarta.network.PersistentCookieStore;
import hram.kvarta.util.CrashlyticsUtil;
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
            Stetho.initializeWithDefaults(this);
        }
        CrashlyticsUtil.init(this);
        CookieManager.setDefault(new CookieManager(new PersistentCookieStore(this), CookiePolicy.ACCEPT_ALL));
        TypedPrefs.setupDefaults(this, Settings.class);
        Injector.init(new NetworkModule(this));
    }
}
