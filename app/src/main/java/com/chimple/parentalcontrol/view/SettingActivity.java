package com.chimple.parentalcontrol.view;

import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.chimple.parentalcontrol.core.MyDeviceAdminReceiver;
import com.chimple.parentalcontrol.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding binding;
    private ComponentName compName;

    private final ActivityResultLauncher<Intent> requestAdminPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
            binding.adminAccessSwitch.setChecked(true);
        } else {
            binding.adminAccessSwitch.setChecked(false);
            Toast.makeText(this, "Admin access permission denied", Toast.LENGTH_SHORT).show();
        }
        updateButtonState();
    });

    private final ActivityResultLauncher<Intent> requestAlertWindowPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            binding.alertWindowSwitch.setChecked(true);
        } else {
            binding.alertWindowSwitch.setChecked(false);
            Toast.makeText(this, "Alert window permission denied", Toast.LENGTH_SHORT).show();
        }
        updateButtonState();
    });

    private final ActivityResultLauncher<Intent> requestAppUsagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkAppUsagePermission()) {
            binding.appUsageSwitch.setChecked(true);
        } else {
            binding.appUsageSwitch.setChecked(false);
            Toast.makeText(this, "App usage access permission denied", Toast.LENGTH_SHORT).show();
        }
        updateButtonState();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyDeviceAdminReceiver.class);

        binding.adminAccessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You need to enable device admin to use Child Mode.");
                requestAdminPermissionLauncher.launch(intent);
            } else {
                updateButtonState();
            }
        });

        binding.alertWindowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                requestAlertWindowPermissionLauncher.launch(intent);
            } else {
                updateButtonState();
            }
        });

        binding.appUsageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                requestAppUsagePermissionLauncher.launch(intent);
            } else {
                updateButtonState();
            }
        });

        updateButtonState();

        binding.nextBtn.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
    }

    private void checkAndSetAppUsageSwitchState() {
        boolean isPermissionGranted = checkAppUsagePermission();
        binding.appUsageSwitch.setChecked(isPermissionGranted);
    }

    private boolean checkAppUsagePermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void updateButtonState() {
        boolean isBothSwitchesOn = binding.adminAccessSwitch.isChecked() && binding.alertWindowSwitch.isChecked() && binding.appUsageSwitch.isChecked();
        binding.nextBtn.setEnabled(isBothSwitchesOn);
    }


}
