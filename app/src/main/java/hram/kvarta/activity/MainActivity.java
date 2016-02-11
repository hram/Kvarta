package hram.kvarta.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnClick;
import droidkit.content.BoolValue;
import droidkit.content.StringValue;
import droidkit.content.TypedBundle;
import droidkit.content.TypedPrefs;
import droidkit.content.Value;
import hram.kvarta.R;
import hram.kvarta.alarm.AlarmManager;
import hram.kvarta.data.Account;
import hram.kvarta.data.Settings;
import hram.kvarta.events.BusProvider;
import hram.kvarta.events.LoadDataEndedEvent;
import hram.kvarta.events.LoadDataStartedEvent;
import hram.kvarta.events.LogInErrorEvent;
import hram.kvarta.events.SaveDataErrorEvent;
import hram.kvarta.events.SaveDataStartedEvent;
import hram.kvarta.network.GetInfoService;
import hram.kvarta.network.SaveInfoService;
import hram.kvarta.network.ValuesManager;

import static android.Manifest.permission.CAMERA;

/**
 * @author Evgeny Hramov
 */
public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    //private final static String TAG = "kvarta";
    private static final int REQUEST_CAMERA = 1;
    Settings mSettings;
    Account mAccount;
    private AlarmManager mAlarmManager;
    private Args mArgs;

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
    View mActionSave;

    private String mNewValueHot, mNewValueCold;
    private Camera mCamera;
    private String mCurrentHotValue;
    private String mCurrentColdValue;
    Bus bus = BusProvider.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        mAlarmManager = new AlarmManager(getApplicationContext());

        if (getIntent().getExtras() != null) {
            mArgs = TypedBundle.from(getIntent().getExtras(), Args.class);
        } else {
            mArgs = TypedBundle.from(new Bundle(), Args.class);
        }

        setTitle(R.string.title_activity_main);

        //setTitle(getString(R.string.selected_configuration));

        SavedInstanceArgs savedInstanceArgs;
        if (savedInstanceState != null) {
            savedInstanceArgs = TypedBundle.from(savedInstanceState, SavedInstanceArgs.class);
        } else {
            savedInstanceArgs = TypedBundle.from(new Bundle(), SavedInstanceArgs.class);
        }

        if (StringValue.EMPTY.equals(savedInstanceArgs.currentColdValue().get()) || StringValue.EMPTY.equals(savedInstanceArgs.currentHotValue().get())) {
            getInfo();
        } else {
            displayCurrentState(mAccount.getAddress(), mAccount.getUserInfo(), savedInstanceArgs.currentHotValue().get(), savedInstanceArgs.currentColdValue().get());
            showActionMenu(savedInstanceArgs.actionSaveDisplayed().get());
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

        bus.register(this);

        mLayoutUsetInfo.setVisibility(mSettings.enableUserInfo().get() ? View.VISIBLE : View.GONE);
        mAlarmManager.setAlarm();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFlash.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        bus.unregister(this);
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

        SavedInstanceArgs savedInstanceArgs = TypedBundle.from(outState, SavedInstanceArgs.class);
        savedInstanceArgs.currentHotValue().set(mCurrentHotValue);
        savedInstanceArgs.currentColdValue().set(mCurrentColdValue);
        savedInstanceArgs.actionSaveDisplayed().set(mActionSave.getVisibility() == View.VISIBLE);
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
        if (!mArgs.disableAnimation().get() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        if (!mArgs.disableAnimation().get() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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

    /**
     * Сбрасывает все значения в 0
     */
    private void resetValues() {
        mNPH1.setValue(0);
        mNPH2.setValue(0);
        mNPH3.setValue(0);
        mNPH4.setValue(0);
        mNPH5.setValue(0);

        mNPC1.setValue(0);
        mNPC2.setValue(0);
        mNPC3.setValue(0);
        mNPC4.setValue(0);
        mNPC5.setValue(0);
    }

    /**
     * Отоплажает текущае значения
     *
     * @param address          адрес пользователя
     * @param userInfo         информация о пользователе
     * @param currentHotValue  новые значения хорячей воды
     * @param currentColdValue новые значения ходолной воды
     */
    private void displayCurrentState(String address, String userInfo, String currentHotValue, String currentColdValue) {
        mAddress.setText(address);
        mUserInfo.setText(userInfo);

        resetValues();

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
    public void onFlashViewClick() {
        if (mayCreateCamera()) {
            cameraOnOff();
        }
    }

    private void cameraOnOff() {
        if (mCamera == null) {
            //try {
            mCamera = Camera.open();
            mCamera.startPreview();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
            mFlash.setImageResource(R.drawable.flash_on);
            //} catch (java.lang.NullPointerException e) {
            //    showSnackbar(getString(R.string.camera_not_found));
            //} catch (Exception e) {
            //    showSnackbar(getString(R.string.camera_open_error));
            //}
        } else {
            //try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mFlash.setImageResource(R.drawable.flash_off);
            //} catch (Exception e) {
            //    showSnackbar(getString(R.string.camera_close_error));
            //}
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
    public void onActionSaveClick() {
        startSaveInfoTask();
    }

    private void startGetInfoTask() {
        showProgress(true);
        startService(GetInfoService.getStartIntent(this));
    }

    private void startSaveInfoTask() {
        mNewValueHot = "" + mNPH5.getValue() + mNPH4.getValue() + mNPH3.getValue() + mNPH2.getValue() + mNPH1.getValue();
        mNewValueCold = "" + mNPC5.getValue() + mNPC4.getValue() + mNPC3.getValue() + mNPC2.getValue() + mNPC1.getValue();
        showProgress(true);
        startService(SaveInfoService.getStartIntent(this, mNewValueHot, mNewValueCold));
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
            case REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraOnOff();
                } else {
                    mFlash.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Subscribe
    public void loadDataStarted(LoadDataStartedEvent event) {
        showProgress(true);
    }

    @Subscribe
    public void saveDataStarted(SaveDataStartedEvent event) {
        showProgress(true);
    }

    @Subscribe
    public void loadDataEnded(LoadDataEndedEvent event) {
        displayCurrentState(mAccount.getAddress(), mAccount.getUserInfo(), "" + event.getValue(ValuesManager.WATER_HOT), "" + event.getValue(ValuesManager.WATER_COLD));
        showProgress(false);
    }

    @Subscribe
    public void logInError(LogInErrorEvent event) {
        LoginActivity.start(this);
    }

    @Subscribe
    public void saveDataError(SaveDataErrorEvent event) {
        LoginActivity.start(this);
    }

    public interface Args {
        @Value
        BoolValue disableAnimation();
    }

    public interface SavedInstanceArgs {
        @Value
        StringValue currentHotValue();

        @Value
        StringValue currentColdValue();

        @Value
        BoolValue actionSaveDisplayed();
    }
}
