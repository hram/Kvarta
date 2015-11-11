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

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import hram.kvarta.Account;
import hram.kvarta.BuildConfig;
import hram.kvarta.Constants;
import hram.kvarta.LoginActivity;
import hram.kvarta.MainActivity;
import hram.kvarta.NetworkModuleMock;
import hram.kvarta.R;
import hram.kvarta.di.Injector;
import okio.Buffer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressMenuKey;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.times;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityMockTest {

    SharedPreferences mPreferences;
    Account mAccount;

    @Rule
    public final MockWebServer mServer = new MockWebServer();

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Before
    public void setUp() {

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        Injector.init(new NetworkModuleMock(getContext(), mServer));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mAccount = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(getContext());
        assertThat(mAccount, is(notNullValue()));
    }

    @After
    public void tearDown() {
    }

    /**
     * @return The current context.
     */
    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    /**
     * Если есть авторизационные данные и куки не просрочены то запускается главный экран
     *
     * @throws Exception
     */
    @Test
    public void testWhenThereInExistingAccount() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));

        Intents.init();
        rule.launchActivity(new Intent());
        intended(hasComponent(MainActivity.class.getName()));
        intended(hasComponent(LoginActivity.class.getName()), times(0));
        Intents.release();

        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
        onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));

        assertThat(mServer.getRequestCount(), is(1));
    }

    /**
     * Если есть авторизационные данные и куки просрочены то запускается экран авторизации. Авторизационные данные при этом введены.
     *
     * @throws Exception
     */
    @Test
    public void testWhenThereInExistingAccountButCookieExpires() throws Exception {
        mServer.enqueue(getResponse("cookie_expires/voda_action=tenant.txt"));

        Intents.init();
        rule.launchActivity(new Intent());
        intended(hasComponent(MainActivity.class.getName()));
        intended(hasComponent(LoginActivity.class.getName()));
        Intents.release();

        onView(withId(R.id.tsgid)).check(matches(withText(BuildConfig.tsgid)));
        onView(withId(R.id.accountid)).check(matches(withText(BuildConfig.accountid)));
        onView(withId(R.id.password)).check(matches(withText(BuildConfig.password))).perform(closeSoftKeyboard());

        assertThat(mServer.getRequestCount(), is(1));
    }

    /**
     * Меню вызывается кликом по иконке меню вправа вверху
     *
     * @throws Exception
     */
    @Test
    public void testOpenMenu() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));
        rule.launchActivity(new Intent());
        openActionBarOverflowOrOptionsMenu(getContext());

        onView(withText(R.string.action_settings)).check(matches(isDisplayed()));
        onView(withText(R.string.action_reload)).check(matches(isDisplayed()));
        onView(withText(R.string.action_logout)).check(matches(isDisplayed()));
    }

    /**
     * Меню вызывается кликом по аппаратной кнопке "Меню"
     *
     * @throws Exception
     */
    @Test
    public void testOpenMenuByPressMenuKey() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));
        rule.launchActivity(new Intent());

        onView(isRoot()).perform(pressMenuKey());

        onView(withText(R.string.action_settings)).check(matches(isDisplayed()));
        onView(withText(R.string.action_reload)).check(matches(isDisplayed()));
        onView(withText(R.string.action_logout)).check(matches(isDisplayed()));
    }

    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }
}
