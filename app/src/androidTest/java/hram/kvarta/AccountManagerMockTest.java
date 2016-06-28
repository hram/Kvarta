package hram.kvarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import hram.kvarta.data.Account;
import hram.kvarta.di.Injector;
import hram.kvarta.network.AccountManager;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccountManagerMockTest {
    @Rule
    public final MockWebServer mServer = new MockWebServer();
    SharedPreferences mPreferences;
    Account mAccount;
    AccountManager mAccountManager;

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

        Injector.init(new NetworkModuleMock(getContext(), mServer));

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mAccount = new Account(getContext());
        mAccount.reset();

        mAccountManager = new AccountManager();
        assertThat(mAccountManager, is(notNullValue()));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMockLogin() throws IOException, InterruptedException {

        mServer.enqueue(getResponse("voda_action=login.txt"));
        mServer.enqueue(getResponse("login_post_response.txt"));
        mServer.enqueue(getResponse("voda_action=tenant.txt"));

        assertThat(mAccountManager.logIn(BuildConfig.tsgid, BuildConfig.accountid, BuildConfig.password, false), is(true));
        assertThat(mServer.getRequestCount(), is(3));

        RecordedRequest request1 = mServer.takeRequest();
        assertThat(request1.getPath(), is("/voda.php?action=login"));

        RecordedRequest request2 = mServer.takeRequest();
        assertThat(request2.getPath(), is("/voda.php"));
        assertThat(request2.getBody().readUtf8(), is(String.format("action=login&subaction=enter&usertype=tenant&tsgid=%1s&accountid=%2s&password=%3s", BuildConfig.tsgid, BuildConfig.accountid, BuildConfig.password)));

        RecordedRequest request3 = mServer.takeRequest();
        assertThat(request3.getPath(), is("/voda.php?action=tenant"));

        mServer.enqueue(getResponse("voda_action=login.txt"));
        mServer.enqueue(getResponse("login_post_response.txt"));
        mServer.enqueue(getResponse("voda_action=tenant.txt"));

        Account account = new Account.Builder()
                .accountId(BuildConfig.accountid)
                .tsgId(BuildConfig.tsgid)
                .password(BuildConfig.password)
                .demo(false)
                .build(getContext());
        assertThat(account, is(notNullValue()));

        assertThat(mAccountManager.logIn(account), is(true));
        assertThat(mServer.getRequestCount(), is(6));
    }

    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }
}
