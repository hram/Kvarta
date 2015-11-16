package hram.kvarta.espresso;

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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import droidkit.content.TypedBundle;
import hram.kvarta.data.Account;
import hram.kvarta.BuildConfig;
import hram.kvarta.activity.LoginActivity;
import hram.kvarta.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static hram.kvarta.espresso.CustomViewMatchers.hasErrorText;
import static hram.kvarta.espresso.CustomViewMatchers.isEmptyEditText;
import static hram.kvarta.espresso.OrientationChangeAction.orientationLandscape;
import static hram.kvarta.espresso.OrientationChangeAction.orientationPortrait;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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

    /**
     * При повороте экрана введеные значения не должны пропадать
     */
    @Test
    public void testScreenOrientationChanged() {
        mActivityRule.launchActivity(new Intent());

        onView(withId(R.id.tsgid)).check(matches(isEmptyEditText()));
        onView(withId(R.id.accountid)).check(matches(isEmptyEditText()));
        onView(withId(R.id.password)).check(matches(isEmptyEditText()));

        onView(withId(R.id.tsgid)).perform(replaceText(BuildConfig.tsgid), closeSoftKeyboard());
        onView(withId(R.id.accountid)).perform(replaceText(BuildConfig.accountid), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText(BuildConfig.password), closeSoftKeyboard());

        onView(isRoot()).perform(orientationLandscape());

        onView(withId(R.id.tsgid)).check(matches(withText(BuildConfig.tsgid)));
        onView(withId(R.id.accountid)).check(matches(withText(BuildConfig.accountid)));
        onView(withId(R.id.password)).check(matches(withText(BuildConfig.password)));

        onView(isRoot()).perform(orientationPortrait());

        onView(withId(R.id.tsgid)).check(matches(withText(BuildConfig.tsgid)));
        onView(withId(R.id.accountid)).check(matches(withText(BuildConfig.accountid)));
        onView(withId(R.id.password)).check(matches(withText(BuildConfig.password)));
    }

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
}
