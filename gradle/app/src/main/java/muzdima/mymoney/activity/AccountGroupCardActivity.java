package muzdima.mymoney.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.AccountGroupCard;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ConfirmDialog;
import muzdima.mymoney.utils.ErrorDialog;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.MultiSelect;

public class AccountGroupCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_group_card);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        long accountGroupId = extras.getLong("account_group_id", -1);

        CheckBox checkBoxHide = findViewById(R.id.checkBoxHideAccountGroupCard);
        EditText editTextName = findViewById(R.id.editTextNameAccountGroupCard);
        EditText editTextComment = findViewById(R.id.editTextCommentAccountGroupCard);
        MultiSelect multiSelect = findViewById(R.id.multiSelectAccountGroupCard);

        Worker.run(this, () -> {
            List<SpinnerItem> accounts = Repository.getRepository().getAccountSpinnerItems();
            if (accountGroupId != -1) {
                AccountGroupCard card = Repository.getRepository().getAccountGroupCard(accountGroupId);
                runOnUiThread(() -> {
                    checkBoxHide.setChecked(!card.isVisible);
                    editTextName.setText(card.name);
                    editTextComment.setText(card.comment);
                    multiSelect.init(accounts, card.accountIds);
                });
            } else {
                runOnUiThread(() -> multiSelect.init(accounts, new ArrayList<>()));
            }
        });

        ((TextView) findViewById(R.id.textViewTitleAccountGroupCard)).setText(accountGroupId == -1 ? R.string.account_group_add : R.string.account_group_edit);
        Button buttonAccept = findViewById(R.id.buttonAcceptAccountGroupCard);
        buttonAccept.setText(getString(accountGroupId == -1 ? R.string.add_button_label : R.string.edit_button_label));
        buttonAccept.setOnClickListener(view -> {
            String name = editTextName.getText().toString();
            boolean isVisible = !checkBoxHide.isChecked();
            String comment = editTextComment.getText().toString();
            List<Long> accountIds = multiSelect.getSelected();
            Worker.run(this, () -> {
                if (accountGroupId == -1) {
                    Repository.getRepository().insertAccountGroup(name, isVisible, comment, accountIds);
                } else {
                    Repository.getRepository().updateAccountGroup(accountGroupId, name, isVisible, comment, accountIds);
                }
                runOnUiThread(this::finish);
            });
        });
        Button buttonDelete = findViewById(R.id.buttonDeleteAccountGroupCard);
        buttonDelete.setVisibility(accountGroupId == -1 ? View.GONE : View.VISIBLE);
        buttonDelete.setOnClickListener(view ->
                ConfirmDialog.show(this, R.string.dialog_delete_title, String.format(getString(R.string.dialog_delete_message), 1), () ->
                        Worker.run(this, () -> {
                            if (!Repository.getRepository().deleteAccountGroup(accountGroupId)) {
                                runOnUiThread(() -> ErrorDialog.showError(this, R.string.error_account_group_delete));
                            } else {
                                runOnUiThread(this::finish);
                            }
                        })
                )
        );
        findViewById(R.id.buttonCancelAccountGroupCard).setOnClickListener(view -> finish());
    }
}
