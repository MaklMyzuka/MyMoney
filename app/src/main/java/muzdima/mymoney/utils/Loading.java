package muzdima.mymoney.utils;

import android.app.AlertDialog;
import android.content.Context;

import muzdima.mymoney.R;

public class Loading {

    private static AlertDialog dialog;

    public static void show(Context context) {
        if (dialog != null) {
            return;
        }
        dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(R.layout.loading_dialog)
                .show();
    }

    public static void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
