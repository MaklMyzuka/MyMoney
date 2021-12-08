package muzdima.mymoney.repository.model;

import java.util.List;

public class AccountCard {
    public long id;
    public String name;
    public long currencyId;
    public long sum10000;
    public boolean isVisible;
    public String comment;
    public List<Long> accountGroupIds;
}
