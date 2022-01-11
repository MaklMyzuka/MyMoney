package muzdima.mymoney.repository.model;

public interface IActionItem {
    int TRANSACTION = 0, TRANSFER = 1;
    long getId();
    int getType();
    Money getMoney();
}
