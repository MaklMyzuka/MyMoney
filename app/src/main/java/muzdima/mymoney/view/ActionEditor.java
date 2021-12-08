package muzdima.mymoney.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.AccountInfo;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.repository.model.TransactionItem;
import muzdima.mymoney.repository.model.TransferItem;
import muzdima.mymoney.utils.ActivitySolver;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.ErrorDialog;
import muzdima.mymoney.utils.Worker;

public class ActionEditor extends LinearLayout {

    public ActionEditor(Context context) {
        super(context);
    }

    public ActionEditor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionEditor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ActionEditor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // DON'T MODIFY item
    private void updateTransactionItem(TransactionItem item, Runnable update) {
        inflate(getContext(), R.layout.action_editor_transaction, this);
        Activity activity = ActivitySolver.getActivity(getContext());
        boolean isIncome = item.sum.sum10000 > 0;
        setBackgroundColor(ContextCompat.getColor(getContext(), isIncome ? R.color.transaction_income : R.color.transaction_expense));

        ((TextView) findViewById(R.id.textViewTitleTransactionEditor)).setText(isIncome ? R.string.action_income : R.string.action_expense);
        TextView date = findViewById(R.id.textViewDateTransactionEditor);
        date.setText(DateTime.printUTCToLocalDate(item.createdAtUTC));
        date.setOnClickListener(view ->
                new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) ->
                        date.setText(DateTime.printLocalDate(year, month, dayOfMonth)),
                        DateTime.getLocalYear(item.createdAtUTC),
                        DateTime.getLocalMonth(item.createdAtUTC),
                        DateTime.getLocalDayOfMonth(item.createdAtUTC))
                        .show()
        );
        TextView time = findViewById(R.id.textViewTimeTransactionEditor);
        time.setText(DateTime.printUTCToLocalTime(item.createdAtUTC));
        time.setOnClickListener(view ->
                new TimePickerDialog(getContext(), (datePicker, hourOfDay, minute) ->
                        time.setText(DateTime.printLocalTime(hourOfDay, minute)),
                        DateTime.getLocalHourOfDay(item.createdAtUTC),
                        DateTime.getLocalMinute(item.createdAtUTC),
                        true)
                        .show()
        );
        TextView currency = findViewById(R.id.textViewCurrencyTransactionEditor);
        currency.setText(item.sum.currencySymbol);
        HistorySpinner account = findViewById(R.id.accountTransactionEditor);
        Worker.run(activity, () -> {
            List<SpinnerItem> accounts = Repository.getRepository().getAccountSpinnerItems();
            activity.runOnUiThread(() -> {
                account.setOnItemSelectedListener(id ->
                    Worker.run(activity, () -> {
                        String currencySymbol = Repository.getRepository().getAccountInfo(id).currencySymbol;
                        activity.runOnUiThread(() -> currency.setText(currencySymbol));
                    })
                );
                account.init(String.format("history_account_selector_on_edit_transaction_on_%s", isIncome ? "income" : "expense"), accounts, item.accountId);
            });
        });
        EditText sum = findViewById(R.id.editSumTransactionEditor);
        if (item.id != -1) {
            sum.setText(String.valueOf(Math.abs(item.sum.sum10000) / 10000.0d));
        }
        EditText product = findViewById(R.id.editProductTransactionEditor);
        product.setText(item.product);
        HistorySpinner category = findViewById(R.id.categoryTransactionEditor);
        Worker.run(activity, () -> {
            List<SpinnerItem> categories = Repository.getRepository().getCategorySpinnerItems();
            activity.runOnUiThread(() ->
                category.init(String.format("history_category_selector_on_edit_transaction_on_%s", isIncome ? "income" : "expense"), categories, item.categoryId)
            );
        });
        Button buttonAccept = findViewById(R.id.buttonAcceptTransactionEditor);
        buttonAccept.setText(item.id == -1 ? R.string.add_button_label : R.string.edit_button_label);
        buttonAccept.setOnClickListener(view -> {
            long categoryId = category.getSelectedId();
            long accountId = account.getSelectedId();
            long sum10000;
            try {
                sum10000 = Math.round(Double.parseDouble(sum.getText().toString()) * 10000.0d);
            } catch (NumberFormatException exception) {
                ErrorDialog.showError(activity, R.string.error_sum_parse);
                return;
            }
            if (sum10000 < 0) {
                ErrorDialog.showError(activity, R.string.error_sum_negative);
                return;
            }
            long sum1000Final = isIncome ? sum10000 : -sum10000;
            String productValue = product.getText().toString();
            long createdAtUTC = DateTime.parseUTCFromLocal(date.getText().toString(), time.getText().toString());
            Worker.run(activity, () -> {
                if (item.id == -1) {
                    Repository.getRepository().insertTransaction(categoryId, accountId, sum1000Final, productValue, createdAtUTC);
                    TransactionItem transaction = new TransactionItem();
                    transaction.id = -1;
                    transaction.categoryId = categoryId;
                    transaction.accountId = accountId;
                    transaction.sum = new Money.MoneyItem();
                    transaction.sum.sum10000 = isIncome ? 1 : -1;
                    transaction.product = productValue;
                    transaction.createdAtUTC = createdAtUTC;
                    activity.runOnUiThread(() -> updateItem(transaction, update));
                } else {
                    Repository.getRepository().updateTransaction(item.id, categoryId, accountId, sum1000Final, productValue, createdAtUTC);
                    activity.runOnUiThread(() -> updateItem(null, update));
                }
                activity.runOnUiThread(update);
            });
        });
        findViewById(R.id.buttonCancelTransactionEditor).setOnClickListener(view -> updateItem(null, update));
    }

    // DON'T MODIFY item
    private void updateTransferItem(TransferItem item, Runnable update) {
        inflate(getContext(), R.layout.action_editor_transfer, this);
        Activity activity = ActivitySolver.getActivity(getContext());
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transfer));

        ((TextView) findViewById(R.id.textViewTitleTransferEditor)).setText(R.string.action_transfer);
        TextView date = findViewById(R.id.textViewDateTransferEditor);
        date.setText(DateTime.printUTCToLocalDate(item.createdAtUTC));
        date.setOnClickListener(view ->
                new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) ->
                        date.setText(DateTime.printLocalDate(year, month, dayOfMonth)),
                        DateTime.getLocalYear(item.createdAtUTC),
                        DateTime.getLocalMonth(item.createdAtUTC),
                        DateTime.getLocalDayOfMonth(item.createdAtUTC))
                        .show()
        );
        TextView time = findViewById(R.id.textViewTimeTransferEditor);
        time.setText(DateTime.printUTCToLocalTime(item.createdAtUTC));
        time.setOnClickListener(view ->
                new TimePickerDialog(getContext(), (datePicker, hourOfDay, minute) ->
                        time.setText(DateTime.printLocalTime(hourOfDay, minute)),
                        DateTime.getLocalHourOfDay(item.createdAtUTC),
                        DateTime.getLocalMinute(item.createdAtUTC),
                        true)
                        .show()
        );
        TextView currencyFrom = findViewById(R.id.textViewCurrencyTransferEditorFrom);
        currencyFrom.setText(item.sumFrom.currencySymbol);
        TextView currencyTo = findViewById(R.id.textViewCurrencyTransferEditorTo);
        currencyTo.setText(item.sumTo.currencySymbol);
        EditText sumFrom = findViewById(R.id.editSumTransferEditorFrom);
        if (item.id != -1) {
            sumFrom.setText(String.valueOf(item.sumFrom.sum10000 / 10000.0d));
        }
        EditText sumTo = findViewById(R.id.editSumTransferEditorTo);
        if (item.id != -1) {
            sumTo.setText(String.valueOf(item.sumTo.sum10000 / 10000.0d));
        }
        HistorySpinner accountFrom = findViewById(R.id.accountTransferEditorFrom);
        HistorySpinner accountTo = findViewById(R.id.accountTransferEditorTo);
        Worker.run(activity, () -> {
            List<SpinnerItem> accounts = Repository.getRepository().getAccountSpinnerItems();
            Runnable checkCurrency = () -> {
                long accountIdFrom = accountFrom.getSelectedId();
                long accountIdTo = accountTo.getSelectedId();
                Worker.run(activity, () -> {
                    AccountInfo accountInfoFrom = Repository.getRepository().getAccountInfo(accountIdFrom);
                    AccountInfo accountInfoTo = Repository.getRepository().getAccountInfo(accountIdTo);
                    activity.runOnUiThread(() -> {
                        currencyFrom.setText(accountInfoFrom.currencySymbol);
                        currencyTo.setText(accountInfoTo.currencySymbol);
                        if (accountInfoFrom.currencyId == accountInfoTo.currencyId) {
                            currencyTo.setVisibility(GONE);
                            sumTo.setVisibility(GONE);
                        } else {
                            currencyTo.setVisibility(VISIBLE);
                            sumTo.setVisibility(VISIBLE);
                        }
                    });
                });
            };
            activity.runOnUiThread(() -> {
                accountFrom.setOnItemSelectedListener(id -> checkCurrency.run());
                accountTo.setOnItemSelectedListener(id -> checkCurrency.run());
                accountFrom.init("history_account_selector_on_edit_transfer_from", accounts, item.accountIdFrom);
                accountTo.init("history_account_selector_on_edit_transfer_to", accounts, item.accountIdTo);
            });
        });
        Button buttonAccept = findViewById(R.id.buttonAcceptTransferEditor);
        buttonAccept.setText(item.id == -1 ? R.string.add_button_label : R.string.edit_button_label);
        buttonAccept.setOnClickListener(view -> {
            long accountIdFrom = accountFrom.getSelectedId();
            long accountIdTo = accountTo.getSelectedId();
            long sum10000From;
            long sum10000To;
            try {
                sum10000From = Math.round(Double.parseDouble(sumFrom.getText().toString()) * 10000.0d);
                sum10000To = sumTo.getVisibility() == VISIBLE ? Math.round(Double.parseDouble(sumTo.getText().toString()) * 10000.0d) : sum10000From;
            } catch (NumberFormatException exception) {
                ErrorDialog.showError(activity, R.string.error_sum_parse);
                return;
            }
            if (sum10000From < 0 || sum10000To < 0) {
                ErrorDialog.showError(activity, R.string.error_sum_negative);
                return;
            }
            long createdAtUTC = DateTime.parseUTCFromLocal(date.getText().toString(), time.getText().toString());
            Worker.run(activity, () -> {
                if (item.id == -1) {
                    Repository.getRepository().insertTransfer(accountIdFrom, accountIdTo, sum10000From, sum10000To, createdAtUTC);
                    activity.runOnUiThread(() -> updateItem(item, update));
                } else {
                    Repository.getRepository().updateTransfer(item.id, accountIdFrom, accountIdTo, sum10000From, sum10000To, createdAtUTC);
                    activity.runOnUiThread(() -> updateItem(null, update));
                }
                activity.runOnUiThread(update);
            });
        });
        findViewById(R.id.buttonCancelTransferEditor).setOnClickListener(view -> updateItem(null, update));
    }

    // DON'T MODIFY item
    public void updateItem(IActionItem item, Runnable update) {
        removeAllViews();
        if (item == null) {
            return;
        }
        switch (item.getType()) {
            case IActionItem.TRANSACTION:
                updateTransactionItem((TransactionItem) item, update);
                break;
            case IActionItem.TRANSFER:
                updateTransferItem((TransferItem) item, update);
                break;
        }
    }
}
