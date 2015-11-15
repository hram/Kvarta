package hram.kvarta;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import hram.kvarta.di.Injector;
import hram.kvarta.network.ValuesManager;
import okio.Buffer;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Evgeny Khramov
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ValuesManagerMockTest {
    @Rule
    public final MockWebServer mServer = new MockWebServer();
    SharedPreferences mPreferences;
    Account mAccount;

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

    @Test
    public void testGetValuesMock() throws IOException, InterruptedException {

        mServer.enqueue(getResponse("voda_action=tenant.txt"));

        ValuesManager valuesManager = new ValuesManager();
        assertThat(valuesManager, is(notNullValue()));

        assertThat(valuesManager.getValues(mAccount), is(true));
        assertThat(mServer.getRequestCount(), is(1));

        RecordedRequest request = mServer.takeRequest();
        assertThat(request.getPath(), is("/voda.php?action=tenant"));

        assertThat(valuesManager.getValue(ValuesManager.WATER_HOT), is(124L));
        assertThat(valuesManager.getValue(ValuesManager.WATER_COLD), is(221L));
    }

    @Test
    public void testSaveValuesMock() throws IOException, InterruptedException {

        mServer.enqueue(getResponse("save_post_response.txt"));
        mServer.enqueue(getResponse("voda_action=tenant&rnd=1039871437.txt"));

        ValuesManager valuesManager = new ValuesManager();
        valuesManager.setServicesCount(2);
        assertThat(valuesManager, is(notNullValue()));

        assertThat(valuesManager.saveValues(mAccount, "124", "221"), is(true));
        assertThat(mServer.getRequestCount(), is(2));

        RecordedRequest request1 = mServer.takeRequest();
        assertThat(request1.getPath(), is("/voda.php"));
        // TODO надо разобраться с кодировкой
        //assertThat(request1.getBody().readString(Charset.forName("windows-1251")), is("action=tenant&subaction=tenantedit&service1counter1=221&service2counter1=124&put=%D1%EE%F5%F0%E0%ED%E8%F2%FC"));

        RecordedRequest request2 = mServer.takeRequest();
        assertThat(request2.getPath(), is("/voda.php?action=tenant&rnd=1039871437"));

        assertThat(valuesManager.getValue(ValuesManager.WATER_HOT), is(124L));
        assertThat(valuesManager.getValue(ValuesManager.WATER_COLD), is(221L));
    }

    @Test
    public void testGetWaterAndElectricityMock() throws IOException, InterruptedException {

        mServer.enqueue(getResponse("water_and_electricity/voda_action=tenant.txt"));

        ValuesManager valuesManager = new ValuesManager();
        assertThat(valuesManager, is(notNullValue()));

        assertThat(valuesManager.getValues(mAccount), is(true));
        assertThat(mServer.getRequestCount(), is(1));

        RecordedRequest request = mServer.takeRequest();
        assertThat(request.getPath(), is("/voda.php?action=tenant"));

        assertThat(valuesManager.getValue(ValuesManager.WATER_HOT), is(116L));
        assertThat(valuesManager.getValue(ValuesManager.WATER_COLD), is(281L));
        assertThat(valuesManager.getValue(ValuesManager.ELECTRICITY_DAY), is(3098L));
        assertThat(valuesManager.getValue(ValuesManager.ELECTRICITY_NIGHT), is(685L));
    }

    private MockResponse getResponse(String fileName) throws IOException {
        InputStream in = InstrumentationRegistry.getContext().getResources().getAssets().open(fileName);
        assertThat(in, is(notNullValue()));

        return new MockResponse().setBody(new Buffer().readFrom(in));
    }
}
