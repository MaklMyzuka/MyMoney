package muzdima.mymoney.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.MoneyListItem;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.MoneyList;

public class StatisticsActivity extends BaseActivity {
    private ViewGroup layout;
    private TabLayout tabs;

    @Override
    protected String getMenuTitle() {
        return getString(R.string.statistics);
    }

    private void setMoneyListView(int position) {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.single_money_list, null);
        MoneyList list = view.findViewById(R.id.moneyListSingle);
        Money.DisplayParams params = list.getDisplayParams();
        params.paint = true;
        params.bold = true;
        list.setDisplayParams(params);
        layout.addView(view);
        Worker.run(this, () -> {
            List<MoneyListItem> items = null;
            if (position == 0) {
                items = Repository.getRepository().getAccountMoneyListItems();
            } else if (position == 1) {
                items = Repository.getRepository().getAccountGroupMoneyListItems();
            }
            List<MoneyListItem> itemsFinal = items == null ? new ArrayList<>() : items;
            runOnUiThread(() -> list.update(itemsFinal));
        });
    }

    private void update() {
        int position = tabs.getSelectedTabPosition();
        layout.removeAllViews();
        if (position == 0 || position == 1) {
            setMoneyListView(position);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        layout = findViewById(R.id.layoutStatistics);
        tabs = findViewById(R.id.tabLayoutStatistics);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                update();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        update();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }
}