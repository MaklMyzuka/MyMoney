package muzdima.mymoney.repository.model;

public class TransferItem implements IActionItem {
    public long id;
    public long accountIdFrom;
    public String accountNameFrom;
    public long accountIdTo;
    public String accountNameTo;
    public Money.MoneyItem sumFrom;
    public Money.MoneyItem sumTo;
    public long createdAtUTC;

    public static boolean areItemsTheSame(TransferItem a, TransferItem b) {
        return a.id == b.id;
    }

    public static boolean areContentsTheSame(TransferItem a, TransferItem b) {
        return a.accountIdFrom == b.accountIdFrom &&
                a.accountIdTo == b.accountIdTo &&
                a.createdAtUTC == b.createdAtUTC &&
                a.sumFrom.equals(b.sumFrom) &&
                a.sumTo.equals(b.sumTo);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getType() {
        return IActionItem.TRANSFER;
    }

    @Override
    public Money getMoney() {
        Money money = new Money();
        Money.MoneyItem moneyFrom = new Money.MoneyItem();
        Money.MoneyItem moneyTo = new Money.MoneyItem();
        moneyFrom.currencyId = sumFrom.currencyId;
        moneyFrom.currencySymbol = sumFrom.currencySymbol;
        moneyFrom.sum10000 = -sumFrom.sum10000;
        moneyTo.currencyId = sumTo.currencyId;
        moneyTo.currencySymbol = sumTo.currencySymbol;
        moneyTo.sum10000 = sumTo.sum10000;
        money.items.add(moneyFrom);
        money.items.add(moneyTo);
        return money;
    }
}
