package com.chimple.parentalcontrol.view;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.adapter.ApprovedAppAdapter;
import com.chimple.parentalcontrol.databinding.ActivityMainBinding;
import com.chimple.parentalcontrol.firebase.Constant;
import com.chimple.parentalcontrol.model.AppModel;
import com.chimple.parentalcontrol.services.PersistentForegroundService;
import com.chimple.parentalcontrol.util.LocalPreference;
import com.chimple.parentalcontrol.util.PinVerificationUtil;
import com.chimple.parentalcontrol.util.VUtil;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SwitchCompat childModeSwitch;
    private boolean isSwitchOn = false;
    private final boolean isPinDialogVisible = false;
    private boolean previousSwitchState = false;
    private AlertDialog alertDialog;
    private final ActivityResultLauncher<Intent> homeSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (isDefaultLauncher()) {
                startAppChecker();
            } else {
                defaultLauncherSetting();
            }
        }
    });


    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        childModeSwitch = binding.childModeBtn;
        isSwitchOn = LocalPreference.getChildModeStatus().equals("on");
        childModeSwitch.setChecked(isSwitchOn);

        if (isSwitchOn && LocalPreference.getChildModeStatus().equals("on") && !isServiceRunning(getApplicationContext(), PersistentForegroundService.class)) {
            startAppChecker();
        }


        childModeSwitch.setOnClickListener(v -> {
            if (!isPinDialogVisible) {
                previousSwitchState = isSwitchOn;
                PinVerificationUtil.showPinDialog(MainActivity.this, new PinVerificationUtil.PinVerificationCallbacks() {
                    @Override
                    public void onCorrectPin() {
                        isSwitchOn = !isSwitchOn;
                        childModeSwitch.setChecked(isSwitchOn);
                        updateChildModeStatus();
                        if (isSwitchOn) {
                            VUtil.showSuccessToast(MainActivity.this, "Kid Mode ON");
                            defaultLauncherSetting();
                        } else {
                            defaultLauncherSetting();
                            closeAppChecker();
                            VUtil.showSuccessToast(MainActivity.this, "Kid Mode OFF");
                        }
                    }

                    @Override
                    public void onWrongPin() {
                        VUtil.showErrorToast(MainActivity.this, "Incorrect PIN");
                    }

                    @Override
                    public void onCloseDialog() {
                        isSwitchOn = previousSwitchState;
                        childModeSwitch.setChecked(isSwitchOn);
                    }
                });
            }
        });

        KeyboardVisibilityEvent.setEventListener(MainActivity.this, isOpen -> {
            if (isOpen) {
                binding.bottomNavigationView.setVisibility(View.GONE);
            } else {
                binding.bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        binding.bottomNavigationView.setOnItemReselectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_app) {
                navigateActivity(AppListActivity.class);
            } else if (menuItem.getItemId() == R.id.menu_setting) {
                navigateActivity(SettingActivity.class);
            }
        });

        final int[] clickCount = {0};
        binding.title.setOnClickListener(v -> {
            clickCount[0]++;
            if (clickCount[0] == 4) {
                clickCount[0] = 0;
                String storedPin = LocalPreference.getPin();
                VUtil.showSuccessToast(MainActivity.this, "PIN: " + storedPin);
            }
        });

        binding.logo.setOnClickListener(v -> Constant.mAuth.signOut());
    }

    private void defaultLauncherSetting() {
        Intent callHomeSettingIntent = new Intent(Settings.ACTION_HOME_SETTINGS);
        homeSettingsLauncher.launch(callHomeSettingIntent);
    }

    private void startAppChecker() {
        Intent serviceIntent = new Intent(this, PersistentForegroundService.class);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
    }

    private void closeAppChecker() {
        Intent serviceIntent = new Intent(this, PersistentForegroundService.class);
        stopService(serviceIntent);
    }

    private void navigateActivity(Class<?> activityClass) {
        PinVerificationUtil.showPinDialog(MainActivity.this, new PinVerificationUtil.PinVerificationCallbacks() {
            @Override
            public void onCorrectPin() {
                Intent intent = new Intent(MainActivity.this, activityClass);
                startActivity(intent);
            }

            @Override
            public void onWrongPin() {
                VUtil.showErrorToast(MainActivity.this, "Incorrect PIN");
            }

            @Override
            public void onCloseDialog() {
                // Handle dialog closure if needed
            }
        });
    }

    private void updateChildModeStatus() {
        LocalPreference.saveChildModeStatus(isSwitchOn ? "on" : "off");
    }

    private void showApprovedApps() {
        List<AppModel> approvedAppsList = VUtil.getApprovedAppsList(getPackageManager(), MainActivity.this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        binding.approvedAppsRecyclerView.setLayoutManager(layoutManager);
        ApprovedAppAdapter adapter = new ApprovedAppAdapter(this, approvedAppsList);
        binding.approvedAppsRecyclerView.setAdapter(adapter);

        if (approvedAppsList.isEmpty()) {
            binding.noAppView.setVisibility(View.VISIBLE);
        } else {
            binding.noAppView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isSwitchOn) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH || event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(this, SettingActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOverlayPermission();
        showApprovedApps();

        if (!isDefaultLauncher()) {
            if (isSwitchOn && LocalPreference.getChildModeStatus().equals("on") && !isDefaultLauncher()) {
                showSetDefaultLauncherDialog();
            }
        }

        if (!isSwitchOn && LocalPreference.getChildModeStatus().equals("off") && isServiceRunning(getApplicationContext(), PersistentForegroundService.class)) {
            closeAppChecker();
        }
    }


    boolean isDefaultLauncher() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<>();
        final PackageManager packageManager = getPackageManager();
        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void showSetDefaultLauncherDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            return; // Prevent multiple dialogs
        }

        AlertDialog.Builder builder = getBuilder();
        builder.setNegativeButton("Set as Default", (dialog, which) -> {
            defaultLauncherSetting();
            startAppChecker();
        });

        AlertDialog dialog = builder.create();
        dialog.show();


        alertDialog = builder.create();
        alertDialog.show();
    }

    @NonNull
    private AlertDialog.Builder getBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Child Mode Active");
        builder.setMessage("For proper functionality, please set the Chimple app as the default launcher.");
        builder.setPositiveButton("Cancel Child Mode", (dialog, which) -> {
            isSwitchOn = false;
            childModeSwitch.setChecked(false);
            updateChildModeStatus();
            closeAppChecker();
            VUtil.showSuccessToast(getApplicationContext(), "Child mode is off");
        });
        return builder;
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);

        if (isInMultiWindowMode) {
            VUtil.showErrorToast(MainActivity.this, "Multi-window mode is not supported");
            finish();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);

        if (isInPictureInPictureMode) {
            VUtil.showErrorToast(MainActivity.this, "Picture-in-picture mode is not supported");
            finish();
        }
    }
}
