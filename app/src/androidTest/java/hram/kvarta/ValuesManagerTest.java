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
    public void setUp() throws MalformedURLException {

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mAccount = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(getContext());
        assertThat(mAccount, is(notNullValue()));

        mAccountManager = new AccountManager();
        assertThat(mAccountManager, is(notNullValue()));

        assertThat(mAccountManager.logIn(mAccount), is(true));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetValues() {
        ValuesManager valuesManager = new ValuesManager();
        assertThat(valuesManager, is(notNullValue()));

        assertThat(valuesManager.getValues(mAccount), is(true));

        assertThat(valuesManager.getValue(ValuesManager.WATER_HOT), is(124L));
        assertThat(valuesManager.getValue(ValuesManager.WATER_COLD), is(221L));
    }
}
