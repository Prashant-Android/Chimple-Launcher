package com.chimple.parentalcontrol.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.model.AppModel;
import com.chimple.parentalcontrol.util.AsyncTaskHelper;
import com.chimple.parentalcontrol.util.VUtil;
import com.chimple.parentalcontrol.view.MainActivity;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class PersistentForegroundService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "PersistentForegroundService";
    private Timer timer;
    private static final long STATUS_BAR_COLLAPSE_DELAY = 10L;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        startTimer();
        startCollapseStatusBarTask();
        return START_STICKY;
    }

    private void startTimer() {

        Set<String> approvedAppPackageNames = new HashSet<>();
        for (AppModel app : VUtil.getApprovedAppsList(getPackageManager(), getApplicationContext())) {
            approvedAppPackageNames.add(app.getPackageName());
        }
        approvedAppPackageNames.add("com.chimple.parentalcontrol");
        approvedAppPackageNames.add("com.coloros.safecenter");


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String currentAppPackageName = getCurrentAppPackageName();


                if (approvedAppPackageNames.contains(currentAppPackageName) || currentAppPackageName.isEmpty()) {
                    Log.d("AppChecker", "Approved Package Name: " + currentAppPackageName);
                } else {
                    AsyncTaskHelper.runOnUiThread(() -> {
                        Intent broadcastIntent = new Intent(getApplicationContext(), LauncherReceiver.class);
                        getApplicationContext().sendBroadcast(broadcastIntent);
                    });

                    Log.d("AppChecker", "Unapproved Package Name: " + currentAppPackageName);
                }
            }
        }, 0, 1000);
    }

    private void startCollapseStatusBarTask() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                collapseNotificationPanel();
                handler.postDelayed(this, STATUS_BAR_COLLAPSE_DELAY);
            }
        }, STATUS_BAR_COLLAPSE_DELAY);
    }

    private void collapseNotificationPanel() {
        try {
            Object statusBarService = getSystemService(Context.STATUS_BAR_SERVICE);
            if (statusBarService != null) {
                Class<?> statusBarManagerClass = Class.forName("android.app.StatusBarManager");
                Method collapsePanelMethod = statusBarManagerClass.getMethod("collapsePanels");
                collapsePanelMethod.invoke(statusBarService);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private String getCurrentAppPackageName() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager != null) {
            long currentTime = System.currentTimeMillis();
            List<UsageStats> appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 10, currentTime);
            if (appList != null && !appList.isEmpty()) {
                SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!sortedMap.isEmpty()) {
                    return Objects.requireNonNull(sortedMap.get(sortedMap.lastKey())).getPackageName();
                }
            }
        }
        return "";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Chimple Launcher").setContentText("Child mode is activated").setSmallIcon(R.drawable.logo).setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Persistent Foreground Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        return builder.build();
    }
}
