package hram.kvarta.espresso;

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

import hram.kvarta.Account;
import hram.kvarta.BuildConfig;
import hram.kvarta.Constants;
import hram.kvarta.LoginActivity;
import hram.kvarta.MainActivity;
import hram.kvarta.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.times;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static hram.kvarta.espresso.CustomViewMatchers.isEmptyEditText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityLaunchesLoginActivityTest {

    private static final String TAG = "UIT";
    private SharedPreferences mPreferences;
    private Account mAccount;

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

    /**
     * При первом запуске должно открываться окно авторизации. Все поля пустые.
     *
     * @throws Exception
     */
    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity() throws Exception {
        try {
            mAccount.reset();
            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()));

            onView(withId(R.id.tsgid)).check(matches(isEmptyEditText()));
            onView(withId(R.id.accountid)).check(matches(isEmptyEditText()));
            onView(withId(R.id.password)).check(matches(isEmptyEditText()));
        } finally {
            Intents.release();
        }
    }

    @Test
    public void testWhenThereIsAnExistingAccount_DoesNotLaunchLoginActivity() throws Exception {
        try {
            Intents.init();
            mAccount = new Account.Builder()
                    .accountId(BuildConfig.accountid)
                    .tsgId(BuildConfig.tsgid)
                    .password(BuildConfig.password)
                    .build(getContext());

            rule.launchActivity(new Intent());

            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()), times(0));

        } finally {
            Intents.release();
        }
    }

    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_AndLogin() throws Exception {
        try {
            mAccount.reset();
            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()));

            onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
            onView(withId(R.id.accountid)).perform(replaceText(BuildConfig.accountid), closeSoftKeyboard());
            onView(withId(R.id.password)).perform(replaceText(BuildConfig.password), closeSoftKeyboard());

            onView(withId(R.id.sign_in_button)).perform(click());

            assertThat(mAccount.isValid(), is(true));
            assertThat(mAccount.isDemo(), is(false));
            assertThat(mAccount.getTsgId(), is(BuildConfig.tsgid));
            assertThat(mAccount.getAccountId(), is(BuildConfig.accountid));
            assertThat(mAccount.getPassword(), is(BuildConfig.password));
        } finally {
            Intents.release();
        }
    }

    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_AndLoginDemo() throws Exception {
        try {
            mAccount.reset();
            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()));

            onView(withId(R.id.sign_in_button_demo)).perform(click());

            assertThat(mAccount.isValid(), is(true));
            assertThat(mAccount.isDemo(), is(true));
            assertThat(mAccount.getUserInfo(), is(Constants.DEMO_NAME));
            assertThat(mAccount.getAddress(), is(Constants.DEMO_ADDR));

            onView(withId(R.id.tvAddress)).check(matches(withText(Constants.DEMO_ADDR)));
            onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.DEMO_NAME)));
        } finally {
            Intents.release();
        }
    }

    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_AndNotLogin() throws Exception {
        try {
            mAccount.reset();
            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()));
            pressBack();
            assertThat(mAccount.isValid(), is(false));
        } finally {
            Intents.release();
        }
    }
}
