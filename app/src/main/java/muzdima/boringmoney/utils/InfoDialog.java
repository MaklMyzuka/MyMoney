package muzdima.boringmoney.utils;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.StringRes;

import muzdima.boringmoney.R;

public class InfoDialog {

    public static void show(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialog_ok, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public static void show(Context context, @StringRes int title, String message) {
        show(context, context.getString(title), message);
    }

    public static void show(Context context, String title, @StringRes int message) {
        show(context, title, context.getString(message));
    }

    public static void show(Context context, @StringRes int title, @StringRes int message) {
        show(context, context.getString(title), context.getString(message));
    }
}
