package com.chimple.parentalcontrol.view;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;

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

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SwitchCompat childModeSwitch;
    private boolean isSwitchOn = false;
    private final boolean isPinDialogVisible = false;
    private boolean previousSwitchState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        childModeSwitch = binding.childModeBtn;
        childModeSwitch.setOnClickListener(null);

        childModeSwitch.setOnClickListener(v -> {
            if (!isPinDialogVisible) {
                previousSwitchState = isSwitchOn;
                PinVerificationUtil.showPinDialog(MainActivity.this, new PinVerificationUtil.PinVerificationCallbacks() {
                    @Override
                    public void onCorrectPin() {
                        VUtil.showSuccessToast(MainActivity.this, "Mode Changed");
                        isSwitchOn = !isSwitchOn;
                        childModeSwitch.setChecked(isSwitchOn);
                        updateChildModeStatus();
                        if (isSwitchOn) {
                            startAppChecker();
                        } else {
                            closeAppChecker();
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

        isSwitchOn = LocalPreference.getChildModeStatus().equals("on");
        childModeSwitch.setChecked(isSwitchOn);


        KeyboardVisibilityEvent.setEventListener(MainActivity.this, isOpen -> {
            if (isOpen){
                binding.bottomNavigationView.setVisibility(View.GONE);
            }else{
                binding.bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_app) {
                navigateActivity(AppListActivity.class);
                return true;
            } else if (menuItem.getItemId() == R.id.menu_setting) {
                navigateActivity(SettingActivity.class);
                return true;
            }
            return false;
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

            }
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


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isSwitchOn) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH || event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void updateChildModeStatus() {
        LocalPreference.saveChildModeStatus(isSwitchOn ? "on" : "off");
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


    }




}
