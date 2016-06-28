package hram.kvarta.services;

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
import hram.kvarta.events.UserLoginEndedEvent;
import hram.kvarta.events.UserLoginErrorEvent;
import hram.kvarta.events.UserLoginStartedEvent;
import hram.kvarta.network.UserLoginService;
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
public class UserLoginServiceTest extends ServiceTestCase<UserLoginService> {

    private boolean userLoginStarted, userLoginEnded, userLoginError;

    @Rule
    public final MockWebServer mServer = new MockWebServer();

    public UserLoginServiceTest() {
        super(UserLoginService.class);
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
        mServer.enqueue(getResponse("voda_action=login.txt"));
        mServer.enqueue(getResponse("login_post_response.txt"));
        mServer.enqueue(getResponse("voda_action=tenant.txt"));

        startService(UserLoginService.getStartDemoIntent(getContext()));

        Thread.sleep(1000);

        assertThat(userLoginStarted, is(true));
        assertThat(userLoginEnded, is(true));
        assertThat(userLoginError, is(false));

        shutdownService();
    }

    @Subscribe
    public void userLoginStarted(UserLoginStartedEvent event) {
        userLoginStarted = true;
    }

    @Subscribe
    public void userLoginEnded(UserLoginEndedEvent event) {
        userLoginEnded = true;
    }

    @Subscribe
    public void userLoginError(UserLoginErrorEvent event) {
        userLoginError = true;
    }


    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }
}
