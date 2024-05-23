package com.chimple.parentalcontrol.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public  class LocalPreference {
    private static final String USER_DETAILS = "ChimpleMode";
    private static final String APPROVED_APPS = "approved_apps";
    private static SharedPreferences sharedPrefs;

    public static void init(Application context) {
        sharedPrefs = context.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
    }

    public static void savePin(String pin) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("pin", pin);
        editor.apply();
    }

    public static void saveChildModeStatus(String status) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("status", status);
        editor.apply();
    }

    public static String getPin() {
        return sharedPrefs.getString("pin", "");
    }

    public static String getChildModeStatus() {
        return sharedPrefs.getString("status", "");
    }

    public static Set<String> getApprovedApps(Context context) {
        return sharedPrefs.getStringSet(APPROVED_APPS, new HashSet<>());
    }

    public static void addAppToApprovedList(Context context, String packageName) {
        Set<String> approvedApps = getApprovedApps(context);
        approvedApps.add(packageName);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(APPROVED_APPS, approvedApps);
        editor.apply();
    }

    public static void removeAppFromApprovedList(Context context, String packageName) {
        Set<String> approvedApps = getApprovedApps(context);
        approvedApps.remove(packageName);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(APPROVED_APPS, approvedApps);
        editor.apply();
    }

    public static boolean isAppApproved(Context context, String packageName) {
        Set<String> approvedApps = getApprovedApps(context);
        return approvedApps.contains(packageName);
    }
}
