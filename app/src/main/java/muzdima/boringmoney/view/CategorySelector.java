package muzdima.boringmoney.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import muzdima.boringmoney.R;
import muzdima.boringmoney.repository.Repository;
import muzdima.boringmoney.repository.model.SpinnerItem;
import muzdima.boringmoney.utils.ActivitySolver;
import muzdima.boringmoney.utils.Worker;

public class CategorySelector extends LinearLayout {
    private final List<SpinnerItem> items = new ArrayList<>();
    private HistorySpinner spinner;
    private TextView textView;

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
        textView.setText("");
    }

    private void updateSum() {
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            String sumLabel = Repository.getRepository().getCategorySum(spinner.getSelectedId()).toString();
            activity.runOnUiThread(() -> textView.setText(sumLabel));
        });
    }

    public void init(String history_key) {
        updateData(() -> {
            spinner.setOnItemSelectedListener(id -> updateSum());
            spinner.init(history_key, items, null);
        });
    }

    private void updateData(Runnable callback) {
        items.clear();
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            items.addAll(Repository.getRepository().getCategorySpinnerItems());
            activity.runOnUiThread(callback);
        });
    }

    public void update() {
        updateData(() -> {
            spinner.update();
            updateSum();
        });
    }
}
