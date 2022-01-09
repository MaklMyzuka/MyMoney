package muzdima.mymoney.utils;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.StringRes;

import muzdima.mymoney.R;

public class InfoDialog {

    public static void show(Context context, String title, String message, Runnable callback) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialog_ok, callback == null ? null : (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    callback.run();
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public static void show(Context context, @StringRes int title, String message, Runnable callback) {
        show(context, context.getString(title), message, callback);
    }

    public static void show(Context context, String title, @StringRes int message, Runnable callback) {
        show(context, title, context.getString(message), callback);
    }

    public static void show(Context context, @StringRes int title, @StringRes int message, Runnable callback) {
        show(context, context.getString(title), context.getString(message), callback);
    }
}
