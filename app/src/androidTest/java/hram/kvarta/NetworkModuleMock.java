package hram.kvarta;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hram.kvarta.network.AccountManager;
import hram.kvarta.network.OkClient;
import hram.kvarta.network.ValuesManager;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;

/**
 * @author Evgeny Khramov
 */
@Module(injects = {AccountManager.class, ValuesManager.class})
public class NetworkModuleMock {

    private final Context mContext;
    private final MockWebServer mServer;

    public NetworkModuleMock(Context context, MockWebServer server) {
        mContext = context;
        mServer = server;
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    OkHttpClient provideClient() {
        return OkClient.create(mContext);
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    HttpUrl provideUrl() {
        return mServer.url("/");
    }
}
