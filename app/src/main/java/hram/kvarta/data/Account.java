package hram.kvarta.data;

import android.content.Context;
import android.text.TextUtils;

import droidkit.content.TypedPrefs;
import hram.kvarta.network.PersistentCookieStore;

/**
 * @author Evgeny Hramov
 */
public class Account {

    private static final String EMPTY_STRING = "";
    private Settings mSettings;
    private Context mContext;

    public String getTsgId() {
        return mSettings.tsgId().get();
    }

    public void setTsgId(String tsgId) {
        mSettings.tsgId().set(tsgId);
    }

    public String getAccountId() {
        return mSettings.accountId().get();
    }

    public void setAccountId(String accountId) {
        mSettings.accountId().set(accountId);
    }

    public String getPassword() {
        return mSettings.password().get();
    }

    public void setPassword(String password) {
        mSettings.password().set(password);
    }

    public boolean isDemo() {
        return mSettings.demo().get();
    }

    public void setDemo(boolean demo) {
        mSettings.demo().set(demo);
    }

    public String getAddress() {
        return mSettings.address().get();
    }

    public void setAddress(String address) {
        mSettings.address().set(address);
    }

    public String getUserInfo() {
        return mSettings.userInfo().get();
    }

    public void setUserInfo(String userInfo) {
        mSettings.userInfo().set(userInfo);
    }

    public String getLastTime() {
        return mSettings.lastTime().get();
    }

    public void setLastTime(String lastTime) {
        mSettings.lastTime().set(lastTime);
    }

    public Account(Context context) {
        mContext = context;
        mSettings = TypedPrefs.from(context, Settings.class);
    }

    private Account(Context context, Builder builder) {
        this(context);

        mSettings.tsgId().set(builder.mTsgId);
        mSettings.accountId().set(builder.mAccountId);
        mSettings.password().set(builder.mPassword);
        mSettings.demo().set(builder.mDemo);
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(getTsgId()) && !TextUtils.isEmpty(getAccountId()) && !TextUtils.isEmpty(getPassword());
    }

    public void reset() {
        mSettings.tsgId().set(EMPTY_STRING);
        mSettings.accountId().set(EMPTY_STRING);
        mSettings.password().set(EMPTY_STRING);
        new PersistentCookieStore(mContext).removeAll();
    }

    public static class Builder {
        private String mTsgId;
        private String mAccountId;
        private String mPassword;
        private boolean mDemo;

        public Builder() {
        }

        public Builder tsgId(String tsgId) {
            if (tsgId == null) throw new IllegalArgumentException("tsgId == null");
            mTsgId = tsgId;
            return this;
        }

        public Builder accountId(String accountId) {
            if (accountId == null) throw new IllegalArgumentException("accountId == null");
            mAccountId = accountId;
            return this;
        }

        public Builder password(String password) {
            if (password == null) throw new IllegalArgumentException("password == null");
            mPassword = password;
            return this;
        }

        public Builder demo(boolean demo) {
            mDemo = demo;
            return this;
        }

        public Account build(Context context) {
            if (mTsgId == null) throw new IllegalStateException("mTsgId == null");
            if (mAccountId == null) throw new IllegalStateException("mAccountId == null");
            if (mPassword == null) throw new IllegalStateException("mPassword == null");
            return new Account(context, this);
        }
    }
}
