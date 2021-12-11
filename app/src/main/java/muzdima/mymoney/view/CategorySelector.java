package muzdima.mymoney.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ActivitySolver;
import muzdima.mymoney.utils.Worker;

public class CategorySelector extends LinearLayout {
    private final List<SpinnerItem> items = new ArrayList<>();
    private HistorySpinner spinner;
    private MoneyTextView textView;
    private long fromUTC;
    private long toUTC;

    public CategorySelector(Context context) {
        super(context);
        init();
    }

    public CategorySelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CategorySelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CategorySelector(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.category_selector, this);
        spinner = findViewById(R.id.categorySpinner);
        textView = findViewById(R.id.textViewCategorySum);
        textView.setText();
    }

    private void updateSum() {
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            Money sum = Repository.getRepository().getCategorySum(spinner.getSelectedId(), fromUTC, toUTC);
            activity.runOnUiThread(() -> textView.setText(sum));
        });
    }

    public void init(String history_key, long fromUTC, long toUTC) {
        this.fromUTC = fromUTC;
        this.toUTC = toUTC;
        updateData(() -> {
            spinner.setOnItemSelectedListener(id -> updateSum());
            spinner.init(history_key, items, null);
        });
    }

    private void updateData(Runnable callback) {

        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            List<SpinnerItem> spinnerItems = Repository.getRepository().getCategorySpinnerItems();
            activity.runOnUiThread(()->{
                items.clear();
                items.addAll(spinnerItems);
                callback.run();
            });
        });
    }

    public void update(long fromUTC, long toUTC) {
        this.fromUTC = fromUTC;
        this.toUTC = toUTC;
        updateData(() -> {
            spinner.update();
            updateSum();
        });
    }
}
