package hram.kvarta;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;

import hram.kvarta.data.Account;
import hram.kvarta.network.AccountManager;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccountManagerTest {

    private AccountManager mAccountManager;

    /**
     * @return The current context.
     */
    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws MalformedURLException {

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        Account account = new Account(getContext());
        account.reset();

        mAccountManager = new AccountManager();
        assertThat(mAccountManager, is(notNullValue()));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLogin() throws MalformedURLException {
        assertThat(mAccountManager.logIn(BuildConfig.tsgid, BuildConfig.accountid, BuildConfig.password, false), is(true));

        Account account = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(getContext());
        assertThat(account, is(notNullValue()));

        assertThat(mAccountManager.logIn(account), is(true));
    }

    @Test
    public void testLoginFailed() throws MalformedURLException {
        assertThat(mAccountManager.logIn(BuildConfig.tsgid, BuildConfig.accountid, "000", false), is(false));

        Account account = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password("000")
                .demo(false)
                .build(getContext());
        assertThat(account, is(notNullValue()));

        assertThat(mAccountManager.logIn(account), is(false));
    }

    @Test
    public void testLoginDemo() throws MalformedURLException {
        assertThat(mAccountManager.logIn("000000000", "000000000", "демо", true), is(true));
    }
}
