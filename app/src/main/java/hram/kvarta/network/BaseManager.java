package hram.kvarta.network;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import hram.kvarta.di.Injector;

/**
 * @author Evgeny Khramov
 */
public class BaseManager {

    @Inject
    protected OkHttpClient mClient;

    @Inject
    protected HttpUrl mHttpUrl;

    public BaseManager() {
        Injector.inject(this);
    }

    protected URL createUrl(String path) throws MalformedURLException {
        return new URL(mHttpUrl.url(), path);
    }
}
