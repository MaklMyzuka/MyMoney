package muzdima.boringmoney.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.List;

import muzdima.boringmoney.R;
import muzdima.boringmoney.repository.model.IActionItem;

public class ChangeableActionList extends LinearLayout {

    private ActionListForChangeable actionList;

    public ChangeableActionList(Context context) {
        super(context);
    }

    public ChangeableActionList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangeableActionList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChangeableActionList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // DON'T CHANGE CONTENTS OF LIST items
    // FOR CHANGE, PASS NEW LIST INTO update(items)
    public void init(boolean selectable, List<IActionItem> items, IActionItem editItem, Runnable update) {
        inflate(getContext(), R.layout.changeable_action_list, this);
        actionList = findViewById(R.id.actionList);
        ActionEditor actionEditor = findViewById(R.id.actionEditor);
        actionList.init(selectable, items, actionEditor, update);
        actionEditor.updateItem(editItem, update);
    }

    public void update(List<IActionItem> items) {
        actionList.update(items);
    }

    public List<IActionItem> getSelected() {
        return actionList.getSelected();
    }

    public void toggleSelected() {
        actionList.toggleSelected();
    }

    public void setOnCheckedChangeListener(@Nullable ActionList.OnCheckedChangeListener listener) {
        actionList.setOnCheckedChangeListener(listener);
    }
}
