package muzdima.mymoney.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.model.DictionaryItem;
import muzdima.mymoney.utils.Worker;

public abstract class DictionaryActivity extends AppCompatActivity {

    private ViewGroup layoutDictionaryItems;
    private SwitchCompat switchShowHidden;

    protected abstract String getTitleName();

    protected abstract List<DictionaryItem> getItems(boolean include_hidden);

    protected abstract void onCreate();

    protected abstract void onEdit(long id);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        ((TextView) findViewById(R.id.textViewDictionaryTitle)).setText(getTitleName());
        findViewById(R.id.buttonAddDictionary).setOnClickListener(view -> onCreate());
        layoutDictionaryItems = findViewById(R.id.layoutDictionaryItems);
        switchShowHidden = findViewById(R.id.switchShowHidden);
        switchShowHidden.setChecked(true);
        switchShowHidden.setOnCheckedChangeListener((compoundButton, b) -> update());
    }

    @Override
    public void onResume() {
        update();
        super.onResume();
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