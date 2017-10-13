package hram.kvarta.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hram.kvarta.BuildConfig;
import hram.kvarta.network.AccountManager;
import hram.kvarta.network.OkClient;
import hram.kvarta.network.ValuesManager;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * @author Evgeny Khramov
 */
@Module(injects = {AccountManager.class, ValuesManager.class})
public class NetworkModule {

    private Context mContext;

    public NetworkModule(Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    OkHttpClient provideClient() {
        return OkClient.create();
    }

    @Provides
    @Singleton
    HttpUrl provideUrl() {
        return new HttpUrl.Builder()
                .scheme("http")
                .host(BuildConfig.HOST)
                .build();
    }
}
