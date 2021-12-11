package muzdima.mymoney.view;

import muzdima.mymoney.repository.model.Money;

public interface IMoneyVIew {
    void setDisplayParams(Money.DisplayParams params);
    Money.DisplayParams getDisplayParams();
}
