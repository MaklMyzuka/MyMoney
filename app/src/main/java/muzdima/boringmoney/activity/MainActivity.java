package muzdima.boringmoney.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import muzdima.boringmoney.R;
import muzdima.boringmoney.repository.Repository;
import muzdima.boringmoney.utils.Worker;
import muzdima.boringmoney.view.AccountGroupSelector;
import muzdima.boringmoney.view.AccountSelector;
import muzdima.boringmoney.view.CategorySelector;

public class MainActivity extends AppCompatActivity {

    private AccountSelector[] accountSelectors;
    private CategorySelector[] categorySelectors;
    private AccountGroupSelector[] accountGroupSelectors;
    private boolean firstResume = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accountSelectors = new AccountSelector[]{
                findViewById(R.id.accountSelector1),
                findViewById(R.id.accountSelector2),
                findViewById(R.id.accountSelector3),
                findViewById(R.id.accountSelector4),
                findViewById(R.id.accountSelector5),
                findViewById(R.id.accountSelector6)};
        categorySelectors = new CategorySelector[]{
                findViewById(R.id.categorySelector1),
                findViewById(R.id.categorySelector2),
                findViewById(R.id.categorySelector3)};
        accountGroupSelectors = new AccountGroupSelector[]{
                findViewById(R.id.accountGroupSelector1),
                findViewById(R.id.accountGroupSelector2),
                findViewById(R.id.accountGroupSelector3)};
        Worker.run(this, () -> {
            Repository.getRepository().init(getApplicationContext());
            runOnUiThread(this::init);
        });
    }

    private void init() {
        int n = 1;
        for (AccountSelector accountSelector : accountSelectors) {
            accountSelector.init("history_account_selector_" + n + "_on_main");
            n++;
        }
        n = 1;
        for (CategorySelector categorySelector : categorySelectors) {
            categorySelector.init("history_category_selector_" + n + "_on_main");
            n++;
        }
        n = 1;
        for (AccountGroupSelector accountGroupSelector : accountGroupSelectors) {
            accountGroupSelector.init("history_account_group_selector_" + n + "_on_main");
            n++;
        }
    }

    @Override
    public void onResume() {
        if (firstResume) {
            firstResume = false;
            super.onResume();
            return;
        }
        for (AccountSelector accountSelector : accountSelectors) {
            accountSelector.update();
        }
        for (CategorySelector categorySelector : categorySelectors) {
            categorySelector.update();
        }
        for (AccountGroupSelector accountGroupSelector : accountGroupSelectors) {
            accountGroupSelector.update();
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_actions) {
            Intent intent = new Intent(this, ActionsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_draft) {
            Intent intent = new Intent(this, DraftActivity.class);
            intent.putExtra("action", 0);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_dictionaries) {
            Intent intent = new Intent(this, DictionariesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_import_export) {
            Intent intent = new Intent(this, ImportExportActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
