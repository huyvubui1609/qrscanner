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

public class ExitDialog extends Dialog {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ExitDialog(@NonNull Context context) {
        super(context);
        final PrettyDialog pDialog = new PrettyDialog(context);
        pDialog
                .setIcon(R.drawable.ic_info_outline_black_24dp)
                .setTitle("Warning")
                .setTitleColor(R.color.pdlg_color_red)
                .setMessage("Are you sure want to exit?")
                .addButton(
                        "OK",
                        R.color.pdlg_color_white,
                        R.color.pdlg_color_green,
                        new PrettyDialogCallback() {
                            @Override
                            public void onClick() {
                                System.exit(0);
                            }
                        }
                )
                .addButton(
                        "Cancel",
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

    public ExitDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ExitDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


}
