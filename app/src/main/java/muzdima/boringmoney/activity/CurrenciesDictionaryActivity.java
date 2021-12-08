package muzdima.boringmoney.activity;

import android.content.Intent;

import java.util.List;

import muzdima.boringmoney.R;
import muzdima.boringmoney.repository.Repository;
import muzdima.boringmoney.repository.model.CurrencyCard;
import muzdima.boringmoney.repository.model.DictionaryItem;

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
