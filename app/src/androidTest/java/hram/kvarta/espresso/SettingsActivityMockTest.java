package hram.kvarta.espresso;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.widget.NumberPicker;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import droidkit.content.TypedBundle;
import droidkit.content.TypedPrefs;
import hram.kvarta.BuildConfig;
import hram.kvarta.Constants;
import hram.kvarta.NetworkModuleMock;
import hram.kvarta.R;
import hram.kvarta.activity.MainActivity;
import hram.kvarta.activity.SettingsActivity;
import hram.kvarta.data.Account;
import hram.kvarta.data.Settings;
import hram.kvarta.di.Injector;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.pressMenuKey;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static hram.kvarta.espresso.CustomViewActions.numberPickerSetValue;
import static hram.kvarta.espresso.CustomViewMatchers.numberPickerHasValue;
import static hram.kvarta.espresso.CustomViewMatchers.withResourceName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
public class SettingsActivityMockTest {
    SharedPreferences mPreferences;
    Account mAccount;
    Settings mSettings;


    @Rule
    public final MockWebServer mServer = new MockWebServer();

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Before
    public void setUp() {

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        Injector.init(new NetworkModuleMock(getContext(), mServer));


        mSettings = TypedPrefs.from(getContext(), Settings.class);
        assertThat(mSettings, is(notNullValue()));

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

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testOpenSettings() throws Exception {
        try {
            mServer.enqueue(getResponse("voda_action=tenant.txt"));
            Intents.init();
            rule.launchActivity(new Intent());
            intended(hasComponent(MainActivity.class.getName()));

            onView(isRoot()).perform(pressMenuKey());
            onView(withText(R.string.action_settings)).perform(click());

            intended(hasComponent(SettingsActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }

    /**
     * Окно настроек закрывается аппаратной кнопкой "Назад"
     *
     * @throws Exception
     */
    @Test
    public void testSettingsClosedWhenPressBack() throws Exception {
        testOpenSettings();

        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
    }

    /**
     * Окно настроек закрывается из меню кнопкой "Перейти вверх"
     *
     * @throws Exception
     */
    @Test
    public void testSettingsClosedWhenPressHome() throws Exception {
        testOpenSettings();

        // TODO не локализованная строка
        onView(withContentDescription("Перейти вверх")).perform(click());
        // TODO так не хочет работать потому что ImageButton{id=-1, desc=Перейти вверх, visibility=VISIBLE
        //onView(withId(android.R.id.home)).perform(click());
        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
    }

    @Test
    public void testPreferences() throws Exception {
        testOpenSettings();

        onData(anything()).inAdapterView(allOf(withId(android.R.id.list), isDisplayed())).atPosition(0).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(allOf(withId(android.R.id.list), isDisplayed())).atPosition(1).check(matches(isDisplayed()));
    }

    /**
     * Открывается окно настроек "Основные"
     *
     * @throws Exception
     */
    @Test
    public void testPreferencesOpenGeneral() throws Exception {
        testOpenSettings();

        onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                .atPosition(0)
                .perform(click());

        // такой текст может встретиться где угодно
        onView(withText(R.string.pref_header_general)).check(matches(isDisplayed()));
        // тут поиск только в ActionBar
        onView(allOf(isDescendantOfA(withResourceName("android:id/action_bar_container")), withText(R.string.pref_header_general)));
    }

    /**
     * Открывается окно настроек "Уведомления"
     *
     * @throws Exception
     */
    @Test
    public void testPreferencesOpenNotifications() throws Exception {
        testOpenSettings();

        onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                .atPosition(1)
                .perform(click());

        onView(allOf(isDescendantOfA(withResourceName("android:id/action_bar_container")), withText(R.string.pref_header_notifications)));
    }

    /**
     * При изменении настройки "Отображать информацию о пользователе на главном экране" меняется параметр и отображение на главной Settings.enableUserInfo()
     *
     * @throws Exception
     */
    @Test
    public void testPreferencesGeneralEnableUserInfo() throws Exception {
        try {
            boolean prevValue = mSettings.enableUserInfo().get();
            mServer.enqueue(getResponse("voda_action=tenant.txt"));

            Intents.init();
            rule.launchActivity(createIntent(true));
            intended(hasComponent(MainActivity.class.getName()));

            onView(withId(R.id.layout_usetInfo)).check(matches(withEffectiveVisibility(mSettings.enableUserInfo().get() ? ViewMatchers.Visibility.VISIBLE : ViewMatchers.Visibility.GONE)));

            onView(isRoot()).perform(pressMenuKey());
            onView(withText(R.string.action_settings)).perform(click());

            intended(hasComponent(SettingsActivity.class.getName()));

            onData(anything())
                    .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                    .atPosition(0)
                    .perform(click());

            onView(allOf(isDescendantOfA(withResourceName("android:id/action_bar_container")), withText(R.string.pref_header_general)));

            onData(anything())
                    .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                    .atPosition(0)
                    .perform(click());

            onView(isRoot()).perform(pressBack());
            onView(isRoot()).perform(pressBack());

            assertThat(prevValue != mSettings.enableUserInfo().get(), is(true));
            onView(withId(R.id.layout_usetInfo)).check(matches(withEffectiveVisibility(mSettings.enableUserInfo().get() ? ViewMatchers.Visibility.VISIBLE : ViewMatchers.Visibility.GONE)));
        } finally {
            Intents.release();
        }
    }

    /**
     * При изменении настройки включения уведомления меняется параметр Settings.enableRemind()
     * При изменении настройки включения уведомления меняется доступность настройки даты уведомления
     *
     * @throws Exception
     */
    @Test
    public void testPreferencesEnableRemind() throws Exception {
        boolean prevValue = mSettings.enableRemind().get();

        testOpenSettings();

        onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                .atPosition(1)
                .perform(click());

        onView(allOf(isDescendantOfA(withResourceName("android:id/action_bar_container")), withText(R.string.pref_header_notifications)));

        if (mSettings.enableRemind().get()) {
            onData(anything())
                    .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                    .atPosition(1)
                    .check(matches(isEnabled()));
        } else {
            onData(anything())
                    .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                    .atPosition(1)
                    .check(matches(not(isEnabled())));
        }

        onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                .atPosition(0)
                .perform(click());

        if (mSettings.enableRemind().get()) {
            onData(anything())
                    .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                    .atPosition(1)
                    .check(matches(isEnabled()));
        } else {
            onData(anything())
                    .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                    .atPosition(1)
                    .check(matches(not(isEnabled())));
        }

        assertThat(prevValue != mSettings.enableRemind().get(), is(true));

        onView(isRoot()).perform(pressBack());
        onView(isRoot()).perform(pressBack());
    }

    /**
     * При изменении настройки даты уведомления меняется параметр Settings.remindDate()
     *
     * @throws Exception
     */
    @Test
    public void testPreferencesRemindDate() throws Exception {
        mSettings.enableRemind().set(true);
        mSettings.remindDate().set(1);

        testOpenSettings();

        onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                .atPosition(1)
                .perform(click());

        onView(allOf(isDescendantOfA(withResourceName("android:id/action_bar_container")), withText(R.string.pref_header_notifications)));

        onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                .atPosition(1)
                .perform(click());

        onView(withText(R.string.pref_title_date)).check(matches(isDisplayed()));
        // import static com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers.isDialog;
        //onView(withText(R.string.pref_title_date)).inRoot(isDialog()).check(matches(isDisplayed()));
        // по ID не находит почему то
        //onView(withId(R.id.alertTitle)).check(matches(withText(R.string.pref_title_date)));

        onView(withClassName(Matchers.equalTo(NumberPicker.class.getName()))).check(matches(numberPickerHasValue(mSettings.remindDate().get())));
        onView(withClassName(Matchers.equalTo(NumberPicker.class.getName()))).perform(numberPickerSetValue(10));
        onView(withId(android.R.id.button1)).perform(click());

        assertThat(mSettings.remindDate().get(), is(10));

        onView(isRoot()).perform(pressBack());
        onView(isRoot()).perform(pressBack());
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
