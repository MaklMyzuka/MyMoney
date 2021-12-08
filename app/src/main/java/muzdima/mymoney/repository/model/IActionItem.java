package muzdima.mymoney.repository.model;

public interface IActionItem {
    final int TRANSACTION = 0, TRANSFER = 1;
    long getId();
    int getType();
}
