package muzdima.mymoney.utils;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.StringRes;

import java.io.PrintWriter;
import java.io.StringWriter;

import muzdima.mymoney.R;

public class ErrorDialog {

    private static String printException(Context context, Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String string = stringWriter.toString();
        Throwable cause = exception.getCause();
        return string + (cause != null ? "\n\n" + context.getString(R.string.cause) + "\n" + printException(context, cause) : "");
    }

    public static void showError(Context context, Exception exception, Runnable callback) {
        showError(context, printException(context, exception), callback);
    }

    public static void showError(Context context, String message, Exception exception, Runnable callback) {
        showError(context, message + "\n\n" + context.getString(R.string.exception) + "\n" + printException(context, exception), callback);
    }

    public static void showError(Context context, @StringRes int message, Exception exception, Runnable callback) {
        showError(context, context.getString(message), exception, callback);
    }

    public static void showError(Context context, String message, Runnable callback) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(context.getString(R.string.error))
                .setMessage(message)
                .setNeutralButton(R.string.dialog_ok, callback == null ? null : (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    callback.run();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void showError(Context context, @StringRes int message, Runnable callback) {
        showError(context, context.getString(message), callback);
    }
}
