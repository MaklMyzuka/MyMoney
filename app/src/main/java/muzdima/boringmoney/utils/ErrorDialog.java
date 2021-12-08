package muzdima.boringmoney.utils;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.StringRes;

import java.io.PrintWriter;
import java.io.StringWriter;

import muzdima.boringmoney.R;

public class ErrorDialog {

    private static String printException(Context context, Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String string = stringWriter.toString();
        Throwable cause = exception.getCause();
        return string + (cause != null ? "\n\n" + context.getString(R.string.cause) + "\n" + printException(context, cause) : "");
    }

    public static void showError(Context context, Exception exception) {
        showError(context, printException(context, exception));
    }

    public static void showError(Context context, String message, Exception exception) {
        showError(context, message + "\n\n" + context.getString(R.string.exception) + "\n" + printException(context, exception));
    }

    public static void showError(Context context, @StringRes int message, Exception exception) {
        showError(context, context.getString(message), exception);
    }

    public static void showError(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.error))
                .setMessage(message)
                .setNeutralButton(R.string.dialog_ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void showError(Context context, @StringRes int message) {
        showError(context, context.getString(message));
    }
}
