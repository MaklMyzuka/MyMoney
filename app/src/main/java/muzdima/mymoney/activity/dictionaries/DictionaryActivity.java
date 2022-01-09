package muzdima.mymoney.activity.dictionaries;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.activity.BaseActivity;
import muzdima.mymoney.repository.model.DictionaryItem;
import muzdima.mymoney.utils.Worker;

public abstract class DictionaryActivity extends BaseActivity {

    private ViewGroup layoutDictionaryItems;
    private SwitchCompat switchShowHidden;

    protected abstract List<DictionaryItem> getItems(boolean include_hidden);

    protected abstract void onCreate();

    protected abstract void onEdit(long id);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        findViewById(R.id.buttonAddDictionary).setOnClickListener(view -> onCreate());
        layoutDictionaryItems = findViewById(R.id.layoutDictionaryItems);
        switchShowHidden = findViewById(R.id.switchShowHidden);
        switchShowHidden.setChecked(true);
        switchShowHidden.setOnCheckedChangeListener((compoundButton, b) -> update());
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        boolean include_hidden = switchShowHidden.isChecked();
        Worker.run(this, () -> {
            List<DictionaryItem> items = getItems(include_hidden);
            runOnUiThread(() -> {
                layoutDictionaryItems.removeAllViews();
                for (DictionaryItem item : items) {
                    @SuppressLint("InflateParams")
                    View itemView = getLayoutInflater().inflate(R.layout.dictionary_item, null);
                    ((TextView) itemView.findViewById(R.id.textViewDictionaryName)).setText(item.name);
                    if (item.isVisible) {
                        ((TextView) itemView.findViewById(R.id.textViewDictionaryHidden)).setText("");
                    } else {
                        ((TextView) itemView.findViewById(R.id.textViewDictionaryHidden)).setText(R.string.hidden);
                    }
                    if (item.comment == null || item.comment.isEmpty()) {
                        itemView.findViewById(R.id.textViewDictionaryComment).setVisibility(View.GONE);
                    } else {
                        ((TextView) itemView.findViewById(R.id.textViewDictionaryComment)).setText(item.comment);
                    }
                    itemView.findViewById(R.id.buttonDictionaryEdit).setOnClickListener(view -> onEdit(item.id));
                    layoutDictionaryItems.addView(itemView);
                }
            });
        });
    }
}
