package muzdima.mymoney.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.AccountGroupSelector;
import muzdima.mymoney.view.AccountSelector;
import muzdima.mymoney.view.CategorySelector;
import muzdima.mymoney.activity.MainActivity;
import muzdima.mymoney.view.CategorySelectorCurrentMonth;

public class ActivityStatistics extends AppCompatActivity {
    private AccountGroupSelector[] accountGroupSelectors1;
    private CategorySelectorCurrentMonth[] categorySelectors1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_statistics);
        accountGroupSelectors1 = new AccountGroupSelector[]{
                findViewById(R.id.accountGroupSelector10),
                findViewById(R.id.accountGroupSelector11),
                findViewById(R.id.accountGroupSelector12),
                findViewById(R.id.accountGroupSelector13),
                findViewById(R.id.accountGroupSelector14),
                findViewById(R.id.accountGroupSelector15)};
        categorySelectors1 = new CategorySelectorCurrentMonth[]{
                findViewById(R.id.categorySelector10),
                findViewById(R.id.categorySelector11),
                findViewById(R.id.categorySelector12),
                findViewById(R.id.categorySelector13),
                findViewById(R.id.categorySelector14),
                findViewById(R.id.categorySelector15)};
        init();
    }
    private void init() {

        int n = 1;
        for (AccountGroupSelector accountGroupSelector : accountGroupSelectors1) {
            accountGroupSelector.init("history_account_group_selector_" + n + "_on_statistics");
            n++;
        }
        n = 1;
        for (CategorySelectorCurrentMonth categorySelector : categorySelectors1) {
            categorySelector.init("history_category_selector_" + n + "_on_statistics");
            n++;
        }
    }
}