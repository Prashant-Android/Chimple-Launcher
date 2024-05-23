package com.chimple.parentalcontrol.core;

import android.app.Application;

import com.chimple.parentalcontrol.util.LocalPreference;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocalPreference.init(this);
    }
}
