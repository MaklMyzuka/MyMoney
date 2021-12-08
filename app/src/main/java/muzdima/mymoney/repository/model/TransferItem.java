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
}
