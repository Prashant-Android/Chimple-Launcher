package com.chimple.parentalcontrol.services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.adapter.ApprovedAppAdapter;
import com.chimple.parentalcontrol.model.AppModel;
import com.chimple.parentalcontrol.util.ChildAlert;
import com.chimple.parentalcontrol.util.LocalPreference;
import com.chimple.parentalcontrol.util.VUtil;
import com.chimple.parentalcontrol.view.MainActivity;

import java.util.List;


public class ModeTrackerService extends Service {
    private final String channelId = "Chimple Service";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        openOnlyApprovedApp(intent);

        String status = intent != null ? intent.getStringExtra("inputExtra") : null;
        ChildAlert childAlert = new ChildAlert(getApplicationContext());

        if ("on".equals(LocalPreference.getChildModeStatus()) && "Deactivated".equals(status)) {
            childAlert.show(R.layout.cl_child_alert);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Chimple Child Mode Activated")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .build();
        int notificationId = 1;
        startForeground(notificationId, notification);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    channelId,
                    "Chimple Mode",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void openOnlyApprovedApp() {
        if (LocalPreference.getChildModeStatus().equals("on")){
            List<AppModel> approvedAppsList = VUtil.getApprovedAppsList(getPackageManager(), getApplicationContext());

        }

    }

    private void openOnlyApprovedApp(Intent intent) {
        if (LocalPreference.getChildModeStatus().equals("on")) {
            List<AppModel> approvedAppsList = VUtil.getApprovedAppsList(getPackageManager(), getApplicationContext());
            if (approvedAppsList != null) {
                // Get the package name of the app being launched
                String packageName = intent.getStringExtra("packageName");
                boolean isApproved = false;
                for (AppModel app : approvedAppsList) {
                    if (app.getPackageName().equals(packageName)) {
                        isApproved = true;
                        break;
                    }
                }
                if (!isApproved) {
                    Toast.makeText(getApplicationContext(), "Cannot open unapproved app", Toast.LENGTH_SHORT).show();
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                }
            }
        }
    }
}

