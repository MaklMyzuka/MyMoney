package muzdima.mymoney.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ActivitySolver;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.Worker;

public class CategorySelectorCurrentMonth extends CategorySelector {

    public CategorySelectorCurrentMonth(Context context) {
        super(context);
    }

    public CategorySelectorCurrentMonth(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CategorySelectorCurrentMonth(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CategorySelectorCurrentMonth(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(String history_key) {
        long monthStartUTC = DateTime.getCurrentMonthStartUTC();
        long monthEndUTC = DateTime.getCurrentMonthEndUTC();
        super.init(history_key, monthStartUTC, monthEndUTC);
    }

    public void update() {
        long monthStartUTC = DateTime.getCurrentMonthStartUTC();
        long monthEndUTC = DateTime.getCurrentMonthEndUTC();
        super.update(monthStartUTC, monthEndUTC);
    }
}
