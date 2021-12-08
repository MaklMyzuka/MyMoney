package muzdima.mymoney.activity;

import android.content.Intent;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.DictionaryItem;

public class CategoriesDictionaryActivity extends DictionaryActivity {

    @Override
    protected String getTitleName() {
        return getString(R.string.categories);
    }

    @Override
    protected List<DictionaryItem> getItems(boolean include_hidden) {
        return Repository.getRepository().getCategoryDictionaryItems(include_hidden);
    }

    @Override
    protected void onEdit(long id) {
        Intent intent = new Intent(this, CategoryCardActivity.class);
        intent.putExtra("category_id", id);
        startActivity(intent);
    }

    @Override
    protected void onCreate() {
        Intent intent = new Intent(this, CategoryCardActivity.class);
        intent.putExtra("category_id", -1);
        startActivity(intent);
    }
}
