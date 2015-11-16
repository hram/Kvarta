package hram.kvarta;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.util.Calendar;

import droidkit.content.TypedPrefs;
import hram.kvarta.alarm.AlarmManager;
import hram.kvarta.data.Settings;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
public class AlarmManagerTest {
    Settings mSettings;
    AlarmManager mAlarmManager;

    /**
     * @return The current context.
     */
    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws MalformedURLException {

        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));

        mSettings = TypedPrefs.from(getContext(), Settings.class);
        mAlarmManager = new AlarmManager(getContext());
    }

    @Test
    public void testSetAlarmEnabled() {
        mSettings.enableRemind().set(true);
        assertThat(mAlarmManager.setAlarm(mAlarmManager.getAlarmDate().getTimeInMillis()), is(true));

        mSettings.enableRemind().set(false);
        assertThat(mAlarmManager.setAlarm(mAlarmManager.getAlarmDate().getTimeInMillis()), is(false));

        mSettings.enableRemind().set(true);
        assertThat(mAlarmManager.setAlarm(), is(true));

        mSettings.enableRemind().set(false);
        assertThat(mAlarmManager.setAlarm(), is(false));
    }

    @Test
    public void testAlarmTime() {

        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        if (currentDay == 1) {
            mSettings.remindDate().set(10);
            Calendar date = mAlarmManager.getAlarmDate();
            assertThat(date.get(Calendar.MONTH), is(currentMonth));
            assertThat(date.get(Calendar.DAY_OF_MONTH), is(10));
        } else if (currentDay >= 28) {
            mSettings.remindDate().set(10);
            Calendar date = mAlarmManager.getAlarmDate();
            assertThat(date.get(Calendar.MONTH), is(currentMonth + 1));
            assertThat(date.get(Calendar.DAY_OF_MONTH), is(10));
        } else {
            mSettings.remindDate().set(currentDay - 1);
            Calendar date = mAlarmManager.getAlarmDate();
            assertThat(date.get(Calendar.MONTH), is(currentMonth + 1));
            assertThat(date.get(Calendar.DAY_OF_MONTH), is(currentDay - 1));

            mSettings.remindDate().set(currentDay);
            date = mAlarmManager.getAlarmDate();
            assertThat(date.get(Calendar.MONTH), is(currentMonth + 1));
            assertThat(date.get(Calendar.DAY_OF_MONTH), is(currentDay));

            mSettings.remindDate().set(currentDay + 1);
            date = mAlarmManager.getAlarmDate();
            assertThat(date.get(Calendar.MONTH), is(currentMonth));
            assertThat(date.get(Calendar.DAY_OF_MONTH), is(currentDay + 1));
        }
    }
}
