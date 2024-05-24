package com.chimple.parentalcontrol.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.databinding.ClPinDialogBinding;

public class PinVerificationUtil {

    public static void showPinDialog(final Context context, final PinVerificationCallbacks callbacks) {
        final MyDialog dialog = new MyDialog(context, R.layout.cl_pin_dialog);
        View dialogView = dialog.getView();
        final ClPinDialogBinding clPinDialogBinding = ClPinDialogBinding.bind(dialogView);

        clPinDialogBinding.pin.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String enteredPin = v.getText().toString();
                String storedPin = LocalPreference.getPin();

                if (enteredPin.equals(storedPin)) {
                    callbacks.onCorrectPin();
                    dialog.dismiss();
                } else {
                    callbacks.onWrongPin();
                }
                return true;
            }
            return false;
        });

        clPinDialogBinding.closeDialogBtn.setOnClickListener(v -> {
            dialog.dismiss();
            callbacks.onCloseDialog();
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    public interface PinVerificationCallbacks {
        void onCorrectPin();
        void onWrongPin();
        void onCloseDialog();
    }
}
