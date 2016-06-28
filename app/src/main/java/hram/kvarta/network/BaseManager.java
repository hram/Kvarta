package hram.kvarta.network;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import hram.kvarta.di.Injector;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

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
