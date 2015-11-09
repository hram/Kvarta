package hram.kvarta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.intent.Intents.times;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityLaunchesLoginActivityTest {

    private static final String TAG = "UIT";
    SharedPreferences mPreferences;
    Account mAccount;

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Before
    public void setUp() {
        Log.v(TAG, "Test was set up!");

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mAccount = new Account(getContext());
    }

    @After
    public void tearDown() {
        Log.v(TAG, "Test was torn down!");
    }

    /**
     * @return The current context.
     */
    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity() throws Exception {
        mAccount.reset();
        Intents.init();
        rule.launchActivity(new Intent());
        intended(hasComponent(LoginActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void testWhenThereIsAnExistingAccount_DoesNotLaunchLoginActivity() throws Exception {
        Intents.init();

        mAccount = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .build(getContext());

        rule.launchActivity(new Intent());

        intended(hasComponent(MainActivity.class.getName()));
        intended(hasComponent(LoginActivity.class.getName()), times(0));

        Intents.release();
    }
}
