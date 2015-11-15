package hram.kvarta.events;

import com.squareup.otto.Bus;

/**
 * @author Evgeny Khramov
 */
public final class BusProvider {
    private static final Bus BUS = new MainThreadBus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
