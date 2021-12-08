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
import muzdima.mymoney.utils.Worker;

public class AccountGroupSelector extends LinearLayout {
    private final List<SpinnerItem> items = new ArrayList<>();
    private HistorySpinner spinner;
    private TextView textView;

    public AccountGroupSelector(Context context) {
        super(context);
        init();
    }

    public AccountGroupSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AccountGroupSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AccountGroupSelector(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.account_group_selector, this);
        spinner = findViewById(R.id.accountGroupSpinner);
        textView = findViewById(R.id.textViewAccountGroupSum);
        textView.setText("");
    }

    private void updateSum() {
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            String sumLabel = Repository.getRepository().getAccountGroupSum(spinner.getSelectedId()).toString();
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
            items.addAll(Repository.getRepository().getAccountGroupSpinnerItems());
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
