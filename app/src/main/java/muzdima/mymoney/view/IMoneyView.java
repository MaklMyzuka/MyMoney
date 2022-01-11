package muzdima.mymoney.view;

import muzdima.mymoney.repository.model.Money;

public interface IMoneyView {
    void setDisplayParams(Money.DisplayParams params);
    Money.DisplayParams getDisplayParams();
}
