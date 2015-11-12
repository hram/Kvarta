package hram.kvarta.espresso;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import hram.kvarta.Account;
import hram.kvarta.BuildConfig;
import hram.kvarta.Constants;
import hram.kvarta.MainActivity;
import hram.kvarta.NetworkModuleMock;
import hram.kvarta.R;
import hram.kvarta.SettingsActivity;
import hram.kvarta.di.Injector;
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
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
public class SettingsActivityTest {
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

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testOpenSettings() throws Exception {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));

        Intents.init();
        rule.launchActivity(new Intent());
        intended(hasComponent(MainActivity.class.getName()));

        onView(isRoot()).perform(pressMenuKey());
        onView(withText(R.string.action_settings)).perform(click());

        intended(hasComponent(SettingsActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void testSettingsClosedWhenPressBack() throws Exception {
        testOpenSettings();

        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.tvAddress)).check(matches(withText(Constants.TEST_ADDR)));
    }

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

    @Test
    public void testPreferencesOpenGeneral() throws Exception {
        testOpenSettings();

        onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                .atPosition(0)
                .perform(click());

        onView(withText(R.string.pref_header_general)).check(matches(isDisplayed()));
    }

    @Test
    public void testPreferencesOpenNotifications() throws Exception {
        testOpenSettings();

        onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list), isDisplayed()))
                .atPosition(1)
                .perform(click());

        onView(withText(R.string.pref_header_notifications)).check(matches(isDisplayed()));
    }

    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }
}
