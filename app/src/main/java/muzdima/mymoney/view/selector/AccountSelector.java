package muzdima.mymoney.view.selector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.activity.DraftActivity;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ActivitySolver;
import muzdima.mymoney.utils.InfoDialog;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.HistorySpinner;
import muzdima.mymoney.view.MoneyTextView;

public class AccountSelector extends LinearLayout {

    private final List<SpinnerItem> items = new ArrayList<>();
    private HistorySpinner spinner;
    private MoneyTextView textView;
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
        textView.setText();

        findViewById(R.id.buttonAccountAction).setOnClickListener(view -> {
            Context context = getContext();
            if (account_id == -1) {
                InfoDialog.show(context, R.string.error, R.string.account_not_selected_message, null);
                return;
            }
            final DialogItemWithIcon[] dialogItems = {
                    new DialogItemWithIcon(context.getString(R.string.account_action_expense), R.drawable.ic_account_action_expense),
                    new DialogItemWithIcon(context.getString(R.string.account_action_income), R.drawable.ic_account_action_income),
                    new DialogItemWithIcon(context.getString(R.string.account_action_transfer_from), R.drawable.ic_account_action_transfer_from),
                    new DialogItemWithIcon(context.getString(R.string.account_action_transfer_to), R.drawable.ic_account_action_transfer_to),
            };
            ListAdapter adapter = new ArrayAdapter<DialogItemWithIcon>(
                    context,
                    android.R.layout.select_dialog_item,
                    android.R.id.text1,
                    dialogItems) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view.findViewById(android.R.id.text1);
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(dialogItems[position].icon, 0, 0, 0);
                    textView.setCompoundDrawablePadding((int) (5 * getResources().getDisplayMetrics().density + 0.5f));
                    return view;
                }
            };
            new AlertDialog.Builder(context)
                    .setTitle(R.string.account_action)
                    .setAdapter(adapter, (dialogInterface, i) -> {
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
            Money.MoneyItem sum = Repository.getRepository().getAccountSum(account_id);
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
            List<SpinnerItem> spinnerItems = Repository.getRepository().getAccountSpinnerItems();
            activity.runOnUiThread(() -> {
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

    private static class DialogItemWithIcon {
        public final String text;
        public final int icon;

        public DialogItemWithIcon(String text, @DrawableRes Integer icon) {
            this.text = text;
            this.icon = icon;
        }

        @NonNull
        @Override
        public String toString() {
            return text;
        }
    }
}
