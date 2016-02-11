package hram.kvarta.espresso;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import droidkit.content.TypedBundle;
import hram.kvarta.data.Account;
import hram.kvarta.BuildConfig;
import hram.kvarta.Constants;
import hram.kvarta.activity.LoginActivity;
import hram.kvarta.activity.MainActivity;
import hram.kvarta.NetworkModuleMock;
import hram.kvarta.R;
import hram.kvarta.di.Injector;
import okio.Buffer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static hram.kvarta.espresso.CustomViewMatchers.hasErrorText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityLaunchesLoginActivityMockTest {

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

        mAccount = new Account(getContext());
    }

    @After
    public void tearDown() {
    }

    private void signIn() throws InterruptedException {
        onView(withId(R.id.sign_in_button)).perform(click());
        Thread.sleep(300);
    }

    /**
     * @return The current context.
     */
    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    /**
     * При первом запуске должно открываться окно авторизации. Все поля пустые.
     * @throws Exception
     */
    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_Login() throws Exception {
        try {
            mAccount.reset();
            Intents.init();
            rule.launchActivity(createIntent(true));
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()));

            mServer.enqueue(getResponse("voda_action=login.txt"));
            mServer.enqueue(getResponse("login_post_response.txt"));
            mServer.enqueue(getResponse("voda_action=tenant.txt"));
            mServer.enqueue(getResponse("voda_action=tenant.txt"));

            onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
            onView(withId(R.id.accountid)).perform(replaceText(BuildConfig.accountid), closeSoftKeyboard());
            onView(withId(R.id.password)).perform(replaceText(BuildConfig.password), closeSoftKeyboard());

            signIn();

            assertThat(mAccount.isValid(), is(true));
            assertThat(mAccount.isDemo(), is(false));
            assertThat(mAccount.getTsgId(), is(BuildConfig.tsgid));
            assertThat(mAccount.getAccountId(), is(BuildConfig.accountid));
            assertThat(mAccount.getPassword(), is(BuildConfig.password));
            assertThat(mAccount.getUserInfo(), is(Constants.TEST_NAME));
            assertThat(mAccount.getAddress(), is(Constants.TEST_ADDR));

            onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
            onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));

            assertThat(mServer.getRequestCount(), is(4));

        } finally {
            Intents.release();
        }
    }

    /**
     * При первом запуске должно открываться окно авторизации. Все поля пустые.
     * @throws Exception
     */
    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_LoginDemo() throws Exception {
        try {
            mAccount.reset();
            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()));

            mServer.enqueue(getResponse("voda_action=login.txt"));
            mServer.enqueue(getResponse("login_post_response.txt"));
            mServer.enqueue(getResponse("demo/voda_action=tenant.txt"));
            mServer.enqueue(getResponse("demo/voda_action=tenant.txt"));

            onView(withId(R.id.sign_in_button_demo)).perform(click());
            Thread.sleep(300);

            assertThat(mAccount.isValid(), is(true));
            assertThat(mAccount.isDemo(), is(true));
            assertThat(mAccount.getUserInfo(), is(Constants.DEMO_NAME));
            assertThat(mAccount.getAddress(), is(Constants.DEMO_ADDR));

            onView(withId(R.id.tvAddress)).check(matches(withText(Constants.DEMO_ADDR)));
            onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.DEMO_NAME)));

            assertThat(mServer.getRequestCount(), is(4));
        } finally {
            Intents.release();
        }
    }

    /**
     * Проверка ошибочной авторизации если задать хоть одно не правильное поле.
     *
     * @throws Exception
     */
    @Test
    public void testWhenThereInNoExistingAccount_LaunchesLoginActivity_LoginIncorrect() throws Exception {
        try {
            mAccount.reset();
            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()));

            mServer.enqueue(getResponse("voda_action=login.txt"));
            mServer.enqueue(getResponse("loginincorrect/login_post_response.txt"));

            onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
            onView(withId(R.id.accountid)).perform(replaceText(BuildConfig.accountid), closeSoftKeyboard());
            onView(withId(R.id.password)).perform(replaceText(BuildConfig.password), closeSoftKeyboard());

            signIn();

            String errorString = getContext().getResources().getString(R.string.error_incorrect_password);
            onView(withId(R.id.password)).check(matches(hasErrorText(errorString)));

            assertThat(mServer.getRequestCount(), is(2));
        }finally {
            Intents.release();
        }
    }

    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }

    private Intent createIntent(boolean disableAnimation) {
        Bundle bundle = new Bundle();
        MainActivity.Args args = TypedBundle.from(bundle, MainActivity.Args.class);
        args.disableAnimation().set(disableAnimation);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        return intent;
    }
}
