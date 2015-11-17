package hram.kvarta.events;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * @author Evgeny Khramov
 */
public class MainThreadBus extends Bus {

    public MainThreadBus() {
        this(ThreadEnforcer.ANY);
    }

    public MainThreadBus(ThreadEnforcer enforcer) {
        super(enforcer);
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainThreadBus.super.post(event);
                }
            });
        }
    }
}
