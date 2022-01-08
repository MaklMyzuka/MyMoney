package muzdima.mymoney.activity.dictionaries.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.activity.BaseActivity;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.AccountCard;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ConfirmDialog;
import muzdima.mymoney.utils.ErrorDialog;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.HistorySpinner;
import muzdima.mymoney.view.MultiSelect;

public class AccountCardActivity extends BaseActivity {

    private long accountId = -1;

    @Override
    protected String getMenuTitle() {
        return getString(accountId == -1 ? R.string.account_add : R.string.account_edit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_card);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        accountId = extras.getLong("account_id", -1);

        CheckBox checkBoxHide = findViewById(R.id.checkBoxHideAccountCard);
        EditText editTextName = findViewById(R.id.editTextNameAccountCard);
        HistorySpinner historySpinnerCurrency = findViewById(R.id.historySpinnerCurrencyAccountCard);
        EditText editTextSum = findViewById(R.id.editTextSumAccountCard);
        EditText editTextComment = findViewById(R.id.editTextCommentAccountCard);
        MultiSelect multiSelect = findViewById(R.id.multiSelectAccountCard);

        Worker.run(this, () -> {
            List<SpinnerItem> currencies = Repository.getRepository().getCurrencySpinnerItems();
            List<SpinnerItem> accountGroups = Repository.getRepository().getAccountGroupSpinnerItems();
            if (accountId != -1) {
                AccountCard card = Repository.getRepository().getAccountCard(accountId);
                runOnUiThread(() -> {
                    checkBoxHide.setChecked(!card.isVisible);
                    editTextName.setText(card.name);
                    historySpinnerCurrency.init("history_currency_selector_on_account_card_add", currencies, card.currencyId);
                    editTextSum.setText(String.valueOf(card.sum10000 / 10000.0d));
                    editTextComment.setText(card.comment);
                    multiSelect.init(accountGroups, card.accountGroupIds);
                });
            } else {
                runOnUiThread(() -> {
                    historySpinnerCurrency.init("history_currency_selector_on_account_card_edit", currencies, null);
                    multiSelect.init(accountGroups, new ArrayList<>());
                });
            }
        });

        Button buttonAccept = findViewById(R.id.buttonAcceptAccountCard);
        buttonAccept.setText(getString(accountId == -1 ? R.string.add_button_label : R.string.edit_button_label));
        buttonAccept.setOnClickListener(view -> {
            String name = editTextName.getText().toString();
            long currencyId = historySpinnerCurrency.getSelectedId();
            long sum10000;
            try {
                sum10000 = Math.round(Double.parseDouble(editTextSum.getText().toString()) * 10000.0d);
            } catch (NumberFormatException exception) {
                ErrorDialog.showError(this, R.string.error_sum_parse, null);
                return;
            }
            boolean isVisible = !checkBoxHide.isChecked();
            String comment = editTextComment.getText().toString();
            List<Long> accountGroupIds = multiSelect.getSelected();
            Worker.run(this, () -> {
                if (accountId == -1) {
                    Repository.getRepository().insertAccount(name, currencyId, sum10000, isVisible, comment, accountGroupIds);
                } else {
                    Repository.getRepository().updateAccount(accountId, name, currencyId, sum10000, isVisible, comment, accountGroupIds);
                }
                runOnUiThread(this::finish);
            });
        });
        Button buttonDelete = findViewById(R.id.buttonDeleteAccountCard);
        buttonDelete.setVisibility(accountId == -1 ? View.GONE : View.VISIBLE);
        buttonDelete.setOnClickListener(view ->
                ConfirmDialog.show(this, R.string.dialog_delete_title, String.format(getString(R.string.dialog_delete_message), 1), () ->
                        Worker.run(this, () -> {
                            if (!Repository.getRepository().deleteAccount(accountId)) {
                                runOnUiThread(() -> ErrorDialog.showError(this, R.string.error_account_delete, null));
                            } else {
                                runOnUiThread(this::finish);
                            }
                        })
                )
        );
        findViewById(R.id.buttonCancelAccountCard).setOnClickListener(view -> finish());
    }
}
