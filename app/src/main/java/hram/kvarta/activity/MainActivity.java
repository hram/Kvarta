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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import hram.kvarta.events.LoadDataEndedEvent;
import hram.kvarta.events.LoadDataStartedEvent;
import hram.kvarta.events.LogInErrorEvent;
import hram.kvarta.events.NetworkErrorEvent;
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

    private static final int REQUEST_CAMERA = 1;
    private Settings mSettings;
    private Account mAccount;
    private AlarmManager mAlarmManager;
    private Args mArgs;

    @InjectView(R.id.layout_usetInfo)
    private View mLayoutUsetInfo;

    @InjectView(R.id.tvAddress)
    private TextView mAddress;

    @InjectView(R.id.tvUserInfo)
    private TextView mUserInfo;

    @InjectView(R.id.numberPickerC5)
    private NumberPicker mNPC5;

    @InjectView(R.id.numberPickerC4)
    private NumberPicker mNPC4;

    @InjectView(R.id.numberPickerC3)
    private NumberPicker mNPC3;

    @InjectView(R.id.numberPickerC2)
    private NumberPicker mNPC2;

    @InjectView(R.id.numberPickerC1)
    private NumberPicker mNPC1;

    @InjectView(R.id.numberPickerH5)
    private NumberPicker mNPH5;

    @InjectView(R.id.numberPickerH4)
    private NumberPicker mNPH4;

    @InjectView(R.id.numberPickerH3)
    private NumberPicker mNPH3;

    @InjectView(R.id.numberPickerH2)
    private NumberPicker mNPH2;

    @InjectView(R.id.numberPickerH1)
    private NumberPicker mNPH1;

    @InjectView(R.id.flash)
    private ImageView mFlash;

    @InjectView(R.id.action_save)
    private View mActionSave;

    private String mNewValueHot, mNewValueCold;
    private Camera mCamera;
    private String mCurrentHotValue;
    private String mCurrentColdValue;

    private int[] mLayoutIds = new int[]{R.id.layout_progress, R.id.layout_main, R.id.layout_network_error};
    private SparseArray<View> mLayouts = new SparseArray<>();
    private int mShowedLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int id : mLayoutIds) {
            final View view = findViewById(id);
            if (view == null) {
                continue;
            }

            mLayouts.put(id, view);
        }

        if (getIntent().getExtras() != null) {
            mArgs = TypedBundle.from(getIntent().getExtras(), Args.class);
        } else {
            mArgs = TypedBundle.from(new Bundle(), Args.class);
        }
        showLayout(R.id.layout_progress);

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

        setTitle(R.string.title_activity_main);

        //setTitle(getString(R.string.selected_configuration));

        SavedInstanceArgs savedInstanceArgs;
        if (savedInstanceState != null) {
            savedInstanceArgs = TypedBundle.from(savedInstanceState, SavedInstanceArgs.class);
        } else {
            savedInstanceArgs = TypedBundle.from(new Bundle(), SavedInstanceArgs.class);
        }


        if (!TextUtils.isEmpty(savedInstanceArgs.newColdValue().get()) && !TextUtils.isEmpty(savedInstanceArgs.newHotValue().get())) {
            displayCurrentState(mAccount.getAddress(), mAccount.getUserInfo(), savedInstanceArgs.newHotValue().get(), savedInstanceArgs.newColdValue().get());
            showActionMenu(savedInstanceArgs.actionSaveDisplayed().get());
        } else if (!TextUtils.isEmpty(savedInstanceArgs.currentColdValue().get()) && !TextUtils.isEmpty(savedInstanceArgs.currentHotValue().get())) {
            displayCurrentState(mAccount.getAddress(), mAccount.getUserInfo(), savedInstanceArgs.currentHotValue().get(), savedInstanceArgs.currentColdValue().get());
            showActionMenu(savedInstanceArgs.actionSaveDisplayed().get());
        } else {
            getInfo();
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
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLayoutUsetInfo.setVisibility(mSettings.enableUserInfo().get() ? View.VISIBLE : View.GONE);
        mAlarmManager.setAlarm();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFlash.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
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

        mNewValueHot = "" + mNPH5.getValue() + mNPH4.getValue() + mNPH3.getValue() + mNPH2.getValue() + mNPH1.getValue();
        mNewValueCold = "" + mNPC5.getValue() + mNPC4.getValue() + mNPC3.getValue() + mNPC2.getValue() + mNPC1.getValue();

        final SavedInstanceArgs savedInstanceArgs = TypedBundle.from(outState, SavedInstanceArgs.class);
        savedInstanceArgs.currentHotValue().set(mCurrentHotValue);
        savedInstanceArgs.currentColdValue().set(mCurrentColdValue);
        savedInstanceArgs.newHotValue().set(mNewValueHot);
        savedInstanceArgs.newColdValue().set(mNewValueCold);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showLayout(int layoutId) {
        if(mShowedLayout == layoutId){
            return;
        }

        if (!mArgs.disableAnimation().get() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            for (int i = 0; i < mLayouts.size(); i++) {
                int key = mLayouts.keyAt(i);
                // get the object by the key.
                final View view = mLayouts.get(key);

                if (key == layoutId) {
                    view.setVisibility(View.VISIBLE);
                    view.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    view.setVisibility(View.GONE);
                    view.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(View.GONE);
                        }
                    });
                }
            }
        } else {

            for (int i = 0; i < mLayouts.size(); i++) {
                int key = mLayouts.keyAt(i);
                // get the object by the key.
                View view = mLayouts.get(key);
                if (key == layoutId) {
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }

        mShowedLayout = layoutId;
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

    private void initNumberPicker(@NonNull NumberPicker picker) {
        picker.setMinValue(0);
        picker.setMaxValue(9);
        picker.setOnValueChangedListener(this);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    private void setDividerColor(@NonNull NumberPicker picker, @ColorInt int color) {

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
    private void displayCurrentState(String address, String userInfo, @NonNull String currentHotValue, @NonNull String currentColdValue) {
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

        showLayout(R.id.layout_main);
        showActionMenu(false);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.flash)
    public void onFlashViewClick() {
        if (mayCreateCamera()) {
            cameraOnOff();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.button_try_again)
    public void onTryAgainViewClick() {
        getInfo();
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

    @SuppressWarnings("unused")
    @OnClick(R.id.action_save)
    public void onActionSaveClick() {
        startSaveInfoTask();
    }

    private void startGetInfoTask() {
        showLayout(R.id.layout_progress);
        startService(GetInfoService.getStartIntent(this));
    }

    private void startSaveInfoTask() {
        mNewValueHot = "" + mNPH5.getValue() + mNPH4.getValue() + mNPH3.getValue() + mNPH2.getValue() + mNPH1.getValue();
        mNewValueCold = "" + mNPC5.getValue() + mNPC4.getValue() + mNPC3.getValue() + mNPC2.getValue() + mNPC1.getValue();
        showLayout(R.id.layout_progress);
        startService(SaveInfoService.getStartIntent(this, mNewValueHot, mNewValueCold));
    }

    @Override
    public void onValueChange(@NonNull NumberPicker picker, int oldVal, int newVal) {
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

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loadDataStarted(LoadDataStartedEvent event) {
        showLayout(R.id.layout_progress);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveDataStarted(SaveDataStartedEvent event) {
        showLayout(R.id.layout_progress);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loadDataEnded(LoadDataEndedEvent event) {
        displayCurrentState(mAccount.getAddress(), mAccount.getUserInfo(), "" + event.getValue(ValuesManager.WATER_HOT), "" + event.getValue(ValuesManager.WATER_COLD));
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void logInError(LogInErrorEvent event) {
        LoginActivity.start(this);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void networkError(NetworkErrorEvent event) {
        showLayout(R.id.layout_network_error);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
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
        StringValue newHotValue();

        @Value
        StringValue newColdValue();

        @Value
        BoolValue actionSaveDisplayed();
    }
}
