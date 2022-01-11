package muzdima.mymoney.repository.model;

public class TransactionItem implements IActionItem {
    public long id;
    public long categoryId;
    public String categoryName;
    public long accountId;
    public String accountName;
    public Money.MoneyItem sum;
    public String product;
    public long createdAtUTC;

    public static boolean areItemsTheSame(TransactionItem a, TransactionItem b) {
        return a.id == b.id;
    }

    public static boolean areContentsTheSame(TransactionItem a, TransactionItem b) {
        return a.categoryId == b.categoryId &&
                a.accountId == b.accountId &&
                a.createdAtUTC == b.createdAtUTC &&
                a.product.equals(b.product) &&
                a.sum.equals(b.sum);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getType() {
        return IActionItem.TRANSACTION;
    }

    @Override
    public Money getMoney() {
        Money money = new Money();
        money.items.add(sum);
        return money;
    }
}
