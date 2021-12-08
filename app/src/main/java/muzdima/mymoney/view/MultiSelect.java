package muzdima.mymoney.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ActivitySolver;

public class MultiSelect extends LinearLayout {

    private final List<CheckBox> checkBoxes = new ArrayList<>();

    public MultiSelect(Context context) {
        super(context);
        init();
    }

    public MultiSelect(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiSelect(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MultiSelect(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
    }

    public List<Long> getSelected() {
        List<Long> result = new ArrayList<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                result.add((Long) checkBox.getTag());
            }
        }
        return result;
    }

    // UPDATE IS NOT SUPPORTED
    public void init(List<SpinnerItem> items, List<Long> selected) {
        checkBoxes.clear();
        Set<Long> selectedSet = Collections.newSetFromMap(new IdentityHashMap<>());
        selectedSet.addAll(selected);
        Activity activity = ActivitySolver.getActivity(getContext());
        List<SpinnerItem> itemsFiltered = items.stream().filter(item -> item.isVisible || selectedSet.contains(item.id)).collect(Collectors.toList());
        for (SpinnerItem item : itemsFiltered) {
            @SuppressLint("InflateParams")
            View itemView = activity.getLayoutInflater().inflate(R.layout.multi_select_item, null);
            ((TextView) itemView.findViewById(R.id.textViewMultiSelect)).setText(item.text);
            CheckBox checkBox = itemView.findViewById(R.id.checkBoxMultiSelect);
            checkBox.setChecked(selectedSet.contains(item.id));
            checkBox.setTag(item.id);
            checkBoxes.add(checkBox);
            addView(itemView);
        }
    }
}
