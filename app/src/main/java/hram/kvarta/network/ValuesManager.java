package hram.kvarta.network;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import hram.kvarta.Account;

/**
 * @author Evgeny Khramov
 */
public class ValuesManager extends BaseManager {

    private final long[] hotValues = new long[4];
    private final long[] coldValues = new long[4];
    Account mAccount;

    public long getHotValue(int index){
        return hotValues[index];
    }

    public long getColdValue(int index){
        return coldValues[index];
    }

    public ValuesManager(OkHttpClient client, Account account) {
        //super(client);
        mAccount = account;
    }

    public boolean getValues(){
        try {
            Request request = new Request.Builder().url("http://www2.kvarta-c.ru/voda.php?action=tenant").build();
            Response response = mClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            ResponseBody body = response.body();
            Document doc = Jsoup.parse(body.string());

            Elements links = doc.select("font[class=medtxt]");

            Element item = links.get(1);
            //Log.d(TAG, item.text());
            if (!item.text().contains("Номер лицевого счета")) throw new IOException();

            item = links.get(2);
            //Log.d(TAG, item.text());
            mAccount.setAddress(item.text());

            item = links.get(3);
            //Log.d(TAG, item.text());
            mAccount.setUserInfo(item.text());

            item = links.get(5);
            //Log.d(TAG, item.text());
            mAccount.setLastTime(item.text());

            setColdValue(0, links.get(28).text());
            setColdValue(1, links.get(30).text());
            setColdValue(2, links.get(32).text());
            setColdValue(3, links.get(34).text());

            setHotValue(0, links.get(38).text());
            setHotValue(1, links.get(40).text());
            setHotValue(2, links.get(42).text());
            setHotValue(3, links.get(44).text());

        } catch (IOException e) {
            return false;
        } catch (IndexOutOfBoundsException iobe) {
            // когда вернулись не те данные
            return false;
        }

        return true;
    }

    public boolean saveValues(String newValueHot, String newValueCold){
        try {

            RequestBody formBody = new FormEncodingBuilder()
                    .add("action", "tenant")
                    .add("subaction", "tenantedit")
                    .add("service1counter1", newValueCold)
                    .add("service2counter1", newValueHot)
                    .add("put", "Сохранить")
                    .build();

            Request request = new Request.Builder()
                    .url("http://www2.kvarta-c.ru/voda.php")
                    .post(formBody)
                    .build();

            Response response = mClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            ResponseBody body = response.body();
            String respString = body.string();
            if (!respString.contains("Data is updated."))
                throw new IOException();

            Document doc = Jsoup.parse(respString);
            Element link = doc.select("a[href]").first();
            //Log.d(TAG, link.attr("href"));

            request = new Request.Builder().url("http://www2.kvarta-c.ru/" + link.attr("href")).build();
            response = mClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            body = response.body();
            doc = Jsoup.parse(body.string());

            Elements links = doc.select("font[class=medtxt]");

            Element item = links.get(1);
            //Log.d(TAG, item.text());
            if (!item.text().contains("Номер лицевого счета")) throw new IOException();

            item = links.get(2);
            //Log.d(TAG, item.text());
            mAccount.setAddress(item.text());

            item = links.get(3);
            //Log.d(TAG, item.text());
            mAccount.setUserInfo(item.text());

            item = links.get(5);
            //Log.d(TAG, item.text());
            mAccount.setLastTime(item.text());

            setColdValue(0, links.get(28).text());
            setColdValue(1, links.get(30).text());
            setColdValue(2, links.get(32).text());
            setColdValue(3, links.get(34).text());

            setHotValue(0, links.get(38).text());
            setHotValue(1, links.get(40).text());
            setHotValue(2, links.get(42).text());
            setHotValue(3, links.get(44).text());

        } catch (IOException e) {
            return false;
        } catch (IndexOutOfBoundsException iobe) {
            // когда вернулись не те данные
            return false;
        }

        return true;
    }

    private void setColdValue(int index, String value) {
        try {
            coldValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
        } catch (NumberFormatException nfe) {
            coldValues[index] = -1;
        }
    }

    private void setHotValue(int index, String value) {
        try {
            hotValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
        } catch (NumberFormatException nfe) {
            hotValues[index] = -1;
        }
    }
}
