package hram.kvarta;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnClick;
import droidkit.content.TypedPrefs;
import io.fabric.sdk.android.Fabric;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import static android.Manifest.permission.CAMERA;

/**
 * @author Evgeny Hramov
 */
public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    //private final static String TAG = "kvarta";
    private static final int REQUEST_CAMERA = 1;
    Settings mSettings;
    Account mAccount;
    private AsyncTask<Void, Void, Boolean> mTask = null;

    @InjectView(R.id.layout_progress)
    View mLayoutProgress;

    @InjectView(R.id.layout_main)
    View mLayoutMain;

    @InjectView(R.id.layout_usetInfo)
    View mLayoutUsetInfo;

    @InjectView(R.id.tvAddress)
    TextView mAddress;

    @InjectView(R.id.tvUserInfo)
    TextView mUserInfo;

    @InjectView(R.id.numberPickerC5)
    NumberPicker mNPC5;

    @InjectView(R.id.numberPickerC4)
    NumberPicker mNPC4;

    @InjectView(R.id.numberPickerC3)
    NumberPicker mNPC3;

    @InjectView(R.id.numberPickerC2)
    NumberPicker mNPC2;

    @InjectView(R.id.numberPickerC1)
    NumberPicker mNPC1;

    @InjectView(R.id.numberPickerH5)
    NumberPicker mNPH5;

    @InjectView(R.id.numberPickerH4)
    NumberPicker mNPH4;

    @InjectView(R.id.numberPickerH3)
    NumberPicker mNPH3;

    @InjectView(R.id.numberPickerH2)
    NumberPicker mNPH2;

    @InjectView(R.id.numberPickerH1)
    NumberPicker mNPH1;

    @InjectView(R.id.flash)
    ImageView mFlash;

    @InjectView(R.id.action_save)
    com.getbase.floatingactionbutton.FloatingActionButton mActionSave;

    private final long[] hotValues = new long[4];
    private final long[] coldValues = new long[4];
    private String mNewValueHot, mNewValueCold;
    private Camera mCamera;
    private String mCurrentHotValue;
    private String mCurrentColdValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        //DroidKit.inject(this, this);

        initNumberPicker(mNPC1);
        initNumberPicker(mNPC2);
        initNumberPicker(mNPC3);
        initNumberPicker(mNPC4);
        initNumberPicker(mNPC5);

        initNumberPicker(mNPH1);
        initNumberPicker(mNPH2);
        initNumberPicker(mNPH3);
        initNumberPicker(mNPH4);
        initNumberPicker(mNPH5);

        setDividerColor(mNPH1, Color.RED);
        setDividerColor(mNPH2, Color.RED);
        setDividerColor(mNPH3, Color.RED);
        setDividerColor(mNPH4, Color.RED);
        setDividerColor(mNPH5, Color.RED);

        mSettings = TypedPrefs.from(this, Settings.class);
        mAccount = new Account(this);

        setTitle(R.string.title_activity_main);
        //mAccount.reset();

        //setTitle(getString(R.string.selected_configuration));

        if (savedInstanceState == null) {
            getInfo();
        } else {
            displayCurrentState(mAccount.getAddress(), mAccount.getUserInfo(), savedInstanceState.getString("current_hot_value"), savedInstanceState.getString("current_cold_value"));
        }
    }

    private void getInfo() {
        if (!mAccount.isValid()) {
            LoginActivity.start(this);
        } else {
            startGetInfoTask();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //DroidKit.onResume(this);
        mLayoutUsetInfo.setVisibility(mSettings.enableUserInfo().get() ? View.VISIBLE : View.GONE);

        AlarmManager.setAlarm(getApplicationContext());
    }

    @Override
    public void onPause() {
        //DroidKit.onPause(this);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LoginActivity.LOGIN_REQUEST_CODE:
                if (resultCode != RESULT_OK) {
                    finish();
                    return;
                }
                startGetInfoTask();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!TextUtils.isEmpty(mCurrentColdValue) && !TextUtils.isEmpty(mCurrentHotValue)) {
            outState.putString("current_cold_value", mCurrentColdValue);
            outState.putString("current_hot_value", mCurrentHotValue);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsActivity.start(this);
                return true;
            case R.id.action_reload:
                getInfo();
                return true;
            case R.id.action_logout:
                mAccount.reset();
                getInfo();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

            mLayoutMain.setVisibility(show ? View.GONE : View.VISIBLE);
            mLayoutMain.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLayoutMain.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mLayoutProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mLayoutProgress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLayoutProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLayoutProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mLayoutMain.setVisibility(show ? View.GONE : View.VISIBLE);
        }

        if (show) {
            showActionMenu(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showActionMenu(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mActionSave.setVisibility(show ? View.VISIBLE : View.GONE);
            mActionSave.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //mActionMenu.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mActionSave.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void initNumberPicker(NumberPicker picker) {
        picker.setMinValue(0);
        picker.setMaxValue(9);
        picker.setOnValueChangedListener(this);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void displayCurrentState(String address, String userInfo, String currentHotValue, String currentColdValue) {
        mAddress.setText(address);
        mUserInfo.setText(userInfo);

        mCurrentHotValue = currentHotValue;
        for (int i = 0; i < mCurrentHotValue.length(); i++) {
            switch (mCurrentHotValue.length() - i) {
                case 1:
                    mNPH1.setValue(Integer.parseInt("" + mCurrentHotValue.charAt(i)));
                    break;
                case 2:
                    mNPH2.setValue(Integer.parseInt("" + mCurrentHotValue.charAt(i)));
                    break;
                case 3:
                    mNPH3.setValue(Integer.parseInt("" + mCurrentHotValue.charAt(i)));
                    break;
                case 4:
                    mNPH4.setValue(Integer.parseInt("" + mCurrentHotValue.charAt(i)));
                    break;
                case 5:
                    mNPH5.setValue(Integer.parseInt("" + mCurrentHotValue.charAt(i)));
                    break;
            }
        }

        mCurrentColdValue = currentColdValue;
        for (int i = 0; i < mCurrentColdValue.length(); i++) {
            switch (mCurrentColdValue.length() - i) {
                case 1:
                    mNPC1.setValue(Integer.parseInt("" + mCurrentColdValue.charAt(i)));
                    break;
                case 2:
                    mNPC2.setValue(Integer.parseInt("" + mCurrentColdValue.charAt(i)));
                    break;
                case 3:
                    mNPC3.setValue(Integer.parseInt("" + mCurrentColdValue.charAt(i)));
                    break;
                case 4:
                    mNPC4.setValue(Integer.parseInt("" + mCurrentColdValue.charAt(i)));
                    break;
                case 5:
                    mNPC5.setValue(Integer.parseInt("" + mCurrentColdValue.charAt(i)));
                    break;
            }
        }
    }

    @OnClick(R.id.flash)
    public void onFlashViewClick(View v) {
        if (mayCreateCamera()) {
            cameraOnOff();
        }
    }

    private void cameraOnOff() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                mCamera.startPreview();
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                mFlash.setImageResource(R.drawable.flash_on);
            } catch (java.lang.NullPointerException e) {
                showSnackbar(getString(R.string.camera_not_found));
            } catch (Exception e) {
                showSnackbar(getString(R.string.camera_open_error));
            }
        } else {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                mFlash.setImageResource(R.drawable.flash_off);
            } catch (Exception e) {
                showSnackbar(getString(R.string.camera_close_error));
            }

        }
    }

    private void showSnackbar(String text) {
        Snackbar.make(mFlash, text, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
            }
        }).show();
    }

    @OnClick(R.id.action_save)
    public void onActionSaveClick(View v) {
        startSaveTask();
    }

    private void startGetInfoTask() {
        showProgress(true);
        mTask = new GetInfoTask();
        mTask.execute((Void) null);
    }

    private void startSaveTask() {
        mNewValueHot = "" + mNPH5.getValue() + mNPH4.getValue() + mNPH3.getValue() + mNPH2.getValue() + mNPH1.getValue();
        mNewValueCold = "" + mNPC5.getValue() + mNPC4.getValue() + mNPC3.getValue() + mNPC2.getValue() + mNPC1.getValue();
        showProgress(true);
        mTask = new SaveTask();
        mTask.execute((Void) null);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if (mActionSave.getVisibility() != View.VISIBLE) {
            showActionMenu(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean mayCreateCamera() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
            Snackbar.make(mFlash, R.string.permission_camera, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(View v) {
                    requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraOnOff();
                } else {
                    mFlash.setVisibility(View.GONE);
                }
                return;
            }
        }

    }

    public class GetInfoTask extends AsyncTask<Void, Void, Boolean> {

        OkHttpClient client = OkClient.create(getApplicationContext());

        GetInfoTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Request request = new Request.Builder().url("http://www2.kvarta-c.ru/voda.php?action=tenant").build();
                Response response = client.newCall(request).execute();
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

        private void setColdValue(int index, String value) {
            try {
                coldValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
            } catch (NumberFormatException nfe) {
                coldValues[index] = -1;
                //Log.d(TAG, "Cold valie " + index + " error");
            }
        }

        private void setHotValue(int index, String value) {
            try {
                hotValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
            } catch (NumberFormatException nfe) {
                hotValues[index] = -1;
                //Log.d(TAG, "Hot valie " + index + " error");
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mTask = null;
            showProgress(false);

            if (success) {
                displayCurrentState(mAccount.getAddress(), mAccount.getUserInfo(), "" + hotValues[0], "" + coldValues[0]);
            } else {
                LoginActivity.start(MainActivity.this);
            }
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            showProgress(false);
        }
    }

    public class SaveTask extends AsyncTask<Void, Void, Boolean> {

        OkHttpClient client = OkClient.create(getApplicationContext());

        SaveTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                RequestBody formBody = new FormEncodingBuilder()
                        .add("action", "tenant")
                        .add("subaction", "tenantedit")
                        .add("service1counter1", mNewValueCold)
                        .add("service2counter1", mNewValueHot)
                        .add("put", "Сохранить")
                        .build();

                Request request = new Request.Builder()
                        .url("http://www2.kvarta-c.ru/voda.php")
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                ResponseBody body = response.body();
                String respString = body.string();
                if (!respString.contains("Data is updated."))
                    throw new IOException();

                Document doc = Jsoup.parse(respString);
                Element link = doc.select("a[href]").first();
                //Log.d(TAG, link.attr("href"));

                request = new Request.Builder().url("http://www2.kvarta-c.ru/" + link.attr("href")).build();
                response = client.newCall(request).execute();
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
                //Log.d(TAG, "Cold valie " + index + " error");
            }
        }

        private void setHotValue(int index, String value) {
            try {
                hotValues[index] = Long.parseLong(value.replaceAll("\\s+", ""));
            } catch (NumberFormatException nfe) {
                hotValues[index] = -1;
                //Log.d(TAG, "Hot valie " + index + " error");
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                displayCurrentState(mAccount.getAddress(), mAccount.getUserInfo(), "" + hotValues[0], "" + coldValues[0]);
            } else {
                LoginActivity.start(MainActivity.this);
            }

            mTask = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            showProgress(false);
        }
    }
}
