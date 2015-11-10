package hram.kvarta.di;

import android.content.Context;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hram.kvarta.OkClient;
import hram.kvarta.network.AccountManager;
import hram.kvarta.network.ValuesManager;

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
        return OkClient.create(mContext);
    }

    @Provides
    @Singleton
    HttpUrl provideUrl() {
        return new HttpUrl.Builder().scheme("http").host("www2.kvarta-c.ru").build();
    }
}
