package hram.kvarta;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
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
public class LoginActivityTest {

    private static final String TAG = "UI Tests!";
    SharedPreferences mPreferences;
    Account mAccount;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        Log.d(TAG, "Test was set up!");

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mAccount = new Account(getContext());
    }

    @After
    public void tearDown() {
        Log.d(TAG, "Test was torn down!");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        //PinAFoodApp.useMocks = true;

    }

    /**
     * @return The current context.
     */
    private Context getContext() {
        //return mActivityRule.getActivity();
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testErrorMessage() {
        //mActivityRule.getActivity();
        Intent intent = new Intent();
        LoginActivity.Args args = TypedBundle.from(intent.getExtras(), LoginActivity.Args.class);
        args.toMockNetwork().set(true);
        args.networkResult().set(true);
        mActivityRule.launchActivity(intent);

        onView(withId(R.id.tsgid)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());

        String errorString = getContext().getResources().getString(R.string.error_field_required);
        onView(withId(R.id.tsgid)).check(matches(hasErrorText(errorString)));
        onView(withId(R.id.tsgid)).check(matches(hasErrorText(errorString)));
        onView(withId(R.id.tsgid)).check(matches(hasErrorText(errorString)));
    }

    private static Matcher<? super View> hasErrorText(String expectedError) {
        return new ErrorTextMatcher(expectedError);
    }

    @Test
    public void testLoadDataFromSettings() {

        mAccount.setTsgId(BuildConfig.tsgid);
        mAccount.setAccountId(BuildConfig.accountid);
        mAccount.setPassword(BuildConfig.password);

        Bundle bundle = new Bundle();
        LoginActivity.Args args = TypedBundle.from(bundle, LoginActivity.Args.class);
        args.toMockNetwork().set(true);
        args.networkResult().set(true);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        mActivityRule.launchActivity(intent);

        Intent resultData = new Intent();
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(toPackage("hram.kvarta")).respondWith(result);

        // Type text and then press the button.
        onView(withId(R.id.tsgid)).check(matches(withText(BuildConfig.tsgid)));
        onView(withId(R.id.accountid)).check(matches(withText(BuildConfig.accountid)));
        onView(withId(R.id.password)).check(matches(withText(BuildConfig.password)));
        onView(withId(R.id.sign_in_button)).perform(click());

        //onView(withId(R.id.layout_progress)).check(matches(isDisplayed()));
        onView(isRoot()).perform(CustomViewActions.waitForMatch(withId(R.id.layout_progress), 1000));
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

    private static class ErrorTextMatcher extends TypeSafeMatcher<View> {
        private final String expectedError;

        private ErrorTextMatcher(String expectedError) {
            this.expectedError = expectedError;
        }

        @Override
        public boolean matchesSafely(View view) {
            if (!(view instanceof EditText)) {
                return false;
            }
            EditText editText = (EditText) view;
            return expectedError.equals(editText.getError());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with error: " + expectedError);
        }
    }
}
