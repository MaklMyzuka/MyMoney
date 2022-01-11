package muzdima.mymoney.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import muzdima.mymoney.repository.model.Money;

@SuppressLint("AppCompatCustomView")
public class MoneyTextView extends TextView implements IMoneyView {

    private Context context;
    private Money.DisplayParams params;

    public MoneyTextView(Context context) {
        super(context);
        init();
    }

    public MoneyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoneyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MoneyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        context = getContext();
        params = new Money.DisplayParams(context);
    }

    public void setText() {
        setText("");
    }

    private void setHtml(String html) {
        setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT));
    }

    public void setText(Money.MoneyItem value) {
        setHtml(value.toHtmlString(context, params));
    }

    public void setText(Money value) {
        setHtml(value.toHtmlString(context, params));
    }

    public void setText(Money.MoneyItem a, Money.MoneyItem b) {
        String sa = a.toHtmlString(context, params);
        String sb = b.toHtmlString(context, params);
        setHtml(sa.equals(sb) ? sa : String.format("%s / %s", sa, sb));
    }

    @Override
    public void setDisplayParams(Money.DisplayParams params) {
        this.params = params;
    }

    @Override
    public Money.DisplayParams getDisplayParams() {
        return params;
    }
}
