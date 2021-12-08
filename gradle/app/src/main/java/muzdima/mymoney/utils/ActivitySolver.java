package muzdima.mymoney.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

public class ActivitySolver {
    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof ContextWrapper) {
            if (context instanceof android.app.Activity) {
                return (android.app.Activity) context;
            } else {
                return getActivity(((ContextWrapper) context).getBaseContext());
            }
        }
        return null;
    }
}
