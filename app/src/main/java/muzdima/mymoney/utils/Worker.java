package muzdima.mymoney.utils;

import android.app.Activity;
import android.content.Context;

import muzdima.mymoney.activity.BaseActivity;

public class Worker {

    private static int running = 0;

    public static void run(BaseActivity activity, Runnable runnable) {
        if (activity.isRecreating()) return;
        running++;
        Loading.show(activity);
        new Thread(() -> {
            try {
                runnable.run();
            } catch (Exception exception) {
                activity.runOnUiThread(() -> ErrorDialog.showError(activity, exception, null));
            } finally {
                activity.runOnUiThread(() -> {
                    running--;
                    if (running == 0) {
                        Loading.dismiss();
                    }
                });
            }
        }).start();
    }

    public static void run(Context context, Runnable runnable) {
        Activity activity = ActivitySolver.getActivity(context);
        run((BaseActivity) activity, runnable);
    }
}
