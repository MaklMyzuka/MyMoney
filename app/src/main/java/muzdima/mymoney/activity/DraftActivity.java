package muzdima.mymoney.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.AccountInfo;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.TransactionItem;
import muzdima.mymoney.repository.model.TransferItem;
import muzdima.mymoney.utils.ConfirmDialog;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.ChangeableActionList;

public class DraftActivity extends MenuActivity {

    private Button buttonToggle;
    private ChangeableActionList actionList;

    // CALL FROM WORKER THREAD
    private void update() {
        List<IActionItem> items = Repository.getRepository().getDraftActionItems();
        runOnUiThread(() -> actionList.update(items));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);
        buttonToggle = findViewById(R.id.buttonToggleDraft);
        actionList = findViewById(R.id.changeableActionListDraft);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int action = extras.getInt("action", 0);
        long accountId = extras.getLong("account_id", -1);

        Worker.run(this, () -> {
            IActionItem editItem = null;
            AccountInfo accountInfo = (action == 0 ? null : Repository.getRepository().getAccountInfo(accountId));
            switch (action) {
                case 0:
                    // no action
                    break;
                case 1:
                    // expense
                    TransactionItem expense = new TransactionItem();
                    expense.id = -1;
                    expense.categoryId = -1;
                    expense.categoryName = null;
                    expense.accountId = accountInfo.id;
                    expense.accountName = accountInfo.name;
                    expense.sum = new Money.MoneyItem();
                    expense.sum.sum10000 = -1;
                    expense.sum.currencyId = accountInfo.currencyId;
                    expense.sum.currencySymbol = accountInfo.currencySymbol;
                    expense.product = "";
                    expense.createdAtUTC = DateTime.getNowUTC();
                    editItem = expense;
                    break;
                case 2:
                    // income
                    TransactionItem income = new TransactionItem();
                    income.id = -1;
                    income.categoryId = -1;
                    income.categoryName = null;
                    income.accountId = accountInfo.id;
                    income.accountName = accountInfo.name;
                    income.sum = new Money.MoneyItem();
                    income.sum.sum10000 = 1;
                    income.sum.currencyId = accountInfo.currencyId;
                    income.sum.currencySymbol = accountInfo.currencySymbol;
                    income.product = "";
                    income.createdAtUTC = DateTime.getNowUTC();
                    editItem = income;
                    break;
                case 3:
                    // transfer from
                    TransferItem transferFrom = new TransferItem();
                    transferFrom.id = -1;
                    transferFrom.accountIdFrom = accountInfo.id;
                    transferFrom.accountNameFrom = accountInfo.name;
                    transferFrom.sumFrom = new Money.MoneyItem();
                    transferFrom.sumFrom.sum10000 = 0;
                    transferFrom.sumFrom.currencyId = accountInfo.currencyId;
                    transferFrom.sumFrom.currencySymbol = accountInfo.currencySymbol;
                    transferFrom.accountIdTo = -1;
                    transferFrom.accountNameTo = null;
                    transferFrom.sumTo = new Money.MoneyItem();
                    transferFrom.sumTo.sum10000 = 0;
                    transferFrom.sumTo.currencyId = -1;
                    transferFrom.sumTo.currencySymbol = null;
                    transferFrom.createdAtUTC = DateTime.getNowUTC();
                    editItem = transferFrom;
                    break;
                case 4:
                    // transfer to
                    TransferItem transferTo = new TransferItem();
                    transferTo.id = -1;
                    transferTo.accountIdFrom = -1;
                    transferTo.accountNameFrom = null;
                    transferTo.sumFrom = new Money.MoneyItem();
                    transferTo.sumFrom.sum10000 = 0;
                    transferTo.sumFrom.currencyId = -1;
                    transferTo.sumFrom.currencySymbol = null;
                    transferTo.accountIdTo = accountInfo.id;
                    transferTo.accountNameTo = accountInfo.name;
                    transferTo.sumTo = new Money.MoneyItem();
                    transferTo.sumTo.sum10000 = 0;
                    transferTo.sumTo.currencyId = accountInfo.currencyId;
                    transferTo.sumTo.currencySymbol = accountInfo.currencySymbol;
                    transferTo.createdAtUTC = DateTime.getNowUTC();
                    editItem = transferTo;
                    break;
            }
            List<IActionItem> items = Repository.getRepository().getDraftActionItems();
            IActionItem editItemFinal = editItem;
            runOnUiThread(() -> {
                actionList.init(true, items, editItemFinal, () -> Worker.run(this, this::update));
                actionList.setOnCheckedChangeListener(selectedAll -> buttonToggle.setText(selectedAll ? R.string.toggle_none_button_label : R.string.toggle_all_button_label));
                findViewById(R.id.buttonToggleDraft).setOnClickListener(view -> actionList.toggleSelected());
                findViewById(R.id.buttonCommitDraft).setOnClickListener(view -> {
                    List<IActionItem> selected = actionList.getSelected();
                    ConfirmDialog.show(this, R.string.dialog_commit_title, String.format(getString(R.string.dialog_commit_message), selected.size()), () ->
                            Worker.run(this, () -> {
                                Repository.getRepository().commitActionItems(selected);
                                update();
                            })
                    );
                });
                findViewById(R.id.buttonDeleteDraft).setOnClickListener(view -> {
                    List<IActionItem> selected = actionList.getSelected();
                    ConfirmDialog.show(this, R.string.dialog_delete_title, String.format(getString(R.string.dialog_delete_message), selected.size()), () ->
                            Worker.run(this, () -> {
                                Repository.getRepository().deleteActionItems(selected);
                                update();
                            })
                    );
                });
            });
        });
    }
}
