package muzdima.mymoney.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.MoneyListItem;
import muzdima.mymoney.utils.ActivitySolver;

public class MoneyList extends LinearLayout implements IMoneyVIew {
    private LinearLayout layout;
    private Money.DisplayParams params;

    public MoneyList(Context context) {
        super(context);
        init();
    }

    public MoneyList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoneyList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MoneyList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.money_list, this);
        layout = findViewById(R.id.layoutMoneyList);
        params = new Money.DisplayParams(getContext());
    }

    @Override
    public void setDisplayParams(Money.DisplayParams params) {
        this.params = params;
    }

    @Override
    public Money.DisplayParams getDisplayParams() {
        return params;
    }

    // DON'T CHANGE CONTENTS OF LIST items
    // FOR CHANGE, PASS NEW LIST INTO update(items)
    public void update(List<MoneyListItem> items) {
        layout.removeAllViews();
        Activity activity = ActivitySolver.getActivity(getContext());
        for (MoneyListItem item : items) {
            @SuppressLint("InflateParams")
            View itemView = activity.getLayoutInflater().inflate(R.layout.money_list_item, null);
            ((TextView) itemView.findViewById(R.id.textMoneyListItem)).setText(item.text);
            MoneyTextView moneyView = itemView.findViewById(R.id.moneyMoneyListItem);
            moneyView.setDisplayParams(params);
            moneyView.setText(item.money);
            layout.addView(itemView);
        }
    }
}
