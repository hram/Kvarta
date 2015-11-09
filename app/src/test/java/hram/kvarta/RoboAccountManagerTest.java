package hram.kvarta;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import hram.kvarta.network.AccountManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Evgeny Khramov
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RoboAccountManagerTest {

    Account mAccount;
    AccountManager mAccountManager;

    private Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {

        OkHttpClient client = OkClient.create(getContext());
        assertNotNull(client);

        mAccountManager = new AccountManager(client);
        assertNotNull(mAccountManager);
    }

    @Test
    public void testLogin() throws Exception {
        assertTrue(mAccountManager.logIn(BuildConfig.tsgid, BuildConfig.accountid, BuildConfig.password, false));

        Account account = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(getContext());
        assertNotNull(account);

        assertTrue(mAccountManager.logIn(account));
    }

    @Test
    public void testLoginDemo() {
        assertTrue(mAccountManager.logIn("000000000", "000000000", "демо", true));
    }
}
