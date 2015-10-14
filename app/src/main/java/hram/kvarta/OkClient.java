package hram.kvarta;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

/**
 * @author Evgeny Hramov
 */
public class OkClient {
    public static OkHttpClient create(Context context) {
        OkHttpClient client = new OkHttpClient();

        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
        client.networkInterceptors().add(new InterceptorWin1251());

        client.setCookieHandler(new CookieManager(new PersistentCookieStore(context), CookiePolicy.ACCEPT_ALL));

        return client;
    }
}
