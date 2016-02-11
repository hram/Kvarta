package hram.kvarta.espresso;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import hram.kvarta.BuildConfig;
import hram.kvarta.Constants;
import hram.kvarta.R;
import hram.kvarta.activity.MainActivity;
import hram.kvarta.data.Account;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
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

    private Account mAccount;

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Before
    public void setUp() {

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        mAccount = new Account(getContext());
    }

    @After
    public void tearDown() {
    }

    private void signIn() throws InterruptedException {
        onView(withId(R.id.sign_in_button)).perform(click());
        Thread.sleep(2000);
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
        mAccount.reset();
        rule.launchActivity(new Intent());

        onView(withId(R.id.tsgid)).check(matches(isEmptyEditText()));
        onView(withId(R.id.accountid)).check(matches(isEmptyEditText()));
        onView(withId(R.id.password)).check(matches(isEmptyEditText()));
    }

    @Test
    public void testWhenThereIsAnExistingAccount_DoesNotLaunchLoginActivity() throws Exception {
        mAccount = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .build(getContext());

        rule.launchActivity(new Intent());
    }

    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_AndLogin() throws Exception {
        mAccount.reset();
        rule.launchActivity(new Intent());

        onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
        onView(withId(R.id.accountid)).perform(replaceText(BuildConfig.accountid), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText(BuildConfig.password), closeSoftKeyboard());

        signIn();

        assertThat(mAccount.isValid(), is(true));
        assertThat(mAccount.isDemo(), is(false));
        assertThat(mAccount.getTsgId(), is(BuildConfig.tsgid));
        assertThat(mAccount.getAccountId(), is(BuildConfig.accountid));
        assertThat(mAccount.getPassword(), is(BuildConfig.password));
    }

    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_AndLoginDemo() throws Exception {
        mAccount.reset();
        rule.launchActivity(new Intent());

        onView(withId(R.id.sign_in_button_demo)).perform(click());
        Thread.sleep(2000);

        assertThat(mAccount.isValid(), is(true));
        assertThat(mAccount.isDemo(), is(true));
        assertThat(mAccount.getUserInfo(), is(Constants.DEMO_NAME));
        assertThat(mAccount.getAddress(), is(Constants.DEMO_ADDR));

        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.DEMO_ADDR)));
        onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.DEMO_NAME)));
    }

    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_AndNotLogin() throws Exception {
        mAccount.reset();
        rule.launchActivity(new Intent());
        pressBack();
        assertThat(mAccount.isValid(), is(false));
    }
}
