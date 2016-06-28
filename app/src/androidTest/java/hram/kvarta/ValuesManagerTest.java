package hram.kvarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import hram.kvarta.data.Account;
import hram.kvarta.network.AccountManager;
import hram.kvarta.network.ValuesManager;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ValuesManagerTest {

    private Account mAccount;

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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        assertThat(preferences, is(notNullValue()));

        mAccount = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(getContext());
        assertThat(mAccount, is(notNullValue()));

        AccountManager accountManager = new AccountManager();
        assertThat(accountManager, is(notNullValue()));

        assertThat(accountManager.logIn(mAccount), is(true));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetValues() throws SocketTimeoutException {
        ValuesManager valuesManager = new ValuesManager();
        assertThat(valuesManager, is(notNullValue()));

        assertThat(valuesManager.getValues(mAccount), is(true));

        assertThat(valuesManager.getValue(ValuesManager.WATER_HOT), is(152L));
        assertThat(valuesManager.getValue(ValuesManager.WATER_COLD), is(271L));
    }
}
