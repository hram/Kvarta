package hram.kvarta;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnClick;
import droidkit.content.BoolValue;
import droidkit.content.TypedBundle;
import droidkit.content.Value;

import static android.Manifest.permission.INTERNET;

/**
 * @author Evgeny Hramov
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity INTERNET permission request.
     */
    private static final int REQUEST_INTERNET = 1;

    public static final int LOGIN_REQUEST_CODE = 100;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    @InjectView(R.id.tsgid)
    private EditText mTsgIdView;

    @InjectView(R.id.accountid)
    private EditText mAccountIdView;

    @InjectView(R.id.password)
    private EditText mPasswordView;

    @InjectView(R.id.layout_progress)
    private View mProgressView;

    @InjectView(R.id.login_form)
    private View mLoginFormView;

    private Args mArgs;

    public static void start(Activity context) {
        context.startActivityForResult(new Intent(context, LoginActivity.class), LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //DroidKit.inject(this, this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Account account = new Account(this);
        if (account.isValid()) {
            mTsgIdView.setText(account.getTsgId());
            mAccountIdView.setText(account.getAccountId());
            mPasswordView.setText(account.getPassword());
        }

        if(getIntent().getExtras() != null) {
            mArgs = TypedBundle.from(getIntent().getExtras(), LoginActivity.Args.class);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //DroidKit.onResume(this);
    }

    @Override
    public void onPause() {
        //DroidKit.onPause(this);
        super.onPause();
    }

    @OnClick(R.id.sign_in_button)
    public void onEmailSignInButtonClick(View v) {
        attemptLogin();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean mayConnectInternet() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(INTERNET) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(INTERNET)) {
            Snackbar.make(mAccountIdView, R.string.permission_inernet, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{INTERNET}, REQUEST_INTERNET);
                        }
                    });
        } else {
            requestPermissions(new String[]{INTERNET}, REQUEST_INTERNET);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_INTERNET) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                attemptLogin();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (!mayConnectInternet()) {
            return;
        }
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mTsgIdView.setError(null);
        mAccountIdView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String tsgId = mTsgIdView.getText().toString();
        String accountId = mAccountIdView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) /*&& !isPasswordValid(password)*/) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid account ID.
        if (TextUtils.isEmpty(accountId)) {
            mAccountIdView.setError(getString(R.string.error_field_required));
            focusView = mAccountIdView;
            cancel = true;
        } else if (!isAccountIdValid(accountId)) {
            mAccountIdView.setError(getString(R.string.error_invalid_account_id));
            focusView = mAccountIdView;
            cancel = true;
        }

        // Check for a valid tsg ID.
        if (TextUtils.isEmpty(tsgId)) {
            mTsgIdView.setError(getString(R.string.error_field_required));
            focusView = mTsgIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(tsgId, accountId, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isAccountIdValid(String accountId) {
        return accountId.length() == 9;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mTsgID;
        private final String mAccountID;
        private final String mPassword;

        OkHttpClient client = OkClient.create(getApplicationContext());

        UserLoginTask(String tsgID, String accountID, String password) {
            mTsgID = tsgID;
            mAccountID = accountID;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(mArgs != null && mArgs.toMockNetwork().get()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                return mArgs.networkResult().get();
            }

            try {
                Request request = new Request.Builder().url("http://www2.kvarta-c.ru/voda.php?action=login").build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);

                ResponseBody body = response.body();
                String respString = body.string();
                if (!respString.contains("form_tenant"))
                    throw new IOException();

                RequestBody formBody = new FormEncodingBuilder()
                        .add("action", "login")
                        .add("subaction", "enter")
                        .add("usertype", "tenant")
                        .add("tsgid", mTsgID)
                        .add("accountid", mAccountID)
                        .add("password", mPassword)
                        .build();

                request = new Request.Builder()
                        .url("http://www2.kvarta-c.ru/voda.php")
                        .post(formBody)
                        .build();

                response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);

                body = response.body();
                respString = body.string();
                if (!respString.contains("Logged in. Click to continue"))
                    throw new IOException();

                request = new Request.Builder().url("http://www2.kvarta-c.ru/voda.php?action=tenant").build();
                response = client.newCall(request).execute();
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

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                new Account.Builder()
                        .accountId(mAccountID)
                        .tsgId(mTsgID)
                        .password(mPassword).build(getApplicationContext());
                setResult(RESULT_OK);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public interface Args {
        @Value
        BoolValue toMockNetwork();

        @Value
        BoolValue networkResult();
    }
}

