package muzdima.boringmoney.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import muzdima.boringmoney.R;
import muzdima.boringmoney.activity.DraftActivity;
import muzdima.boringmoney.repository.Repository;
import muzdima.boringmoney.repository.model.SpinnerItem;
import muzdima.boringmoney.utils.ActivitySolver;
import muzdima.boringmoney.utils.Worker;

public class AccountSelector extends LinearLayout {

    private final List<SpinnerItem> items = new ArrayList<>();
    private HistorySpinner spinner;
    private TextView textView;
    private long account_id;

    public AccountSelector(Context context) {
        super(context);
        init();
    }

    public AccountSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AccountSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AccountSelector(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.account_selector, this);
        spinner = findViewById(R.id.accountSpinner);
        textView = findViewById(R.id.textViewAccountSum);
        textView.setText("");
        findViewById(R.id.buttonAccountAction).setOnClickListener(view -> {
            Context context = getContext();
            new AlertDialog.Builder(context)
                    .setTitle(R.string.account_action)
                    .setItems(new String[]{
                            context.getString(R.string.account_action_expense),
                            context.getString(R.string.account_action_income),
                            context.getString(R.string.account_action_transfer_from),
                            context.getString(R.string.account_action_transfer_to),
                    }, (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        Intent intent = new Intent(context, DraftActivity.class);
                        intent.putExtra("action", i + 1);
                        intent.putExtra("account_id", account_id);
                        context.startActivity(intent);
                    })
                    .show();
        });
    }

    private void updateSum() {
        Activity activity = ActivitySolver.getActivity(getContext());
        account_id = spinner.getSelectedId();
        Worker.run(activity, () -> {
            String sumLabel = Repository.getRepository().getAccountSum(account_id).toString();
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
            items.addAll(Repository.getRepository().getAccountSpinnerItems());
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
