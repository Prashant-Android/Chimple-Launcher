package com.chimple.parentalcontrol.util;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.databinding.ClPinDialogBinding;

public class PinVerificationUtil {

    public static void showPinDialog(final Context context, final PinVerificationCallbacks callbacks) {
        final MyDialog dialog = new MyDialog(context, R.layout.cl_pin_dialog);
        View dialogView = dialog.getView();
        final ClPinDialogBinding clPinDialogBinding = ClPinDialogBinding.bind(dialogView);

        clPinDialogBinding.pin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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
            }
        });

        clPinDialogBinding.closeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Dismiss the dialog if close button is clicked
                callbacks.onCloseDialog();
            }
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
