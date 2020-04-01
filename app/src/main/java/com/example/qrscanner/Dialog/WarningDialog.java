package com.example.qrscanner.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.qrscanner.R;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

public class WarningDialog extends Dialog {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WarningDialog(@NonNull Context context) {
        super(context);
        final PrettyDialog pDialog = new PrettyDialog(context);
        pDialog
                .setIcon(R.drawable.ic_info_outline_black_24dp)
                .setTitle("Warning")
                .setTitleColor(R.color.pdlg_color_red)
                .setMessage("Result is empty!")
                .addButton(
                        "OK",
                        R.color.pdlg_color_white,
                        R.color.pdlg_color_red,
                        new PrettyDialogCallback() {
                            @Override
                            public void onClick() {
                                pDialog.dismiss();
                            }
                        }
                )
                .show();
    }

    public WarningDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected WarningDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}
