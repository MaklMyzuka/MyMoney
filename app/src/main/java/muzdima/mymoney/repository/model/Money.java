package muzdima.mymoney.repository.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            if (negative) {
                s.append("-");
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
        if (s.length() == 0) s.append("0");
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
    }
}
