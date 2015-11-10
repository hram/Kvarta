package hram.kvarta.di;

import hram.kvarta.network.AccountManager;

/**
 * @author Evgeny Khramov
 */
//@Module(injects = {AccountManager.class, ValuesManager.class})
public class AccountModule {

    //@Provides
    AccountManager provideAccountManager() {
        return new AccountManager();
    }
}
