package com.chimple.parentalcontrol.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.chimple.parentalcontrol.model.AppModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.dmoral.toasty.Toasty;

public class VUtil {

    public static List<AppModel> loadAppList(PackageManager pm) {
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = pm.queryIntentActivities(main, 0);

        List<AppModel> appList = new ArrayList<>();
        Set<String> packageNames = new HashSet<>();

        for (ResolveInfo resolveInfo : packages) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (!packageNames.contains(packageName)) {
                String appName = resolveInfo.loadLabel(pm).toString();
                Drawable appIcon = resolveInfo.loadIcon(pm);
                AppModel app = new AppModel(appName, packageName, appIcon);
                appList.add(app);
                packageNames.add(packageName);
            }
        }

        appList.sort((app1, app2) -> app1.getAppName().compareToIgnoreCase(app2.getAppName()));

        return appList;
    }

    public static List<AppModel> getApprovedAppsList(PackageManager pm, Context context) {
        List<AppModel> approvedAppsList = new ArrayList<>();
        Set<String> approvedAppPackageNames = LocalPreference.getApprovedApps(context);

        for (String packageName : approvedAppPackageNames) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                String appName = pm.getApplicationLabel(appInfo).toString();
                Drawable appIcon = pm.getApplicationIcon(appInfo);
                AppModel app = new AppModel(appName, packageName, appIcon);
                approvedAppsList.add(app);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        return approvedAppsList;
    }
    public static void showSuccessToast(Context context, String message) {
        Toasty.success(context, message).show();
    }

    public static void showErrorToast(Context context, String message) {
        Toasty.error(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showWarning(Context context, String message) {
        Toasty.warning(context, message, Toast.LENGTH_LONG).show();
    }

    public static boolean isSplitScreenModeActive(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                Rect rect = new Rect();
                display.getRectSize(rect);
                return rect.width() < display.getWidth() || rect.height() < display.getHeight();
            }
        }
        return false;
    }
}
