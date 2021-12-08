package muzdima.mymoney.activity;

import android.content.Intent;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.DictionaryItem;

public class CurrenciesDictionaryActivity extends DictionaryActivity {

    @Override
    protected String getTitleName() {
        return getString(R.string.currencies);
    }

    @Override
    protected List<DictionaryItem> getItems(boolean include_hidden) {
        return Repository.getRepository().getCurrencyDictionaryItems(include_hidden);
    }

    @Override
    protected void onEdit(long id) {
        Intent intent = new Intent(this, CurrencyCardActivity.class);
        intent.putExtra("currency_id", id);
        startActivity(intent);
    }

    @Override
    protected void onCreate() {
        Intent intent = new Intent(this, CurrencyCardActivity.class);
        intent.putExtra("currency_id", -1);
        startActivity(intent);
    }
}
