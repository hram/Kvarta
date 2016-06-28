package hram.kvarta.services;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ServiceTestCase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import hram.kvarta.NetworkModuleMock;
import hram.kvarta.di.Injector;
import hram.kvarta.events.LoadDataEndedEvent;
import hram.kvarta.events.LoadDataErrorEvent;
import hram.kvarta.events.LoadDataStartedEvent;
import hram.kvarta.events.LogInErrorEvent;
import hram.kvarta.network.GetInfoService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
public class GetInfoServiceTest extends ServiceTestCase<GetInfoService> {

    private boolean loadDataStarted, loadDataEnded, loadDataError, logInError;

    @Rule
    public final MockWebServer mServer = new MockWebServer();

    public GetInfoServiceTest() {
        super(GetInfoService.class);
    }

    @Before
    public void setUp() throws Exception {
        EventBus.getDefault().register(this);
        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));
        setContext(InstrumentationRegistry.getTargetContext());
        testAndroidTestCaseSetupProperly();

        Injector.init(new NetworkModuleMock(getContext(), mServer));
    }

    @After
    public void tearDown() {
        EventBus.getDefault().unregister(this);
    }

    @Test
    public void testStartStop() throws InterruptedException, IOException {
        mServer.enqueue(getResponse("voda_action=tenant.txt"));

        Intent startIntent = new Intent(getContext(), GetInfoService.class);
        startService(startIntent);

        Thread.sleep(1000);

        assertThat(loadDataStarted, is(true));
        assertThat(loadDataEnded, is(true));
        assertThat(loadDataError, is(false));
        assertThat(logInError, is(false));

        shutdownService();
    }

    @Subscribe
    public void loadDataStarted(LoadDataStartedEvent event) {
        loadDataStarted = true;
    }

    @Subscribe
    public void loadDataEnded(LoadDataEndedEvent event) {
        loadDataEnded = true;
    }

    @Subscribe
    public void loadDataError(LoadDataErrorEvent event) {
        loadDataError = true;
    }

    @Subscribe
    public void logInError(LogInErrorEvent event) {
        logInError = true;
    }

    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }
}
