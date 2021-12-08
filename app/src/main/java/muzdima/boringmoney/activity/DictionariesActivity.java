package muzdima.boringmoney.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import muzdima.boringmoney.R;

public class DictionariesActivity extends AppCompatActivity {
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
