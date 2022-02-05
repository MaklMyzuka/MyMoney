package muzdima.mymoney.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.AccountInfo;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.TransactionItem;
import muzdima.mymoney.repository.model.TransferItem;
import muzdima.mymoney.utils.ActionHelper;
import muzdima.mymoney.utils.ActivitySolver;
import muzdima.mymoney.utils.ConfirmDialog;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.ErrorDialog;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.ActionList;
import muzdima.mymoney.view.MoneyTextView;

public class DraftActivity extends BaseActivity {

    private Button buttonToggle;
    private ActionList actionList;
    private TextView labelTotal;
    private MoneyTextView moneyTotal;
    private boolean skipResume = false;

    @Override
    protected String getMenuTitle() {
        return getString(R.string.draft);
    }

    // CALL FROM WORKER THREAD
    private void update(boolean forceRedraw) {
        List<IActionItem> items = Repository.getRepository().getDraftActionItems();
        runOnUiThread(() -> actionList.update(items, forceRedraw));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (skipResume) {
            skipResume = false;
        } else {
            Worker.run(this, () -> update(true));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);
        buttonToggle = findViewById(R.id.buttonToggleDraft);
        actionList = findViewById(R.id.changeableActionListDraft);
        labelTotal = findViewById(R.id.textViewTotalMoneyDraft);
        moneyTotal = findViewById(R.id.moneyTextViewDraft);

        Money.DisplayParams displayParams = new Money.DisplayParams(this);
        displayParams.plus = true;
        displayParams.bold = true;
        moneyTotal.setDisplayParams(displayParams);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int action = extras.getInt("action", 0);
        long accountId = extras.getLong("account_id", -1);
        skipResume = action != 0;

        Worker.run(this, () -> {
            List<IActionItem> items = Repository.getRepository().getDraftActionItems();
            IActionItem editItemFinal = ActionHelper.createAction(action, accountId);
            runOnUiThread(() -> {
                actionList.init(true, items);
                actionList.setOnCheckedChangeListener(selectedAll -> {
                    buttonToggle.setText(selectedAll ? R.string.toggle_none_button_label : R.string.toggle_all_button_label);
                    List<IActionItem> selected = actionList.getSelected();
                    Money total = new Money();
                    for (IActionItem item : selected) {
                        total.add(item.getMoney());
                    }
                    if (total.items.isEmpty()) {
                        labelTotal.setVisibility(View.GONE);
                        moneyTotal.setVisibility(View.GONE);
                    } else {
                        moneyTotal.setText(total);
                        labelTotal.setVisibility(View.VISIBLE);
                        moneyTotal.setVisibility(View.VISIBLE);
                    }
                });
                findViewById(R.id.buttonToggleDraft).setOnClickListener(view -> actionList.toggleSelected());
                findViewById(R.id.buttonCommitDraft).setOnClickListener(view -> {
                    List<IActionItem> selected = actionList.getSelected();
                    ConfirmDialog.show(this, R.string.dialog_commit_title, String.format(getString(R.string.dialog_commit_message), selected.size()), () ->
                            Worker.run(this, () -> {
                                Repository.getRepository().commitActionItems(selected);
                                update(false);
                            })
                    );
                });
                findViewById(R.id.buttonDeleteDraft).setOnClickListener(view -> {
                    List<IActionItem> selected = actionList.getSelected();
                    ConfirmDialog.show(this, R.string.dialog_delete_title, String.format(getString(R.string.dialog_delete_message), selected.size()), () ->
                            Worker.run(this, () -> {
                                Repository.getRepository().deleteActionItems(selected);
                                update(false);
                            })
                    );
                });
                intent.putExtra("action", 0);
                setIntent(intent);
                if (editItemFinal != null) {
                    actionList.onEdit(editItemFinal);
                }
            });
        });
    }
}
