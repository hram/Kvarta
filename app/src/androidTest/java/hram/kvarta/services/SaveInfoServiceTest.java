package hram.kvarta.services;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ServiceTestCase;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import hram.kvarta.NetworkModuleMock;
import hram.kvarta.di.Injector;
import hram.kvarta.events.BusProvider;
import hram.kvarta.events.LoadDataEndedEvent;
import hram.kvarta.events.SaveDataErrorEvent;
import hram.kvarta.events.SaveDataStartedEvent;
import hram.kvarta.network.SaveInfoService;
import okio.Buffer;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
public class SaveInfoServiceTest extends ServiceTestCase<SaveInfoService> {

    private static final Bus bus = BusProvider.getInstance();
    private boolean saveDataStarted, loadDataEnded, saveDataError;

    @Rule
    public final MockWebServer mServer = new MockWebServer();

    public SaveInfoServiceTest() {
        super(SaveInfoService.class);
    }

    @Before
    public void setUp() throws Exception {
        bus.register(this);
        assertThat(InstrumentationRegistry.getContext(), is(notNullValue()));
        assertThat(InstrumentationRegistry.getTargetContext(), is(notNullValue()));
        setContext(InstrumentationRegistry.getTargetContext());
        testAndroidTestCaseSetupProperly();

        Injector.init(new NetworkModuleMock(getContext(), mServer));
    }

    @After
    public void tearDown() {
        bus.unregister(this);
    }

    @Test
    public void testStartStop() throws InterruptedException, IOException {
        mServer.enqueue(getResponse("save_post_response.txt"));
        mServer.enqueue(getResponse("voda_action=tenant&rnd=1039871437.txt"));

        startService(SaveInfoService.getStartIntent(getContext(), "12345", "12345"));

        Thread.sleep(1000);

        assertThat(saveDataStarted, is(true));
        assertThat(loadDataEnded, is(true));
        assertThat(saveDataError, is(false));

        shutdownService();
    }

    @Subscribe
    public void saveDataStarted(SaveDataStartedEvent event) {
        saveDataStarted = true;
    }

    @Subscribe
    public void loadDataEnded(LoadDataEndedEvent event) {
        loadDataEnded = true;
    }

    @Subscribe
    public void saveDataError(SaveDataErrorEvent event) {
        saveDataError = true;
    }

    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }
}
