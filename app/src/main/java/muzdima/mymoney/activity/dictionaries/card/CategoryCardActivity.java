package muzdima.mymoney.activity.dictionaries.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.activity.BaseActivity;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.CategoryCard;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ConfirmDialog;
import muzdima.mymoney.utils.ErrorDialog;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.HistorySpinner;

public class CategoryCardActivity extends BaseActivity {

    private long categoryId = -1;

    @Override
    protected String getMenuTitle() {
        return getString(categoryId == -1 ? R.string.category_add : R.string.category_edit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_card);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        categoryId = extras.getLong("category_id", -1);

        CheckBox checkBoxHide = findViewById(R.id.checkBoxHideCategoryCard);
        EditText editTextName = findViewById(R.id.editTextNameCategoryCard);
        CheckBox checkBoxParent = findViewById(R.id.checkBoxParentCategoryCard);
        TextView textViewParent = findViewById(R.id.textViewParentCategoryCard);
        HistorySpinner historySpinnerParent = findViewById(R.id.historySpinnerParentCategoryCard);
        EditText editTextComment = findViewById(R.id.editTextCommentCategoryCard);

        checkBoxParent.setOnCheckedChangeListener((compoundButton, check) -> {
            if (check) {
                textViewParent.setVisibility(View.GONE);
                historySpinnerParent.setVisibility(View.GONE);
            } else {
                textViewParent.setVisibility(View.VISIBLE);
                historySpinnerParent.setVisibility(View.VISIBLE);
            }
        });

        Worker.run(this, () -> {
            List<SpinnerItem> categories = Repository.getRepository().getCategorySpinnerItems();
            if (categoryId != -1) {
                CategoryCard card = Repository.getRepository().getCategoryCard(categoryId);
                runOnUiThread(() -> {
                    checkBoxHide.setChecked(!card.isVisible);
                    editTextName.setText(card.name);
                    historySpinnerParent.init("history_category_selector_on_category_card_add", categories, card.parentId);
                    if (card.parentId == null) {
                        checkBoxParent.setChecked(true);
                        textViewParent.setVisibility(View.GONE);
                        historySpinnerParent.setVisibility(View.GONE);
                    } else {
                        checkBoxParent.setChecked(false);
                        textViewParent.setVisibility(View.VISIBLE);
                        historySpinnerParent.setVisibility(View.VISIBLE);
                    }
                    editTextComment.setText(card.comment);
                });
            } else {
                checkBoxParent.setChecked(false);
                textViewParent.setVisibility(View.VISIBLE);
                historySpinnerParent.setVisibility(View.VISIBLE);
                runOnUiThread(() -> historySpinnerParent.init("history_category_selector_on_category_card_edit", categories, null));
            }
        });

        Button buttonAccept = findViewById(R.id.buttonAcceptCategoryCard);
        buttonAccept.setText(getString(categoryId == -1 ? R.string.add_button_label : R.string.edit_button_label));
        buttonAccept.setOnClickListener(view -> {
            String name = editTextName.getText().toString();
            Long parentId = checkBoxParent.isChecked() ? null : historySpinnerParent.getSelectedId();
            boolean isVisible = !checkBoxHide.isChecked();
            String comment = editTextComment.getText().toString();
            Worker.run(this, () -> {
                if (categoryId == -1) {
                    Repository.getRepository().insertCategory(name, parentId, isVisible, comment);
                } else {
                    if (!Repository.getRepository().updateCategory(categoryId, name, parentId, isVisible, comment)) {
                        runOnUiThread(() -> ErrorDialog.showError(this, R.string.error_hierarchy_loop, null));
                        return;
                    }
                }
                runOnUiThread(this::finish);
            });
        });
        Button buttonDelete = findViewById(R.id.buttonDeleteCategoryCard);
        buttonDelete.setVisibility(categoryId == -1 ? View.GONE : View.VISIBLE);
        buttonDelete.setOnClickListener(view ->
                ConfirmDialog.show(this, R.string.dialog_delete_title, String.format(getString(R.string.dialog_delete_message), 1), () ->
                        Worker.run(this, () -> {
                            if (!Repository.getRepository().deleteCategory(categoryId)) {
                                runOnUiThread(() -> ErrorDialog.showError(this, R.string.error_category_delete, null));
                            } else {
                                runOnUiThread(this::finish);
                            }
                        })
                )
        );
        findViewById(R.id.buttonCancelCategoryCard).setOnClickListener(view -> finish());
    }
}
