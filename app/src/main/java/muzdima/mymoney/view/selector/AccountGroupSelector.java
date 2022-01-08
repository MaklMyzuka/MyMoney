package muzdima.mymoney.view.selector;

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
import muzdima.mymoney.view.HistorySpinner;
import muzdima.mymoney.view.MoneyTextView;

public class AccountGroupSelector extends LinearLayout {
    private final List<SpinnerItem> items = new ArrayList<>();
    private HistorySpinner spinner;
    private MoneyTextView textView;

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
        textView.setText();
    }

    private void updateSum() {
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            Money sum = Repository.getRepository().getAccountGroupSum(spinner.getSelectedId());
            activity.runOnUiThread(() -> textView.setText(sum));
        });
    }

    public void init(String history_key) {
        updateData(() -> {
            spinner.setOnItemSelectedListener(id -> updateSum());
            spinner.init(history_key, items, null);
        });
    }

    private void updateData(Runnable callback) {
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            List<SpinnerItem> spinnerItems = Repository.getRepository().getAccountGroupSpinnerItems();
            activity.runOnUiThread(()->{
                items.clear();
                items.addAll(spinnerItems);
                callback.run();
            });
        });
    }

    public void update() {
        updateData(() -> {
            spinner.update();
            updateSum();
        });
    }
}
