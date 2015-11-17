package hram.kvarta.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ServiceTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import hram.kvarta.activity.MainActivity;
import hram.kvarta.alarm.AlarmService;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
public class AlarmServiceTest extends ServiceTestCase<AlarmService> {

    public AlarmServiceTest() {
        super(AlarmService.class);
    }

    @Before
    public void setUp() throws Exception {
        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));
        setContext(InstrumentationRegistry.getTargetContext());
        testAndroidTestCaseSetupProperly();
    }

    @Test
    public void testStartStop() {
        Intent startIntent = new Intent(getContext(), AlarmService.class);
        startService(startIntent);
        shutdownService();
    }

    @Test
    public void testShowNotification() {
        Intent startIntent = new Intent(getContext(), AlarmService.class);
        startService(startIntent);

        AlarmService service = getService();
        assertThat(service, is(notNullValue()));

        service.showNotification();

        shutdownService();

        assertThat(isNotificationVisible(), is(true));

        service.clearNotification();

        assertThat(isNotificationVisible(), is(false));
    }

    private boolean isNotificationVisible() {
        PendingIntent test = PendingIntent.getActivity(getContext().getApplicationContext(), 0, new Intent(getContext().getApplicationContext(), MainActivity.class), PendingIntent.FLAG_NO_CREATE);
        return test != null;
    }
}
