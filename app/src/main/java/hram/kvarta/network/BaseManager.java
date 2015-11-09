package hram.kvarta.network;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;

import hram.kvarta.Injector;

/**
 * @author Evgeny Khramov
 */
public class BaseManager {

    @Inject
    protected OkHttpClient mClient;

    public BaseManager() {
        Injector.inject(this);
    }
/*
    @Inject
    public BaseManager(OkHttpClient client) {
        mClient = client;
    }
    */
}
