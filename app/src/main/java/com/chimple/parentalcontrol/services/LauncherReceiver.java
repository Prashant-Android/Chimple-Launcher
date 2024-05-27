package com.chimple.parentalcontrol.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chimple.parentalcontrol.util.LocalPreference;
import com.chimple.parentalcontrol.view.MainActivity;

public class LauncherReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            if (LocalPreference.getChildModeStatus().equals("on")){
                startMainActivity(context);
            }

        }

    }

    private void startMainActivity(Context context) {
        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
    }
}
