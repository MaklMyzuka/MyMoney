package muzdima.mymoney.repository.model;

import java.util.List;

public class AccountGroupCard {
    public long id;
    public String name;
    public boolean isVisible;
    public String comment;
    public List<Long> accountIds;
}
