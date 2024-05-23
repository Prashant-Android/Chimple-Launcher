package com.chimple.parentalcontrol.view;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.ViewUtils;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.adapter.ApprovedAppAdapter;
import com.chimple.parentalcontrol.core.MyDeviceAdminReceiver;
import com.chimple.parentalcontrol.databinding.ActivityMainBinding;
import com.chimple.parentalcontrol.model.AppModel;
import com.chimple.parentalcontrol.services.ModeTrackerService;
import com.chimple.parentalcontrol.util.LocalPreference;
import com.chimple.parentalcontrol.util.VUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private SwitchCompat childModeSwitch;
    private boolean isSwitchOn = false;
    private boolean isPinDialogVisible = false;
    private boolean previousSwitchState = false;
    private boolean isLockTaskActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyDeviceAdminReceiver.class);

        childModeSwitch = binding.childModeBtn;
        childModeSwitch.setOnClickListener(null);

        childModeSwitch.setOnClickListener(v -> {
            if (!isPinDialogVisible) {
                previousSwitchState = isSwitchOn;
                showPinDialog();
            }
        });

        isSwitchOn = LocalPreference.getChildModeStatus().equals("on");
        childModeSwitch.setChecked(isSwitchOn);

        if (isSwitchOn) {
            enableKioskMode();
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_app) {
                startActivity(new Intent(MainActivity.this, AppListActivity.class));
                return true;
            } else if (menuItem.getItemId() == R.id.menu_setting) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                return true;
            }
            return false;
        });



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

    private void showPinDialog() {
        isPinDialogVisible = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String pinInput = input.getText().toString();
            if (pinInput.equals(LocalPreference.getPin())) {
                isSwitchOn = !isSwitchOn;
                childModeSwitch.setChecked(isSwitchOn);
                updateChildModeStatus();
                if (isSwitchOn) {
                    enableKioskMode();
                } else {
                    disableKioskMode();
                }
                Toast.makeText(this, "Mode Changed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                isSwitchOn = previousSwitchState;
                childModeSwitch.setChecked(isSwitchOn);
            }
            isPinDialogVisible = false;
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            isPinDialogVisible = false;
        });

        builder.setOnDismissListener(dialog -> isPinDialogVisible = false);

        builder.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
        showApprovedApps();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isSwitchOn && !isLockTaskActive) {
            startLockTask();
            isLockTaskActive = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isSwitchOn) {
            disableKioskMode();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isSwitchOn) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK ||
                    event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH ||
                    event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void updateChildModeStatus() {
        LocalPreference.saveChildModeStatus( isSwitchOn ? "on" : "off");
    }

    private void enableKioskMode() {
        if (devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            devicePolicyManager.setLockTaskPackages(compName, new String[]{getPackageName()});
            startLockTask();
            isLockTaskActive = true;
            startModeTrackerService(this, "Activated");
        } else {
            Toast.makeText(this, "App is not set as device owner", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableKioskMode() {
        stopLockTask();
        isLockTaskActive = false;
        startModeTrackerService(this, "Deactivated");
    }

    private void checkPermissions() {
        if (!devicePolicyManager.isAdminActive(compName)) {
            startActivity(new Intent(this, SettingActivity.class));
        } else {
            checkOverlayPermission();
        }
    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(this, SettingActivity.class));
        }
    }

    private void startModeTrackerService(Context context, String message) {
        Intent startIntent = new Intent(context, ModeTrackerService.class);
        startIntent.putExtra("inputExtra", message);
        ContextCompat.startForegroundService(context, startIntent);
    }
}
