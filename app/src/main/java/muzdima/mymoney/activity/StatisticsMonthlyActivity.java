package muzdima.mymoney.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.MoneyListItem;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.view.MoneyList;
import muzdima.mymoney.view.StatisticsMonthlyByDays;

public class StatisticsMonthlyActivity extends MenuActivity {

    private ViewGroup layout;
    private TabLayout tabs;
    private TextView textViewDate;
    private int year;
    private int month;
    private IObserver observer;

    private void setMoneyListView(int position) {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.single_money_list, null);
        MoneyList list = view.findViewById(R.id.moneyListSingle);
        Money.DisplayParams params = list.getDisplayParams();
        params.paint = true;
        params.bold = true;
        if (position == 0 || position == 2) params.plus = true;
        list.setDisplayParams(params);
        layout.addView(view);
        observer = () ->
                Worker.run(this, () -> {
                    List<MoneyListItem> items = null;
                    if (position == 0) {
                        items = Repository.getRepository().getAccountMoneyListItems(fromUTC(), toUTC());
                    } else if (position == 1) {
                        items = Repository.getRepository().getCategoryMoneyListItems(fromUTC(), toUTC());
                    } else if (position == 2) {
                        items = Repository.getRepository().getAccountGroupMoneyListItems(fromUTC(), toUTC());
                    }
                    List<MoneyListItem> itemsFinal = items == null ? new ArrayList<>() : items;
                    runOnUiThread(() -> list.update(itemsFinal));
                });
        observer.onNotify();
    }

    private long fromUTC() {
        return DateTime.getMonthStartUTC(year, month);
    }

    private long toUTC() {
        return DateTime.getMonthEndUTC(year, month);
    }

    private void update() {
        int position = tabs.getSelectedTabPosition();
        layout.removeAllViews();
        observer = null;
        if (position == 0 || position == 1 || position == 2) {
            setMoneyListView(position);
        } else if (position == 3) {
            @SuppressLint("InflateParams")
            View view = getLayoutInflater().inflate(R.layout.single_statistics_monthly_by_days, null);
            StatisticsMonthlyByDays statistics = view.findViewById(R.id.statisticsMonthlyByDaysSingle);
            statistics.init(year, month);
            layout.addView(view);
            observer = () -> statistics.update(year, month);
        }
    }

    private void updateText() {
        textViewDate.setText(String.format("%s%s.%s", month / 10, month % 10, year));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_monthly);
        layout = findViewById(R.id.layoutStatisticsMonthly);
        textViewDate = findViewById(R.id.textViewDateStatisticsMonthly);
        year = DateTime.getCurrentYear();
        month = DateTime.getCurrentMonth();
        updateText();
        tabs = findViewById(R.id.tabLayoutStatisticsMonthly);
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
        findViewById(R.id.buttonNextStatisticsMonthly).setOnClickListener(view -> {
            month++;
            if (month == 13) {
                month = 1;
                year++;
            }
            if (year == 10000) year = 9999;
            updateText();
            if (observer != null) observer.onNotify();
        });
        findViewById(R.id.buttonPrevStatisticsMonthly).setOnClickListener(view -> {
            month--;
            if (month == 0) {
                month = 12;
                year--;
            }
            if (year == 999) year = 1000;
            updateText();
            if (observer != null) observer.onNotify();
        });
        update();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private interface IObserver {
        void onNotify();
    }
}
