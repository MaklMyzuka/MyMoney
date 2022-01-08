package muzdima.mymoney.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.ActionList;
import muzdima.mymoney.view.selector.CategorySelector;

public class ActionsActivity extends BaseActivity {
    private long dateStartUTC;
    private TextView textViewDate;
    private ActionList actionList;
    private CategorySelector categorySelector;

    @Override
    protected String getMenuTitle() {
        return getString(R.string.actions);
    }

    // CALL FROM WORKER THREAD
    private void update(boolean forceRedraw) {
        List<IActionItem> items = Repository.getRepository().getActionItems(fromUTC(), toUTC());
        runOnUiThread(() -> {
            actionList.update(items, forceRedraw);
            categorySelector.update(fromUTC(), toUTC());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setDate();
        Worker.run(this, () -> update(true));
    }

    private long dateUTC() {
        return dateStartUTC / 2 + DateTime.addDaysToUTC(dateStartUTC, 1) / 2;
    }

    private void setDate() {
        textViewDate.setText(DateTime.printDate(this, DateTime.convertUTCToLocal(dateUTC())));
    }

    private long fromUTC() {
        return dateStartUTC;
    }

    private long toUTC() {
        return DateTime.addDaysToUTC(dateStartUTC, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);
        actionList = findViewById(R.id.changeableActionListActions);
        textViewDate = findViewById(R.id.textViewDateActions);
        categorySelector = findViewById(R.id.categorySelectorActions);
        DateTime now = DateTime.convertUTCToLocal(DateTime.getNowUTC());
        dateStartUTC = DateTime.convertLocalToUTC(new DateTime(now.year, now.month, now.day, 0, 0, 0));
        categorySelector.init("history_category_selector_on_actions", fromUTC(), toUTC());
        setDate();

        Worker.run(this, () -> {
            List<IActionItem> items = Repository.getRepository().getActionItems(fromUTC(), toUTC());
            runOnUiThread(() -> {
                actionList.init(false, items, null);
                textViewDate.setOnClickListener(view -> {
                    DateTime local = DateTime.convertUTCToLocal(dateUTC());
                    new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                        dateStartUTC = DateTime.convertLocalToUTC(new DateTime(year, month + 1, dayOfMonth, 0, 0, 0));
                        setDate();
                        Worker.run(this, () -> update(false));
                    },
                            local.year,
                            local.month - 1,
                            local.day)
                            .show();
                });
                findViewById(R.id.buttonPrevActions).setOnClickListener(view -> {
                    dateStartUTC = DateTime.addDaysToUTC(dateStartUTC, -1);
                    setDate();
                    Worker.run(this, () -> update(false));
                });
                findViewById(R.id.buttonNextActions).setOnClickListener(view -> {
                    dateStartUTC = DateTime.addDaysToUTC(dateStartUTC, 1);
                    setDate();
                    Worker.run(this, () -> update(false));
                });
            });
        });
    }
}
