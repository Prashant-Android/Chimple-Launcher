package com.chimple.parentalcontrol.util;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.chimple.parentalcontrol.view.MainActivity;

public class ChildAlert {
    private final Context context;
    private boolean isShowing = false;
    private View floatingView;

    public ChildAlert(Context context) {
        this.context = context;
    }

    public void show(int layoutResId) {
        if (!isShowing) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN | // Set full-screen flag
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, // Flag to cover the status bar
                    PixelFormat.TRANSLUCENT
            );
            layoutParams.gravity = Gravity.TOP | Gravity.START;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            floatingView = inflater.inflate(layoutResId, null);

            windowManager.addView(floatingView, layoutParams);
            isShowing = true;

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);



            new Handler(Looper.getMainLooper()).postDelayed(this::gone, 4500);
        } else {
            Toast.makeText(context, "Floating view is already showing", Toast.LENGTH_SHORT).show();
        }
    }

    private void gone() {
        if (isShowing) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (floatingView != null) {
                windowManager.removeView(floatingView);
                isShowing = false;
                floatingView = null;
            }
        } else {
            Toast.makeText(context, "Floating view is not currently showing", Toast.LENGTH_SHORT).show();
        }
    }
}
