package hram.kvarta;

import android.content.Context;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hram.kvarta.network.AccountManager;
import hram.kvarta.network.OkClient;
import hram.kvarta.network.ValuesManager;

/**
 * @author Evgeny Khramov
 */
@Module(injects = {AccountManager.class, ValuesManager.class})
public class NetworkModuleMock {

    private Context mContext;
    private MockWebServer mServer;

    public NetworkModuleMock(Context context, MockWebServer server) {
        mContext = context;
        mServer = server;
    }

    @Provides
    @Singleton
    OkHttpClient provideClient() {
        return OkClient.create(mContext);
    }

    @Provides
    @Singleton
    HttpUrl provideUrl() {
        return mServer.url("/");
    }
}
