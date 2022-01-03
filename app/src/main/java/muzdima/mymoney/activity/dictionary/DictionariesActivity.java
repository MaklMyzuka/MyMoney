package muzdima.mymoney.activity.dictionary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import muzdima.mymoney.R;
import muzdima.mymoney.activity.BaseActivity;
import muzdima.mymoney.activity.dictionary.dictionaries.AccountGroupsDictionaryActivity;
import muzdima.mymoney.activity.dictionary.dictionaries.AccountsDictionaryActivity;
import muzdima.mymoney.activity.dictionary.dictionaries.CategoriesDictionaryActivity;
import muzdima.mymoney.activity.dictionary.dictionaries.CurrenciesDictionaryActivity;

public class DictionariesActivity extends BaseActivity {

    @Override
    protected String getMenuTitle() {
        return getString(R.string.dictionaries);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionaries);
        ((Button) findViewById(R.id.buttonCurrenciesDictionary)).setOnClickListener(view -> {
            Intent intent = new Intent(this, CurrenciesDictionaryActivity.class);
            startActivity(intent);
        });
        ((Button) findViewById(R.id.buttonCategoriesDictionary)).setOnClickListener(view -> {
            Intent intent = new Intent(this, CategoriesDictionaryActivity.class);
            startActivity(intent);
        });
        ((Button) findViewById(R.id.buttonAccountsDictionary)).setOnClickListener(view -> {
            Intent intent = new Intent(this, AccountsDictionaryActivity.class);
            startActivity(intent);
        });
        ((Button) findViewById(R.id.buttonAccountGroupsDictionary)).setOnClickListener(view -> {
            Intent intent = new Intent(this, AccountGroupsDictionaryActivity.class);
            startActivity(intent);
        });
    }
}
