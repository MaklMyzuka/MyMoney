package muzdima.boringmoney.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import muzdima.boringmoney.repository.model.IActionItem;

public class ActionListForChangeable extends ActionList {
    Runnable update;
    ActionEditor actionEditor;

    public ActionListForChangeable(@NonNull Context context) {
        super(context);
    }

    public ActionListForChangeable(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionListForChangeable(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // DON'T CHANGE CONTENTS OF LIST items
    // FOR CHANGE, PASS NEW LIST INTO update(items)
    public void init(boolean selectable, List<IActionItem> items, ActionEditor actionEditor, Runnable update){
        this.actionEditor = actionEditor;
        this.update = update;
        init(selectable, items);
    }

    @Override
    protected void onEdit(IActionItem item) {
        actionEditor.updateItem(item, update);
    }
}
