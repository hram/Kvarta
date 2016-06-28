package hram.kvarta.network;

import android.content.Context;

import java.net.CookieManager;
import java.util.concurrent.TimeUnit;

import hram.kvarta.BuildConfig;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author Evgeny Hramov
 */
public class OkClient {

    public static final OkHttpClient CLIENT;

    static {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(new InterceptorWin1251())
                .cookieJar(new JavaNetCookieJar(CookieManager.getDefault()));

        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addNetworkInterceptor(logging);
        }

        CLIENT = builder.build();
    }

    public static OkHttpClient create(Context context) {
        return CLIENT;
    }
}
