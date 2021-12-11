package muzdima.mymoney.utils;

import android.app.Activity;
import android.content.Context;

public class Worker {

    private static int running = 0;

    public static void run(Activity activity, Runnable runnable) {
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
        run(activity, runnable);
    }
}
