package muzdima.mymoney.utils;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.StringRes;

import muzdima.mymoney.R;

public class ConfirmDialog {
    public static void show(Context context, String title, String message, Runnable runnable) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_yes, (dialogInterface, i) -> runnable.run())
                .setNegativeButton(R.string.dialog_no, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public static void show(Context context, @StringRes int title, String message, Runnable runnable) {
        show(context, context.getString(title), message, runnable);
    }

    public static void show(Context context, String title, @StringRes int message, Runnable runnable) {
        show(context, title, context.getString(message), runnable);
    }

    public static void show(Context context, @StringRes int title, @StringRes int message, Runnable runnable) {
        show(context, context.getString(title), context.getString(message), runnable);
    }
}
