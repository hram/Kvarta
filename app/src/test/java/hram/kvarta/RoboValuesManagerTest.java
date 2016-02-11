package hram.kvarta;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import hram.kvarta.data.Account;
import hram.kvarta.network.AccountManager;
import hram.kvarta.network.ValuesManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Evgeny Khramov
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RoboValuesManagerTest {

    Account mAccount;
    AccountManager mAccountManager;
    ValuesManager mValuesManager;

    @Before
    public void setUp() throws Exception {
        mAccount = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(RuntimeEnvironment.application);
        assertNotNull(mAccount);

        mAccountManager = new AccountManager();
        assertNotNull(mAccountManager);

        assertTrue(mAccountManager.logIn(mAccount));

        mValuesManager = new ValuesManager(2);
        assertNotNull(mValuesManager);
    }

    @Test
    public void testGetValues() throws Exception {
        assertTrue(mValuesManager.getValues(mAccount));

        assertEquals(mValuesManager.getValue(ValuesManager.WATER_HOT, 0), 124L);
        assertEquals(mValuesManager.getValue(ValuesManager.WATER_COLD, 0), 221L);
    }
}
