package muzdima.mymoney.repository.model;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import muzdima.mymoney.R;

public class Money {
    public List<MoneyItem> items = new ArrayList<>();

    private static String leftPad0(String s, int length) {
        if (s.length() >= length) return s;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < length - s.length(); i++) {
            b.append('0');
        }
        b.append(s);
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return items.containsAll(money.items) && money.items.containsAll(items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @NonNull
    @Override
    public String toString() {
        return toHtmlString(new DisplayParams());
    }

    public String toHtmlString(DisplayParams params) {
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (MoneyItem item : items) {
            long sum = item.sum10000;
            boolean negative = false;
            if (sum < 0) {
                negative = true;
                sum = -sum;
            }
            if (sum % 100 >= 50) {
                sum /= 100;
                sum++;
            } else {
                sum /= 100;
            }
            if (sum == 0) {
                continue;
            }
            if (!first) {
                s.append(", ");
            }
            if (params.paint) {
                s.append(String.format("<font color=\"%s\">", params.getColorHtml(item.sum10000)));
            }
            if (params.bold) {
                s.append("<b>");
            }
            if (negative) {
                s.append("-");
            }
            if (!negative && sum > 0 && params.plus) {
                s.append("+");
            }
            if (sum / 100000 != 0) {
                s.append(sum / 100000);
                s.append(" ");
                sum %= 100000;
                s.append(leftPad0(String.valueOf(sum / 100), 3));
            } else {
                s.append(sum / 100);
            }
            sum %= 100;
            if (sum != 0) {
                s.append(".");
                s.append(leftPad0(String.valueOf(sum), 2));
            }
            s.append(" ");
            s.append(item.currencySymbol);

            first = false;
        }
        if (s.length() == 0) {
            if (params.paint) {
                s.append(String.format("<font color=\"%s\">", params.getColorHtml(0)));
            }
            if (params.bold) {
                s.append("<b>");
            }
            s.append("0");
            if (params.bold) {
                s.append("</b>");
            }
            if (params.paint) {
                s.append("</font>");
            }
        }
        return s.toString();
    }

    public static class MoneyItem {
        public long currencyId;
        public String currencySymbol;
        public long sum10000;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MoneyItem moneyItem = (MoneyItem) o;
            return currencyId == moneyItem.currencyId && sum10000 == moneyItem.sum10000;
        }

        @Override
        public int hashCode() {
            return Objects.hash(currencyId, sum10000);
        }

        @NonNull
        @Override
        public String toString() {
            Money money = new Money();
            money.items.add(this);
            return money.toString();
        }

        public String toHtmlString(DisplayParams params) {
            Money money = new Money();
            money.items.add(this);
            return money.toHtmlString(params);
        }
    }

    public static class DisplayParams {
        public boolean bold = false;
        public boolean paint = false;
        public boolean plus = false;
        @ColorInt
        public int colorPositive;
        @ColorInt
        public int colorNegative;
        @ColorInt
        public int colorZero;

        public DisplayParams() {
            colorPositive = Color.BLACK;
            colorNegative = Color.BLACK;
            colorZero = Color.BLACK;
        }

        public DisplayParams(Context context) {
            colorPositive = context.getColor(R.color.sum_positive);
            colorNegative = context.getColor(R.color.sum_negative);
            colorZero = context.getColor(R.color.sum_zero);
        }

        private int getColor(long value) {
            if (value > 0) return colorPositive;
            if (value < 0) return colorNegative;
            return colorZero;
        }

        private String getColorHtml(long value) {
            return String.format("#%s", String.format("%X", getColor(value)).substring(2));
        }
    }
}
