package com.chimple.parentalcontrol.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chimple.parentalcontrol.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;

public class MyDialog {
    private final WeakReference<Context> contextRef;
    private final View dialogView;
    private final AlertDialog alertDialog;
    private boolean isDialogShowing = false; // Flag to track dialog visibility

    public MyDialog(Context appContext, @LayoutRes int layoutResId) {
        this.contextRef = new WeakReference<>(appContext);
        this.dialogView = LayoutInflater.from(appContext).inflate(layoutResId, null);
        this.alertDialog = new MaterialAlertDialogBuilder(appContext, R.style.MaterialAlertDialog_Rounded)
                .setView(dialogView)
                .create();
    }

    public View getView() {
        return dialogView;
    }

    public void setCancelable(boolean isCancelable) {
        alertDialog.setCancelable(isCancelable);
    }

    public void show() {
        if (!isDialogShowing) { // Check if dialog is not already showing
            isDialogShowing = true; // Set flag to true when showing dialog
            Context context = contextRef.get();
            if (context instanceof AppCompatActivity && !((AppCompatActivity) context).isFinishing()) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(600);
                dialogView.startAnimation(alphaAnimation);

                alertDialog.setOnDismissListener(dialog -> isDialogShowing = false); // Reset flag on dismiss

                alertDialog.show();

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                if (alertDialog.getWindow() != null) {
                    alertDialog.getWindow().setLayout(layoutParams.width, layoutParams.height);
                }
            }
        }
    }

    public void dismiss() {
        alertDialog.dismiss();
    }
}
