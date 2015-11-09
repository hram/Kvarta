package hram.kvarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.squareup.okhttp.OkHttpClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    SharedPreferences mPreferences;
    Account mAccount;
    AccountManager mAccountManager;

    /**
     * @return The current context.
     */
    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() {

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mAccount = new Account(getContext());
        mAccount.reset();

        OkHttpClient client = OkClient.create(getContext());
        assertThat(client, is(notNullValue()));

        mAccountManager = new AccountManager();
        assertThat(mAccountManager, is(notNullValue()));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLogin() {
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
    public void testLoginDemo() {
        assertThat(mAccountManager.logIn("000000000", "000000000", "демо", true), is(true));
    }
}
