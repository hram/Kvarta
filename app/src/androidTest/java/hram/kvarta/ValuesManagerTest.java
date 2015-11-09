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
    ValuesManager mValuesManager;

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
        mAccount = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(getContext());
        assertThat(mAccount, is(notNullValue()));

        OkHttpClient client = OkClient.create(getContext());
        assertThat(client, is(notNullValue()));

        mAccountManager = new AccountManager();
        assertThat(mAccountManager, is(notNullValue()));

        assertThat(mAccountManager.logIn(mAccount),is(true));

        mValuesManager = new ValuesManager(client, mAccount);
        assertThat(mValuesManager, is(notNullValue()));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetValues() {
        assertThat(mValuesManager.getValues(), is(true));

        assertThat(mValuesManager.getHotValue(0), is(124L));
        assertThat(mValuesManager.getColdValue(0), is(221L));
    }
}
