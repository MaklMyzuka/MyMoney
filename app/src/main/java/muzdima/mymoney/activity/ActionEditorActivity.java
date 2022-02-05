package muzdima.mymoney.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.AccountInfo;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.repository.model.TransactionItem;
import muzdima.mymoney.repository.model.TransferItem;
import muzdima.mymoney.utils.ConfigurationPreferences;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.ErrorDialog;
import muzdima.mymoney.utils.InfoDialog;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.HistorySpinner;

public class ActionEditorActivity extends BaseActivity {
    private long createdAtUTC;
    private String title;

    @Override
    protected String getMenuTitle() {
        return title;
    }

    @Override
    protected boolean isMenuButtonSupported() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String itemTransactionStr = extras.getString("itemTransaction", null);
        String itemTransferStr = extras.getString("itemTransfer", null);
        IActionItem item = null;
        if (itemTransactionStr != null) {
            item = new Gson().fromJson(itemTransactionStr, TransactionItem.class);
        }
        if (itemTransferStr != null) {
            item = new Gson().fromJson(itemTransferStr, TransferItem.class);
        }
        updateItem(item);
    }

    private void updateItem(IActionItem item) {
        if (item == null) {
            finish();
            return;
        }
        title = (item.getId() == -1 ? getString(R.string.add_title) : getString(R.string.edit_title));
        switch (item.getType()) {
            case IActionItem.TRANSACTION:
                updateTransactionItem((TransactionItem) item);
                break;
            case IActionItem.TRANSFER:
                updateTransferItem((TransferItem) item);
                break;
        }
    }

    private void setDateTextView(TextView date) {
        date.setText(DateTime.printDate(this, DateTime.convertUTCToLocal(createdAtUTC)));
        date.setOnClickListener(view -> {
            DateTime localInit = DateTime.convertUTCToLocal(createdAtUTC);
            new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                DateTime local = DateTime.convertUTCToLocal(createdAtUTC);
                createdAtUTC = DateTime.convertLocalToUTC(new DateTime(year, month + 1, dayOfMonth, local.hours, local.minutes, local.seconds));
                date.setText(DateTime.printDate(this, DateTime.convertUTCToLocal(createdAtUTC)));
            },
                    localInit.year,
                    localInit.month - 1,
                    localInit.day)
                    .show();
        });
    }

    private void setTimeTextView(TextView time) {
        time.setText(DateTime.printTime(this, DateTime.convertUTCToLocal(createdAtUTC)));
        time.setOnClickListener(view -> {
            DateTime localInit = DateTime.convertUTCToLocal(createdAtUTC);
            new TimePickerDialog(this, (datePicker, hourOfDay, minute) -> {
                DateTime local = DateTime.convertUTCToLocal(createdAtUTC);
                createdAtUTC = DateTime.convertLocalToUTC(new DateTime(local.year, local.month, local.day, hourOfDay, minute, 0));
                time.setText(DateTime.printTime(this, DateTime.convertUTCToLocal(createdAtUTC)));
            },
                    localInit.hours,
                    localInit.minutes,
                    true)
                    .show();
        });
    }

    private void showAddInformDialog(boolean intoDraft) {
        InfoDialog.show(this, R.string.success, intoDraft ? R.string.add_into_draft_success_message : R.string.add_success_message, null);
    }

    private void updateTransactionItem(TransactionItem item) {
        setContentView(R.layout.action_editor_transaction);
        boolean isIncome = item.sum.sum10000 > 0;
        getWindow().getDecorView().setBackgroundColor(this.getColor(isIncome ? R.color.transaction_income : R.color.transaction_expense));

        createdAtUTC = item.createdAtUTC;
        ((TextView) findViewById(R.id.textViewTitleTransactionEditor)).setText(isIncome ? R.string.action_income : R.string.action_expense);
        setDateTextView(findViewById(R.id.textViewDateTransactionEditor));
        setTimeTextView(findViewById(R.id.textViewTimeTransactionEditor));
        TextView currency = findViewById(R.id.textViewCurrencyTransactionEditor);
        currency.setText(item.sum.currencySymbol);
        HistorySpinner account = findViewById(R.id.accountTransactionEditor);
        Worker.run(this, () -> {
            List<SpinnerItem> accounts = Repository.getRepository().getAccountSpinnerItems();
            runOnUiThread(() -> {
                account.setOnItemSelectedListener(id ->
                        Worker.run(this, () -> {
                            String currencySymbol = Repository.getRepository().getAccountInfo(id).currencySymbol;
                            runOnUiThread(() -> currency.setText(currencySymbol));
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
        Worker.run(this, () -> {
            List<SpinnerItem> categories = Repository.getRepository().getCategorySpinnerItems();
            runOnUiThread(() ->
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
                ErrorDialog.showError(this, R.string.error_sum_parse, null);
                return;
            }
            if (sum10000 < 0) {
                ErrorDialog.showError(this, R.string.error_sum_negative, null);
                return;
            }
            long sum1000Final = isIncome ? sum10000 : -sum10000;
            String productValue = product.getText().toString();
            boolean useDraft = ConfigurationPreferences.useDraft(this);
            Worker.run(this, () -> {
                if (item.id == -1) {
                    Repository.getRepository().insertTransaction(categoryId, accountId, sum1000Final, productValue, createdAtUTC, useDraft);
                    TransactionItem transaction = new TransactionItem();
                    transaction.id = -1;
                    transaction.categoryId = categoryId;
                    transaction.accountId = accountId;
                    transaction.sum = new Money.MoneyItem();
                    transaction.sum.sum10000 = isIncome ? 1 : -1;
                    transaction.product = productValue;
                    transaction.createdAtUTC = createdAtUTC;
                    runOnUiThread(() -> {
                        updateItem(transaction);
                        showAddInformDialog(useDraft);
                    });
                } else {
                    Repository.getRepository().updateTransaction(item.id, categoryId, accountId, sum1000Final, productValue, createdAtUTC);
                    runOnUiThread(() -> updateItem(null));
                }
            });
        });
        findViewById(R.id.buttonCancelTransactionEditor).setOnClickListener(view -> updateItem(null));
    }

    private void updateTransferItem(TransferItem item) {
        setContentView(R.layout.action_editor_transfer);
        getWindow().getDecorView().setBackgroundColor(this.getColor(R.color.transfer));

        createdAtUTC = item.createdAtUTC;
        ((TextView) findViewById(R.id.textViewTitleTransferEditor)).setText(R.string.action_transfer);
        setDateTextView(findViewById(R.id.textViewDateTransferEditor));
        setTimeTextView(findViewById(R.id.textViewTimeTransferEditor));
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
        Worker.run(this, () -> {
            List<SpinnerItem> accounts = Repository.getRepository().getAccountSpinnerItems();
            Runnable checkCurrency = () -> {
                long accountIdFrom = accountFrom.getSelectedId();
                long accountIdTo = accountTo.getSelectedId();
                Worker.run(this, () -> {
                    AccountInfo accountInfoFrom = Repository.getRepository().getAccountInfo(accountIdFrom);
                    AccountInfo accountInfoTo = Repository.getRepository().getAccountInfo(accountIdTo);
                    runOnUiThread(() -> {
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
            runOnUiThread(() -> {
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
                ErrorDialog.showError(this, R.string.error_sum_parse, null);
                return;
            }
            if (sum10000From < 0 || sum10000To < 0) {
                ErrorDialog.showError(this, R.string.error_sum_negative, null);
                return;
            }
            boolean useDraft = ConfigurationPreferences.useDraft(this);
            Worker.run(this, () -> {
                if (item.id == -1) {
                    Repository.getRepository().insertTransfer(accountIdFrom, accountIdTo, sum10000From, sum10000To, createdAtUTC, useDraft);
                    runOnUiThread(() -> {
                        updateItem(item);
                        showAddInformDialog(useDraft);
                    });
                } else {
                    Repository.getRepository().updateTransfer(item.id, accountIdFrom, accountIdTo, sum10000From, sum10000To, createdAtUTC);
                    runOnUiThread(() -> updateItem(null));
                }
            });
        });
        findViewById(R.id.buttonCancelTransferEditor).setOnClickListener(view -> updateItem(null));
    }
}
