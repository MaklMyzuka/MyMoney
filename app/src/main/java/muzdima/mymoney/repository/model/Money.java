package muzdima.mymoney.repository.model;

import android.content.Context;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import muzdima.mymoney.R;
import muzdima.mymoney.utils.ConfigurationPreferences;

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

    private static String insertThousands(String s, String dividerReverse) {
        StringBuilder result = new StringBuilder();
        int n = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (n == 3) {
                n = 0;
                result.append(dividerReverse);
            }
            result.append(s.charAt(i));
            n++;
        }
        return result.reverse().toString();
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

    public String toHtmlString(Context context, DisplayParams params) {
        ConfigurationPreferences.FormatPreferences preferences = ConfigurationPreferences.getFormatPreferences(context);
        int decimals;
        switch (preferences.formatNumberDecimal) {
            case Point2:
            case Comma2:
            default:
                decimals = 2;
                break;
            case Point1:
            case Comma1:
                decimals = 1;
                break;
            case Zero:
                decimals = 0;
                break;
        }
        long decimalsMod = 10000;
        for (int i = 0; i < decimals; i++) decimalsMod /= 10;
        long decimalsDiv = 1;
        for (int i = 0; i < decimals; i++) decimalsDiv *= 10;
        String spaceHtml = "&nbsp;";
        String thousand;
        switch (preferences.formatNumberThousand) {
            case Space:
            default:
                thousand = spaceHtml;
                break;
            case None:
                thousand = "";
                break;
            case Comma:
                thousand = ",";
                break;
            case Delta:
                thousand = "`";
                break;
        }
        String thousandReverse = new StringBuilder(thousand).reverse().toString();
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (MoneyItem item : items) {
            long sum = item.sum10000;
            boolean negative = false;
            if (sum < 0) {
                negative = true;
                sum = -sum;
            }
            if (sum % decimalsMod >= decimalsMod / 2) {
                sum /= decimalsMod;
                sum++;
            } else {
                sum /= decimalsMod;
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
            long sumInteger = sum / decimalsDiv;
            s.append(insertThousands(String.valueOf(sumInteger), thousandReverse));
            long sumDecimal = sum % decimalsDiv;
            if (sumDecimal != 0) {
                s.append(".");
                s.append(leftPad0(String.valueOf(sumDecimal), decimals));
            }
            s.append(spaceHtml);
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

        public String toHtmlString(Context context, DisplayParams params) {
            Money money = new Money();
            money.items.add(this);
            return money.toHtmlString(context, params);
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
