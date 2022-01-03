package muzdima.mymoney.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.utils.ActivitySolver;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.Worker;

public class StatisticsMonthlyByDays extends LinearLayout {
    private final List<SpinnerItem> categoryItems = new ArrayList<>();
    private final List<SpinnerItem> currencyItems = new ArrayList<>();
    private Long currencyId = null;
    private boolean currencyInit = false;
    private LimitLine averageLimitLine = null;
    private HistorySpinner categorySpinner;
    private HistorySpinner currencySpinner;
    private BarChart barChart;
    private int year;
    private int month;

    public StatisticsMonthlyByDays(Context context) {
        super(context);
    }

    public StatisticsMonthlyByDays(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StatisticsMonthlyByDays(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StatisticsMonthlyByDays(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(int year, int month) {
        this.year = year;
        this.month = month;
        inflate(getContext(), R.layout.statistics_monthly_by_days, this);
        categorySpinner = findViewById(R.id.categoryStatisticsMonthlyByDays);
        currencySpinner = findViewById(R.id.currencyStatisticsMonthlyByDays);
        barChart = findViewById(R.id.barChartStatisticsMonthlyByDays);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setDrawValueAboveBar(true);
        barChart.setHighlightFullBarEnabled(true);
        barChart.setDrawBorders(true);
        barChart.setNoDataText(getContext().getString(R.string.no_data));
        barChart.setDragDecelerationEnabled(false);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);
        barChart.getLegend().setEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setValueFormatter(
                new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        return String.valueOf((int) value);
                    }
                }
        );
        YAxis yLeftAxis = barChart.getAxisLeft();
        yLeftAxis.setDrawLabels(true);
        yLeftAxis.setDrawAxisLine(true);
        yLeftAxis.setDrawGridLines(true);
        yLeftAxis.setDrawZeroLine(true);
        yLeftAxis.setValueFormatter(
                new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        return String.valueOf(value);
                    }
                }
        );
        barChart.getAxisRight().setEnabled(false);
        categorySpinner.setOnItemSelectedListener(id -> updateCurrency());
        currencySpinner.setOnItemSelectedListener(id -> {
            if (currencyId == null) update();
        });
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            List<SpinnerItem> categorySpinnerItems = Repository.getRepository().getCategorySpinnerItems();
            List<SpinnerItem> currencySpinnerItems = Repository.getRepository().getCurrencySpinnerItems();
            activity.runOnUiThread(() -> {
                categoryItems.clear();
                currencyItems.clear();
                categoryItems.addAll(categorySpinnerItems);
                currencyItems.addAll(currencySpinnerItems);
                currencyId = null;
                if (currencySpinnerItems.size() == 1) currencyId = categorySpinnerItems.get(0).id;
                if (currencySpinnerItems.isEmpty()) currencyId = -1L;
                categorySpinner.init("history_category_selector_on_statistics_monthly_by_days", categoryItems, null);

            });
        });
    }

    public void update(int year, int month) {
        this.year = year;
        this.month = month;
        updateCurrency();
    }

    private void updateCurrency() {
        long categoryId = categorySpinner.getSelectedId();
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            List<SpinnerItem> items = Repository.getRepository().getCurrencySpinnerItemsByCategory(
                    categoryId,
                    DateTime.getMonthStartUTCFromLocal(year, month),
                    DateTime.getMonthEndUTCFromLocal(year, month));
            activity.runOnUiThread(() -> {
                currencyItems.clear();
                currencyItems.addAll(items);
                currencyId = null;
                if (items.size() == 1) currencyId = items.get(0).id;
                if (items.isEmpty()) currencyId = -1L;
                if (currencyId == null) {
                    if (currencyInit) {
                        currencySpinner.update();
                    } else {
                        currencyInit = true;
                        currencySpinner.init("history_currency_selector_on_statistics_monthly_by_days", currencyItems, null);
                    }
                } else {
                    update();
                }
                currencySpinner.setVisibility(currencyItems.size() < 2 ? GONE : VISIBLE);
            });
        });
    }

    private void setAverageLimitLine(List<Money.MoneyItem> items) {
        if (averageLimitLine != null) barChart.getAxisLeft().removeLimitLine(averageLimitLine);
        long total = 0;
        for (Money.MoneyItem item : items) {
            total += item.sum10000;
        }
        averageLimitLine = new LimitLine((float) (total / 10000.0d / items.stream().filter(i -> i.sum10000 != 0).count()), getContext().getString(R.string.month_average));
        barChart.getAxisLeft().addLimitLine(averageLimitLine);
    }

    private void setBarChart(List<Money.MoneyItem> items) {
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        float min = 0;
        float max = 0;
        int x = 1;
        for (Money.MoneyItem item : items) {
            if (item.sum10000 != 0) {
                float value = (float) (item.sum10000 / 10000.0d);
                min = Math.min(min, value);
                max = Math.max(max, value);
                entries.add(new BarEntry(x, value));
                colors.add(getContext().getColor(item.sum10000 < 0 ? R.color.bar_negative : R.color.bar_positive));
            }
            x++;
        }
        BarDataSet dataSet = new BarDataSet(entries, getContext().getString(R.string.statistics_monthly));
        dataSet.setColors(colors);
        barChart.setData(new BarData(dataSet));
        int days = DateTime.getLengthOfMonth(year, month);
        XAxis xAxis = barChart.getXAxis();
        xAxis.mAxisMinimum = 1f;
        xAxis.mAxisMaximum = days;
        xAxis.setLabelCount(15);
        YAxis yLeftAxis = barChart.getAxisLeft();
        yLeftAxis.mAxisMinimum = Math.min(0, min);
        yLeftAxis.mAxisMaximum = Math.max(0, max);
        barChart.setVisibleXRange(1, days);
        barChart.invalidate();
    }

    private void update() {
        int year = this.year;
        int month = this.month;
        long currencyId = this.currencyId == null ? currencySpinner.getSelectedId() : this.currencyId;
        long categoryId = categorySpinner.getSelectedId();
        Activity activity = ActivitySolver.getActivity(getContext());
        Worker.run(activity, () -> {
            List<Money.MoneyItem> items = Repository.getRepository().getCategorySumMonthlyByDays(categoryId, currencyId, year, month);
            activity.runOnUiThread(() -> {
                setAverageLimitLine(items);
                setBarChart(items);
            });
        });
    }
}
