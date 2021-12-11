package muzdima.mymoney.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;




import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.utils.ConfirmDialog;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.CategorySelector;
import muzdima.mymoney.view.ChangeableActionList;

public class ActionsActivity extends MenuActivity {
    private long dateStartUTC;
    private TextView textViewDate;
    private Button buttonToggle;
    private ChangeableActionList actionList;
    private CategorySelector categorySelector;

    // CALL FROM WORKER THREAD
    private void update() {
        List<IActionItem> items = Repository.getRepository().getActionItems(fromUTC(), toUTC());
        runOnUiThread(() -> {
            actionList.update(items);
            categorySelector.update(fromUTC(), toUTC());
        });
    }

    private long dateUTC() {
        return dateStartUTC / 2 + DateTime.addDays(dateStartUTC, 1) / 2;
    }

    private void setDate() {
        textViewDate.setText(DateTime.printUTCToLocalDate(dateUTC()));
    }

    private long fromUTC() {
        return dateStartUTC;
    }

    private long toUTC() {
        return DateTime.addDays(dateStartUTC, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);
        buttonToggle = findViewById(R.id.buttonToggleActions);
        actionList = findViewById(R.id.changeableActionListActions);
        textViewDate = findViewById(R.id.textViewDateActions);
        categorySelector = findViewById(R.id.categorySelectorActions);
        dateStartUTC = DateTime.getLocalDayStartUTC(DateTime.getNowUTC());
        categorySelector.init("history_category_selector_on_actions", fromUTC(), toUTC());
        setDate();

        Worker.run(this, () -> {
            List<IActionItem> items = Repository.getRepository().getActionItems(fromUTC(), toUTC());
            runOnUiThread(() -> {
                actionList.init(true, items, null, () -> Worker.run(this, this::update));
                actionList.setOnCheckedChangeListener(selectedAll -> buttonToggle.setText(selectedAll ? R.string.toggle_none_button_label : R.string.toggle_all_button_label));
                findViewById(R.id.buttonToggleActions).setOnClickListener(view -> actionList.toggleSelected());
                findViewById(R.id.buttonDeleteActions).setOnClickListener(view -> {
                    List<IActionItem> selected = actionList.getSelected();
                    ConfirmDialog.show(this, R.string.dialog_delete_title, String.format(getString(R.string.dialog_delete_message), selected.size()), () ->
                            Worker.run(this, () -> {
                                Repository.getRepository().deleteActionItems(selected);
                                update();
                            })
                    );
                });
                textViewDate.setOnClickListener(view -> {
                    long dateUTC = dateUTC();
                    new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                        dateStartUTC = DateTime.parseUTCFromLocal(DateTime.printLocalDate(year, month + 1, dayOfMonth), "00:00");
                        setDate();
                        Worker.run(this, this::update);
                    },
                            DateTime.getLocalYear(dateUTC),
                            DateTime.getLocalMonth(dateUTC) - 1,
                            DateTime.getLocalDayOfMonth(dateUTC))
                            .show();
                });
                findViewById(R.id.buttonPrevActions).setOnClickListener(view -> {
                    dateStartUTC = DateTime.addDays(dateStartUTC, -1);
                    setDate();
                    Worker.run(this, this::update);
                });
                findViewById(R.id.buttonNextActions).setOnClickListener(view -> {
                    dateStartUTC = DateTime.addDays(dateStartUTC, 1);
                    setDate();
                    Worker.run(this, this::update);
                });
            });
        });
    }
}
