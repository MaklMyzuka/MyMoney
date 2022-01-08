package muzdima.mymoney.view.selector;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.view.selector.CategorySelector;

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
        DateTime now = DateTime.convertUTCToLocal(DateTime.getNowUTC());
        super.init(history_key, DateTime.getMonthStartUTCFromLocal(now.year, now.month), DateTime.getMonthEndUTCFromLocal(now.year, now.month));
    }

    public void update() {
        DateTime now = DateTime.convertUTCToLocal(DateTime.getNowUTC());
        super.update(DateTime.getMonthStartUTCFromLocal(now.year, now.month), DateTime.getMonthEndUTCFromLocal(now.year, now.month));
    }
}
