package muzdima.mymoney.activity;

import android.os.Bundle;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.AccountGroupSelector;
import muzdima.mymoney.view.AccountSelector;
import muzdima.mymoney.view.CategorySelectorCurrentMonth;

public class MainActivity extends MenuActivity {

    private AccountSelector[] accountSelectors;
    private CategorySelectorCurrentMonth[] categorySelectors;
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
        categorySelectors = new CategorySelectorCurrentMonth[]{
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
        for (CategorySelectorCurrentMonth categorySelector : categorySelectors) {
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
        for (CategorySelectorCurrentMonth categorySelector : categorySelectors) {
            categorySelector.update();
        }
        for (AccountGroupSelector accountGroupSelector : accountGroupSelectors) {
            accountGroupSelector.update();
        }
        super.onResume();
    }
}
