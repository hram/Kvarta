package hram.kvarta.espresso;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import droidkit.content.TypedBundle;
import hram.kvarta.Account;
import hram.kvarta.BuildConfig;
import hram.kvarta.LoginActivity;
import hram.kvarta.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static hram.kvarta.espresso.CustomViewMatchers.hasErrorText;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    private static final String TAG = "UIT";
    SharedPreferences mPreferences;
    Account mAccount;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(LoginActivity.class, true, false);

    @Before
    public void setUp() {
        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mAccount = new Account(getContext());
        mAccount.reset();
    }

    @After
    public void tearDown() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Log.v(TAG, "set up class!");
        //PinAFoodApp.useMocks = true;
    }

    /**
     * @return The current context.
     */
    private Context getContext() {
        //return mActivityRule.getActivity();
        return InstrumentationRegistry.getTargetContext();
    }

    /**
     * Все поля отображаются
     */
    @Test
    public void testFindAllViews() {
        mActivityRule.launchActivity(new Intent());

        onView(withId(R.id.tsgid)).check(matches(isDisplayed()));
        onView(withId(R.id.accountid)).check(matches(isDisplayed()));
        onView(withId(R.id.password)).check(matches(isDisplayed()));
        onView(withId(R.id.sign_in_button_demo)).check(matches(isDisplayed()));
        onView(withId(R.id.sign_in_button)).check(matches(isDisplayed()));
    }

    /**
     * Все поля обязательны к заполнению.
     */
    @Test
    public void testAllFieldsRequired() {
        mActivityRule.launchActivity(new Intent());

        String errorString = getContext().getResources().getString(R.string.error_field_required);

        // проверка на обязательность поля Номер ТСЖ
        onView(withId(R.id.tsgid)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.accountid)).perform(replaceText(BuildConfig.accountid), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText(BuildConfig.password), closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());
        onView(withId(R.id.tsgid)).check(matches(hasErrorText(errorString)));

        // проверка на обязательность поля ID пользователя
        onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
        onView(withId(R.id.accountid)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText(BuildConfig.password), closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());
        onView(withId(R.id.accountid)).check(matches(hasErrorText(errorString)));

        // проверка на обязательность поля Пароль
        onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
        onView(withId(R.id.accountid)).perform(replaceText(BuildConfig.accountid), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());
        onView(withId(R.id.password)).check(matches(hasErrorText(errorString)));
    }

    @Test
    public void testErrorMessage() {
        mActivityRule.launchActivity(createIntent(true, true, true));

        onView(ViewMatchers.withId(R.id.tsgid)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());

        String errorString = getContext().getResources().getString(R.string.error_field_required);
        onView(withId(R.id.tsgid)).check(matches(hasErrorText(errorString)));
        onView(withId(R.id.tsgid)).check(matches(hasErrorText(errorString)));
        onView(withId(R.id.tsgid)).check(matches(hasErrorText(errorString)));
    }

    @Test
    public void testLoadDataFromSettings() {

        Intents.init();

        mAccount.setTsgId(BuildConfig.tsgid);
        mAccount.setAccountId(BuildConfig.accountid);
        mAccount.setPassword(BuildConfig.password);

        mActivityRule.launchActivity(createIntent(true, false, true));

        // Type text and then press the button.
        onView(withId(R.id.tsgid)).check(matches(withText(BuildConfig.tsgid)));
        onView(withId(R.id.accountid)).check(matches(withText(BuildConfig.accountid)));
        onView(withId(R.id.password)).check(matches(withText(BuildConfig.password))).perform(closeSoftKeyboard());

        onView(withId(R.id.sign_in_button)).perform(click());

        // пока не получается проверить так как идет прогресс и операция считаетс яне завершенной
        // https://groups.google.com/forum/#!searchin/android-test-kit-discuss/Volley/android-test-kit-discuss/RBzGo5nDgwI/bKCwZLkviSUJ
        // возможное решение http://blog.sqisland.com/2015/04/espresso-custom-idling-resource.html
        //onView(withId(R.id.layout_progress)).check(matches(isDisplayed()));
        //onView(isRoot()).perform(CustomViewActions.waitId(R.id.layout_progress, 1000));

        // пока не работает всегда RESULT_OK
        Intent resultData = new Intent();
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasComponent(LoginActivity.class.getName())).respondWith(result);

        Intents.release();
    }

    //@Test
    public void changeText_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
        onView(withId(R.id.accountid)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());

        //onView(withText(R.string.error_field_required)).check(matches(isDisplayed()));
        //onView(isRoot()).perform(CustomViewActions.waitForMatch(withText(R.string.error_field_required), 5000));

        // Check that the text was changed.
        //onView(withId(R.id.textToBeChanged)).check(matches(withText(STRING_TO_BE_TYPED)));

        //Wait for the webview to show up.
        //onView(isRoot()).perform(waitForMatch(withId(R.id.fragment_kittens_webview), UI_TEST_TIMEOUT));

        //Wait for the alert to show up.
        /* NOTE: Simply adding this method may not be sufficient for all devices - some show
                AlertDialogs in a completely separate window, and so waiting for a match on the
                root view will fail unless you wait for the new window to show up before you start
                looping through it, looking for this match.
                But it works on genymotion, so for my purposes: ¯\_(ツ)_/¯
        */
        //onView(isRoot()).perform(waitForMatch(withText(R.string.stf_alert_text), UI_TEST_TIMEOUT));
    }

    /*
        @Test
        public void shouldShowErrorOnEmptyUsername() throws Exception {
            //given

            //when
            onView(withId(R.id.sign_in_button)).perform(click());
            //then
            onView(withId(R.id.tsgid)).check(matches(withError(R.string.error_field_required)));

        }
    */
    private Intent createIntent(boolean toMockNetwork, boolean networkResult, boolean switchOffAnimations) {
        Bundle bundle = new Bundle();
        LoginActivity.Args args = TypedBundle.from(bundle, LoginActivity.Args.class);
        args.toMockNetwork().set(toMockNetwork);
        args.networkResult().set(networkResult);
        args.switchOffAnimations().set(switchOffAnimations);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        return intent;
    }

    private static Matcher<View> withError(final int expectedResourceId) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof EditText)) {
                    return false;
                }
                EditText editText = (EditText) view;
                CharSequence error = editText.getError();

                return error != null && error.toString().
                        equals(view.getResources().getString(expectedResourceId));
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }
}
