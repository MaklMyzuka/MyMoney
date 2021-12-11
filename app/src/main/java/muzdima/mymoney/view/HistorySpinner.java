package muzdima.mymoney.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ActivitySolver;
import muzdima.mymoney.utils.Worker;

public class HistorySpinner extends LinearLayout {
    private final List<SpinnerItem> adapterItems = new ArrayList<>();
    private List<SpinnerItem> items;
    private Spinner spinner;
    private ArrayAdapter<SpinnerItem> adapter;
    private OnItemSelectedListener listener;
    private String historyKey;

    public HistorySpinner(Context context) {
        super(context);
        init();
    }

    public HistorySpinner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HistorySpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public HistorySpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        inflate(getContext(), R.layout.history_spinner, this);
        spinner = findViewById(R.id.spinnerHistorySpinner);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, adapterItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                long id = i < adapter.getCount() ? adapter.getItem(i).id : -1;
                Worker.run(getContext(), () -> {
                    if (historyKey != null) {
                        Repository.getRepository().setSettingsValue(historyKey, String.valueOf(id));
                    }
                });
                if (listener != null) {
                    listener.onItemSelected(id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public long getSelectedId() {
        return adapterItems.isEmpty() ? -1 : ((SpinnerItem) spinner.getSelectedItem()).id;
    }

    private void update(Long selectedId) {
        Long selectedIdOld = (adapterItems.isEmpty()
                ? null
                : getSelectedId());
        Long selectedIdOldFilter = (selectedIdOld == null
                ? null
                : (items.stream().anyMatch(item -> item.id == selectedIdOld) ? selectedIdOld : null));
        Long selectedIdCheck = (selectedId == null
                ? selectedIdOldFilter
                : (items.stream().anyMatch(item -> item.id == selectedId) ? selectedId : selectedIdOldFilter));
        long selectedIdFinal = (selectedIdCheck == null
                ? items.stream().findAny().map(item -> item.id).orElse(-1L)
                : selectedIdCheck);
        adapterItems.clear();
        adapterItems.addAll(items.stream().filter(item -> item.isVisible || item.id == selectedIdFinal).collect(Collectors.toList()));
        adapter.notifyDataSetChanged();
        adapterItems
                .stream()
                .filter(item -> item.id == selectedIdFinal)
                .findAny()
                .ifPresent(item -> spinner.setSelection(adapterItems.indexOf(item)));
        if (selectedIdOld == null || selectedIdOld != selectedIdFinal) {
            if (listener != null) {
                listener.onItemSelected(selectedIdFinal);
            }
        }
    }

    public void update() {
        update(null);
    }

    // DON'T CHANGE REFERENCE TO LIST items
    // AFTER CHANGE OF CONTENTS OF LIST items, CALL update()
    public void init(String historyKey, List<SpinnerItem> items, Long selectedId) {
        this.historyKey = historyKey;
        this.items = items;
        if (selectedId == null) {
            Activity activity = ActivitySolver.getActivity(getContext());
            Worker.run(activity, () -> {
                String historyValue = Repository.getRepository().getSettingsValue(historyKey);
                Long historyId = historyValue == null ? null : Long.valueOf(historyValue);
                activity.runOnUiThread(() -> update(historyId));
            });
        } else {
            update(selectedId);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(long id);
    }
}
