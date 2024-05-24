package com.chimple.parentalcontrol.view;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.databinding.ActivitySettingBinding;
import com.chimple.parentalcontrol.databinding.ClPinLayoutBinding;
import com.chimple.parentalcontrol.firebase.Constant;
import com.chimple.parentalcontrol.util.LocalPreference;
import com.chimple.parentalcontrol.util.MyDialog;
import com.chimple.parentalcontrol.util.VUtil;

import java.util.List;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding binding;
    private boolean isAlertWindowPermissionGranted = false;
    private boolean isAppUsagePermissionGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateDrawOverAppsSwitch();

        updateUsageAccessSwitch();
        setSwitchListeners();


        if (Constant.mAuth.getCurrentUser() == null) {
            binding.changePasswordBtn.setVisibility(View.GONE);
        } else {
            binding.changePasswordBtn.setVisibility(View.VISIBLE);

        }

        binding.nextBtn.setOnClickListener(v -> {
            if (isAlertWindowPermissionGranted && isAppUsagePermissionGranted) {
                showPinDialog();
            }
        });

        binding.changePinCodeBtn.setOnClickListener(v -> showPinDialog());
    }

    private void showPinDialog() {
        final MyDialog dialog = new MyDialog(SettingActivity.this, R.layout.cl_pin_layout);
        View dialogView = dialog.getView();
        final ClPinLayoutBinding pinLayoutBinding = ClPinLayoutBinding.bind(dialogView);

        pinLayoutBinding.btnSetPin.setOnClickListener(v -> {
            if (Objects.requireNonNull(pinLayoutBinding.userPin.getText()).toString().isEmpty()) {
                pinLayoutBinding.userPin.setError("Please enter pin");
            } else if (!pinLayoutBinding.userPin.getText().toString().equals(pinLayoutBinding.userConfirmPin.getText().toString())) {
                VUtil.showWarning(SettingActivity.this, "Pin does not match");
            } else {
                dialog.dismiss();
                LocalPreference.savePin(pinLayoutBinding.userPin.getText().toString());
                startActivity(new Intent(SettingActivity.this, MainActivity.class));
            }
        });

        dialog.setCancelable(true);
        dialog.show();
    }

    private void updateDrawOverAppsSwitch() {
        boolean isPermissionGranted = Settings.canDrawOverlays(this);
        isAlertWindowPermissionGranted = isPermissionGranted;
        binding.alertWindowSwitch.setChecked(isPermissionGranted);
    }

    private void setSwitchListeners() {
        binding.alertWindowSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                VUtil.showWarning(this, "Alert window permission must be enabled");
            } else {
                openDrawOverAppsPermissionScreen();
            }
        });

        binding.appUsageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                VUtil.showWarning(this, "Usage access permission must be enabled");
            } else {
                openUsageAccessPermissionScreen();
            }
        });
    }

    private void openDrawOverAppsPermissionScreen() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void openUsageAccessPermissionScreen() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private void updateUsageAccessSwitch() {
        boolean isPermissionGranted = appUsagePermission();
        isAppUsagePermissionGranted = isPermissionGranted;
        binding.appUsageSwitch.setChecked(isPermissionGranted);
    }

    public boolean appUsagePermission() {
        long tme = System.currentTimeMillis();
        UsageStatsManager usm = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> al = usm.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, tme - (1000 * 1000), tme);
        return !al.isEmpty();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!LocalPreference.getPin().isEmpty()) {
            binding.nextBtn.setVisibility(View.GONE);
        }
        binding.nextBtn.setEnabled(isAlertWindowPermissionGranted && isAppUsagePermissionGranted);
    }
}
