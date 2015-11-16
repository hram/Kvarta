package hram.kvarta.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnClick;
import droidkit.content.BoolValue;
import droidkit.content.TypedBundle;
import droidkit.content.Value;
import hram.kvarta.R;
import hram.kvarta.data.Account;
import hram.kvarta.events.BusProvider;
import hram.kvarta.events.UserLoginEndedEvent;
import hram.kvarta.events.UserLoginErrorEvent;
import hram.kvarta.events.UserLoginStartedEvent;
import hram.kvarta.network.UserLoginService;

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

    Bus bus = BusProvider.getInstance();

    public static void start(Activity context) {
        context.startActivityForResult(new Intent(context, LoginActivity.class), LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        if (getIntent().getExtras() != null) {
            mArgs = TypedBundle.from(getIntent().getExtras(), Args.class);
        } else {
            mArgs = TypedBundle.from(new Bundle(), Args.class);
        }

        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        bus.unregister(this);
    }

    @OnClick(R.id.sign_in_button)
    public void onSignInButtonClick() {
        attemptLogin();
    }

    @OnClick(R.id.sign_in_button_demo)
    public void onSignInDemoButtonClick() {
        attemptLoginDemo();
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
            startService(UserLoginService.getStartIntent(getApplicationContext(), tsgId, accountId, password, false));
        }
    }

    private void attemptLoginDemo() {
        showProgress(true);
        startService(UserLoginService.getStartDemoIntent(getApplicationContext()));
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
        if (!mArgs.switchOffAnimations().get() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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

    @Subscribe
    public void userLoginStarted(UserLoginStartedEvent event) {
        showProgress(true);
    }

    @Subscribe
    public void userLoginEnded(UserLoginEndedEvent event) {
        setResult(RESULT_OK);
        finish();
    }

    @Subscribe
    public void userLoginError(UserLoginErrorEvent event) {
        showProgress(false);
        mPasswordView.setError(getString(R.string.error_incorrect_password));
        mPasswordView.requestFocus();
    }

    public interface Args {
        @Value
        BoolValue toMockNetwork();

        @Value
        BoolValue networkResult();

        @Value
        BoolValue switchOffAnimations();
    }
}

