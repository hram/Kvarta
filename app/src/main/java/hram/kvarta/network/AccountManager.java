package hram.kvarta.network;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.net.URL;

import hram.kvarta.Account;

/**
 * @author Evgeny Khramov
 */
public class AccountManager extends BaseManager {

    public boolean logIn(Account account) {
        return logIn(account.getTsgId(), account.getAccountId(), account.getPassword(), account.isDemo());
    }

    public boolean logIn(String tsgID, String accountID, String password, boolean demo) {
        try {
            Request request = new Request.Builder().url(createUrl("/voda.php?action=login")).build();
            Response response = mClient.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            ResponseBody body = response.body();
            String respString = body.string();
            if (!respString.contains("form_tenant"))
                throw new IOException();

            RequestBody formBody;
            if (!demo) {
                formBody = new FormEncodingBuilder()
                        .add("action", "login")
                        .add("subaction", "enter")
                        .add("usertype", "tenant")
                        .add("tsgid", tsgID)
                        .add("accountid", accountID)
                        .add("password", password)
                        .build();
            } else {
                // "", "000000000", "демо"
                formBody = new FormEncodingBuilder()
                        .add("action", "login")
                        .add("subaction", "enter")
                        .add("usertype", "tenant")
                        .add("tsgid", "")
                        .add("accountid", "000000000")
                        .addEncoded("password", "%E4%E5%EC%EE")
                        .build();
            }

            request = new Request.Builder()
                    .url(createUrl("/voda.php"))
                    .post(formBody)
                    .build();

            response = mClient.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            body = response.body();
            respString = body.string();
            if (!respString.contains("Logged in. Click to continue"))
                throw new IOException();

            request = new Request.Builder().url(new URL(mHttpUrl.url(), "/voda.php?action=tenant")).build();
            response = mClient.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            body = response.body();
            respString = body.string();
            if (!respString.contains("Номер лицевого счета"))
                throw new IOException();

        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
