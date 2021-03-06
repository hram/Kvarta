package hram.kvarta.espresso;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import droidkit.content.TypedBundle;
import hram.kvarta.BuildConfig;
import hram.kvarta.Constants;
import hram.kvarta.NetworkModuleMock;
import hram.kvarta.R;
import hram.kvarta.activity.LoginActivity;
import hram.kvarta.activity.MainActivity;
import hram.kvarta.data.Account;
import hram.kvarta.di.Injector;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import okio.Buffer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.pressMenuKey;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.times;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static hram.kvarta.espresso.CustomViewActions.numberPickerScrollDown;
import static hram.kvarta.espresso.CustomViewActions.numberPickerScrollUp;
import static hram.kvarta.espresso.CustomViewMatchers.isEmptyEditText;
import static hram.kvarta.espresso.CustomViewMatchers.numberPickerHasValue;
import static hram.kvarta.espresso.CustomViewMatchers.withDrawable;
import static hram.kvarta.espresso.OrientationChangeAction.orientationLandscape;
import static hram.kvarta.espresso.OrientationChangeAction.orientationPortrait;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityMockTest {

    @Rule
    public final MockWebServer mServer = new MockWebServer();

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Before
    public void setUp() {

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        Injector.init(new NetworkModuleMock(getContext(), mServer));

        Account account = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(getContext());
        assertThat(account, is(notNullValue()));
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

    private void launchActivity() throws InterruptedException {
        rule.launchActivity(createIntent(true));
        Thread.sleep(1000);
    }

    /**
     * Если есть авторизационные данные и куки не просрочены то запускается главный экран
     *
     * @throws Exception
     */
    @Test
    public void testWhenThereInExistingAccount() throws Exception {
        try {
            mServer.enqueue(getResponse("voda_action=tenant.txt"));

            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()), times(0));

            onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
            onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));

            checkValues(getValues(Constants.VALUES_ID_HOT), getValues(Constants.VALUES_ID_COLD));

            assertThat(mServer.getRequestCount(), is(1));
        } finally {
            Intents.release();
        }
    }

    /**
     * Если есть авторизационные данные и куки просрочены то происходит фоновая авторизация.
     *
     * @throws Exception
     */
    @Test
    public void testWhenThereInExistingAccountButCookieExpires() throws Exception {
        try {
            mServer.enqueue(getResponse("cookie_expires/voda_action=tenant.txt"));
            mServer.enqueue(getResponse("voda_action=login.txt"));
            mServer.enqueue(getResponse("login_post_response.txt"));
            mServer.enqueue(getResponse("voda_action=tenant.txt"));
            mServer.enqueue(getResponse("voda_action=tenant.txt"));

            Intents.init();
            launchActivity();
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()), times(0));

            assertThat(mServer.getRequestCount(), is(5));

            onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
            onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));
            checkValues(getValues(Constants.VALUES_ID_HOT), getValues(Constants.VALUES_ID_COLD));

        } finally {
            Intents.release();
        }
    }

    /**
     * Если есть авторизационные данные и куки просрочены и фоновая авторизация не удалась то запускается окно авторизации.
     * Авторизационные данные при этом введены.
     *
     * @throws Exception
     */
    @Test
    public void testWhenThereInExistingAccountButCookieExpiresAndLoginFailed() throws Exception {
        try {
            mServer.enqueue(getResponse("cookie_expires/voda_action=tenant.txt"));
            mServer.enqueue(getResponse("voda_action=login.txt"));
            mServer.enqueue(getResponse("loginincorrect/login_post_response.txt"));

            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()));

            assertThat(mServer.getRequestCount(), is(3));

            onView(withId(R.id.tsgid)).check(matches(withText(BuildConfig.tsgid)));
            onView(withId(R.id.accountid)).check(matches(withText(BuildConfig.accountid)));
            onView(withId(R.id.password)).check(matches(withText(BuildConfig.password))).perform(closeSoftKeyboard());

        } finally {
            Intents.release();
        }
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

    /**
     * При старте кнопка (+) не видна
     *
     * @throws Exception
     */
    @Test
    public void testSaveButtonAfterStart() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));
        rule.launchActivity(new Intent());

        onView(withId(R.id.action_save)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    /**
     * При изменении показания появляется кнопка (+)
     *
     * @throws Exception
     */
    @Test
    public void testSaveButtonAppearedAfterValueWasChanged() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));
        rule.launchActivity(createIntent(true));
        Thread.sleep(300);

        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
        onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));

        checkValues(getValues(Constants.VALUES_ID_HOT), getValues(Constants.VALUES_ID_COLD));

        onView(withId(R.id.action_save)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.numberPickerH1)).perform(numberPickerScrollUp());
        Thread.sleep(1000);
        onView(withId(R.id.action_save)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    /**
     * При повороте экрана состояние не должно меняться. Новые значения тоже сохраняются.
     *
     * @throws Exception
     */
    @Test
    public void testScreenOrientationChanged() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));
        launchActivity();

        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
        onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));
        onView(withId(R.id.action_save)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        checkValues(getValues(Constants.VALUES_ID_HOT), getValues(Constants.VALUES_ID_COLD));

        onView(isRoot()).perform(orientationLandscape());

        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
        onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));
        onView(withId(R.id.action_save)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        checkValues(getValues(Constants.VALUES_ID_HOT), getValues(Constants.VALUES_ID_COLD));

        onView(withId(R.id.numberPickerH1)).perform(numberPickerScrollUp());
        onView(withId(R.id.numberPickerH2)).perform(numberPickerScrollUp());
        onView(withId(R.id.numberPickerH3)).perform(numberPickerScrollUp());
        onView(withId(R.id.numberPickerH4)).perform(numberPickerScrollUp());
        onView(withId(R.id.numberPickerH5)).perform(numberPickerScrollUp());

        onView(withId(R.id.numberPickerC1)).perform(numberPickerScrollDown());
        onView(withId(R.id.numberPickerC2)).perform(numberPickerScrollDown());
        onView(withId(R.id.numberPickerC3)).perform(numberPickerScrollDown());
        onView(withId(R.id.numberPickerC4)).perform(numberPickerScrollDown());
        onView(withId(R.id.numberPickerC5)).perform(numberPickerScrollDown());

        onView(withId(R.id.action_save)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(isRoot()).perform(orientationPortrait());

        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
        onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));
        onView(withId(R.id.action_save)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        checkValues(getValues(Constants.VALUES_ID_HOT_UP), getValues(Constants.VALUES_ID_COLD_DOWN));
    }

    /**
     * В меню выбрать "Выйти" программа переходит в окно авторизации. Все поля пустые.
     * В окне авторизации при нажатии Back происходит выход из приложения.
     *
     * @throws Exception
     */
    @Test
    public void testStartLoginAfterLogout() throws Exception {
        try {
            mServer.enqueue(getResponse("voda_action=tenant.txt"));

            Intents.init();
            rule.launchActivity(createIntent(true));
            intended(hasComponent(MainActivity.class.getName()));

            onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
            onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));

            onView(isRoot()).perform(pressMenuKey());

            onView(withText(R.string.action_logout)).perform(click());

            intended(hasComponent(LoginActivity.class.getName()));

            onView(withId(R.id.tsgid)).check(matches(isEmptyEditText()));
            onView(withId(R.id.accountid)).check(matches(isEmptyEditText()));
            onView(withId(R.id.password)).check(matches(isEmptyEditText()));

            try {
                onView(isRoot()).perform(pressBack());
                Assert.fail();
            } catch (NoActivityResumedException e) {
                // do nothing
            }

            //onView(withId(R.id.tvAddress)).check(doesNotExist());
        } finally {
            Intents.release();
        }
    }

    /**
     * При смене пользователей сначения счетчиков должны полностью обновляться
     *
     * @throws Exception
     */
    @Test
    public void testChangeUser() throws Exception {
        try {
            mServer.enqueue(getResponse("demo/voda_action=tenant.txt"));

            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()), times(0));

            assertThat(mServer.getRequestCount(), is(1));

            onView(withId(R.id.tvAddress)).check(matches(withText(Constants.DEMO_ADDR)));
            onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.DEMO_NAME)));

            checkValues(getValues(Constants.VALUES_ID_HOT_DЕМО), getValues(Constants.VALUES_ID_COLD_DЕМО));

            onView(isRoot()).perform(pressMenuKey());

            onView(withText(R.string.action_logout)).perform(click());

            intended(hasComponent(LoginActivity.class.getName()));

            onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
            onView(withId(R.id.accountid)).perform(replaceText(BuildConfig.accountid), closeSoftKeyboard());
            onView(withId(R.id.password)).perform(replaceText(BuildConfig.password), closeSoftKeyboard());

            mServer.enqueue(getResponse("voda_action=login.txt"));
            mServer.enqueue(getResponse("login_post_response.txt"));
            mServer.enqueue(getResponse("voda_action=tenant.txt"));
            mServer.enqueue(getResponse("voda_action=tenant.txt"));

            onView(withId(R.id.sign_in_button)).perform(click());

            assertThat(mServer.getRequestCount(), is(5));

            onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
            onView(withId(R.id.tvUserInfo)).check(matches(withText(Constants.TEST_NAME)));

            checkValues(getValues(Constants.VALUES_ID_HOT), getValues(Constants.VALUES_ID_COLD));
        } finally {
            Intents.release();
        }
    }

    /**
     * При нажатии на фонарик загорается или гаснет фонарик
     *
     * @throws Exception
     */
    //@Test
    public void testFlashOnOff() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));
        rule.launchActivity(createIntent(true));

        onView(withId(R.id.flash)).check(matches(isDisplayed()));
        onView(withId(R.id.flash)).check(matches(withDrawable(R.drawable.flash_off)));
        onView(withId(R.id.flash)).perform(click());
        onView(withId(R.id.flash)).check(matches(withDrawable(R.drawable.flash_on)));
        onView(withId(R.id.flash)).perform(click());
        onView(withId(R.id.flash)).check(matches(withDrawable(R.drawable.flash_off)));
    }

    /**
     * По клику в меню "Обновить" обновляются данные счетчиков
     *
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));
        rule.launchActivity(new Intent());

        checkValues(getValues(Constants.VALUES_ID_HOT), getValues(Constants.VALUES_ID_COLD));

        mServer.enqueue(getResponse("demo/voda_action=tenant.txt"));

        onView(isRoot()).perform(pressMenuKey());
        onView(withText(R.string.action_reload)).perform(click());

        checkValues(getValues(Constants.VALUES_ID_HOT_DЕМО), getValues(Constants.VALUES_ID_COLD_DЕМО));

        assertThat(mServer.getRequestCount(), is(2));
    }

    /**
     * В случае сетевой ошибки на главном экране отображается сообщение
     * "Не удалось получить данные. Повторите попытку позже." и кнопка "Обновить"
     *
     * @throws Exception
     */
    @Test
    public void testNetworkError() throws Exception {
        try {
            mServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

            Intents.init();
            launchActivity();
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()), times(0));

            onView(withId(R.id.layout_network_error)).check(matches(isDisplayed()));
            onView(withText(R.string.error_try_again)).check(matches(isDisplayed()));

            assertThat(mServer.getRequestCount(), is(1));
        } finally {
            Intents.release();
        }
    }

    /**
     * В случае сетевой ошибки на главном экране по клику на кнопку "Обновить" происходит запрос данных с сервера.
     * В случае успеха отображаются данные.
     *
     * @throws Exception
     */
    @Test
    public void testTryAgain() throws Exception {
        try {
            mServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
            mServer.enqueue(getResponse("voda_action=tenant.txt"));

            Intents.init();
            launchActivity();
            intended(hasComponent(MainActivity.class.getName()));
            intended(hasComponent(LoginActivity.class.getName()), times(0));

            onView(withId(R.id.layout_network_error)).check(matches(isDisplayed()));
            onView(withText(R.string.error_try_again)).check(matches(isDisplayed()));

            assertThat(mServer.getRequestCount(), is(1));

            onView(withId(R.id.button_try_again)).perform(click());
            Thread.sleep(300);

            checkValues(getValues(Constants.VALUES_ID_HOT), getValues(Constants.VALUES_ID_COLD));
            assertThat(mServer.getRequestCount(), is(2));
        } finally {
            Intents.release();
        }
    }

    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }

    private int[] getValues(int arrayID) {
        return InstrumentationRegistry.getContext().getResources().getIntArray(arrayID);
    }

    private void checkValues(int[] hotValues, int[] coldValues) {
        onView(withId(R.id.numberPickerH1)).check(matches(numberPickerHasValue(hotValues[0])));
        onView(withId(R.id.numberPickerH2)).check(matches(numberPickerHasValue(hotValues[1])));
        onView(withId(R.id.numberPickerH3)).check(matches(numberPickerHasValue(hotValues[2])));
        onView(withId(R.id.numberPickerH4)).check(matches(numberPickerHasValue(hotValues[3])));
        onView(withId(R.id.numberPickerH5)).check(matches(numberPickerHasValue(hotValues[4])));

        onView(withId(R.id.numberPickerC1)).check(matches(numberPickerHasValue(coldValues[0])));
        onView(withId(R.id.numberPickerC2)).check(matches(numberPickerHasValue(coldValues[1])));
        onView(withId(R.id.numberPickerC3)).check(matches(numberPickerHasValue(coldValues[2])));
        onView(withId(R.id.numberPickerC4)).check(matches(numberPickerHasValue(coldValues[3])));
        onView(withId(R.id.numberPickerC5)).check(matches(numberPickerHasValue(coldValues[4])));
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
