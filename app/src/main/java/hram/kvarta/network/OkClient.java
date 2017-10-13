package hram.kvarta.network;

import android.support.annotation.NonNull;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.net.CookieManager;
import java.util.concurrent.TimeUnit;

import hram.kvarta.BuildConfig;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;

/**
 * @author Evgeny Hramov
 */
public class OkClient {

    private OkClient() {
    }

    public static final OkHttpClient CLIENT;

    static {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(new InterceptorWin1251())
                .cookieJar(new JavaNetCookieJar(CookieManager.getDefault()));

        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(new StethoInterceptor());
        }

        CLIENT = builder.build();
    }

    @NonNull
    public static OkHttpClient create() {
        return CLIENT;
    }
}
