package muzdima.mymoney.activity.dictionary.cards;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import muzdima.mymoney.R;
import muzdima.mymoney.activity.BaseActivity;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.CurrencyCard;
import muzdima.mymoney.utils.ConfirmDialog;
import muzdima.mymoney.utils.ErrorDialog;
import muzdima.mymoney.utils.Worker;

public class CurrencyCardActivity extends BaseActivity {

    private long currencyId = -1;

    @Override
    protected String getMenuTitle() {
        return getString(currencyId == -1 ? R.string.currency_add : R.string.currency_edit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_card);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        currencyId = extras.getLong("currency_id", -1);

        CheckBox checkBoxHide = findViewById(R.id.checkBoxHideCurrencyCard);
        EditText editTextName = findViewById(R.id.editTextNameCurrencyCard);
        EditText editTextSymbol = findViewById(R.id.editTextSymbolCurrencyCard);
        EditText editTextComment = findViewById(R.id.editTextCommentCurrencyCard);

        if (currencyId != -1) {
            Worker.run(this, () -> {
                CurrencyCard card = Repository.getRepository().getCurrencyCard(currencyId);
                runOnUiThread(() -> {
                    checkBoxHide.setChecked(!card.isVisible);
                    editTextName.setText(card.name);
                    editTextSymbol.setText(card.symbol);
                    editTextComment.setText(card.comment);
                });
            });
        }

        Button buttonAccept = findViewById(R.id.buttonAcceptCurrencyCard);
        buttonAccept.setText(getString(currencyId == -1 ? R.string.add_button_label : R.string.edit_button_label));
        buttonAccept.setOnClickListener(view -> {
            String name = editTextName.getText().toString();
            String symbol = editTextSymbol.getText().toString();
            boolean isVisible = !checkBoxHide.isChecked();
            String comment = editTextComment.getText().toString();
            Worker.run(this, () -> {
                if (currencyId == -1) {
                    Repository.getRepository().insertCurrency(name, symbol, isVisible, comment);
                } else {
                    Repository.getRepository().updateCurrency(currencyId, name, symbol, isVisible, comment);
                }
                runOnUiThread(this::finish);
            });
        });
        Button buttonDelete = findViewById(R.id.buttonDeleteCurrencyCard);
        buttonDelete.setVisibility(currencyId == -1 ? View.GONE : View.VISIBLE);
        buttonDelete.setOnClickListener(view ->
                ConfirmDialog.show(this, R.string.dialog_delete_title, String.format(getString(R.string.dialog_delete_message), 1), () ->
                        Worker.run(this, () -> {
                            if (!Repository.getRepository().deleteCurrency(currencyId)) {
                                runOnUiThread(() -> ErrorDialog.showError(this, R.string.error_currency_delete, null));
                            } else {
                                runOnUiThread(this::finish);
                            }
                        })
                )
        );
        findViewById(R.id.buttonCancelCurrencyCard).setOnClickListener(view -> finish());
    }
}
