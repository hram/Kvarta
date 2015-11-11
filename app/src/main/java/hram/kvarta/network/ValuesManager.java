package hram.kvarta.network;

import com.squareup.okhttp.FormEncodingBuilder;
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

    public static final int WATER_HOT = 1;
    public static final int WATER_COLD = 2;
    public static final int ELECTRICITY_DAY = 3;
    public static final int ELECTRICITY_NIGHT = 4;

    private final long[] hotValues = new long[4];
    private final long[] coldValues = new long[4];
    private final long[] elDayValues = new long[4];
    private final long[] elNightValues = new long[4];
    private Account mAccount;
    private int mServicesCount;

    public int getServicesCount() {
        return mServicesCount;
    }

    public void setServicesCount(int value) {
        mServicesCount = value;
    }

    public long getValue(int value) {
        return getValue(value, 0);
    }

    public long getValue(int value, int index) {
        switch (value) {
            case WATER_HOT:
                return hotValues[index];
            case WATER_COLD:
                return coldValues[index];
            case ELECTRICITY_DAY:
                return elDayValues[index];
            case ELECTRICITY_NIGHT:
                return elNightValues[index];
            default:
                throw new IllegalArgumentException();
        }
    }

    private void setValue(int v, int index, String value) {
        switch (v) {
            case WATER_HOT:
                try {
                    hotValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
                } catch (NumberFormatException nfe) {
                    hotValues[index] = -1;
                }
                break;
            case WATER_COLD:
                try {
                    coldValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
                } catch (NumberFormatException nfe) {
                    coldValues[index] = -1;
                }
                break;
            case ELECTRICITY_DAY:
                try {
                    elDayValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
                } catch (NumberFormatException nfe) {
                    elDayValues[index] = -1;
                }
                break;
            case ELECTRICITY_NIGHT:
                try {
                    elNightValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
                } catch (NumberFormatException nfe) {
                    elNightValues[index] = -1;
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public ValuesManager(Account account) {
        mAccount = account;
    }

    public boolean getValues() {
        try {
            Request request = new Request.Builder().url(createUrl("/voda.php?action=tenant")).build();
            Response response = mClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            ResponseBody body = response.body();
            Document doc = Jsoup.parse(body.string());

            mServicesCount = 0;
            Elements inputs = doc.select("input[name]");
            for (Element item : inputs) {
                String attr = item.attr("name");
                switch (attr) {
                    case "service1counter1":
                        mServicesCount = 1;
                        break;
                    case "service2counter1":
                        mServicesCount = 2;
                        break;
                    case "service3counter1":
                        mServicesCount = 3;
                        break;
                    case "service4counter1":
                        mServicesCount = 4;
                        break;
                }
            }

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

            setValues(links);

        } catch (IOException e) {
            return false;
        } catch (IndexOutOfBoundsException iobe) {
            // когда вернулись не те данные
            return false;
        }

        return true;
    }

    public boolean saveValues(String newValueHot, String newValueCold) {
        try {

            RequestBody formBody = new FormEncodingBuilder()
                    .add("action", "tenant")
                    .add("subaction", "tenantedit")
                    .add("service1counter1", newValueCold)
                    .add("service2counter1", newValueHot)
                    .add("put", "Сохранить")
                    .build();

            Request request = new Request.Builder()
                    .url(createUrl("/voda.php"))
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

            request = new Request.Builder().url(createUrl(link.attr("href"))).build();
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

            setValues(links);

        } catch (IOException e) {
            return false;
        } catch (IndexOutOfBoundsException iobe) {
            // когда вернулись не те данные
            return false;
        }

        return true;
    }

    private void setValues(Elements links) {
        if (mServicesCount > 0) {
            setValue(WATER_COLD, 0, links.get(28).text());
            setValue(WATER_COLD, 1, links.get(30).text());
            setValue(WATER_COLD, 2, links.get(32).text());
            setValue(WATER_COLD, 3, links.get(34).text());
        }

        if (mServicesCount > 1) {
            setValue(WATER_HOT, 0, links.get(38).text());
            setValue(WATER_HOT, 1, links.get(40).text());
            setValue(WATER_HOT, 2, links.get(42).text());
            setValue(WATER_HOT, 3, links.get(44).text());
        }

        if (mServicesCount > 2) {
            setValue(ELECTRICITY_DAY, 0, links.get(48).text());
            setValue(ELECTRICITY_DAY, 1, links.get(50).text());
            setValue(ELECTRICITY_DAY, 2, links.get(52).text());
            setValue(ELECTRICITY_DAY, 3, links.get(54).text());
        }

        if (mServicesCount > 3) {
            setValue(ELECTRICITY_NIGHT, 0, links.get(58).text());
            setValue(ELECTRICITY_NIGHT, 1, links.get(60).text());
            setValue(ELECTRICITY_NIGHT, 2, links.get(62).text());
            setValue(ELECTRICITY_NIGHT, 3, links.get(64).text());
        }
    }
}
