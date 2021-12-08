package muzdima.mymoney.repository;

import android.content.Context;

import java.util.List;

import muzdima.mymoney.repository.model.AccountCard;
import muzdima.mymoney.repository.model.AccountGroupCard;
import muzdima.mymoney.repository.model.AccountInfo;
import muzdima.mymoney.repository.model.CategoryCard;
import muzdima.mymoney.repository.model.CurrencyCard;
import muzdima.mymoney.repository.model.DictionaryItem;
import muzdima.mymoney.repository.model.ExportResult;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.repository.model.ImportResult;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.SpinnerItem;

public interface IRepository {
    void init(Context context);
    ImportResult importDatabase();
    ExportResult exportDatabase();
    String getSettingsValue(String key);
    void setSettingsValue(String key, String value);
    List<SpinnerItem> getCurrencySpinnerItems();
    List<SpinnerItem> getAccountSpinnerItems();
    List<SpinnerItem> getCategorySpinnerItems();
    List<SpinnerItem> getAccountGroupSpinnerItems();
    Money.MoneyItem getAccountSum(long accountId);
    Money getCategorySum(long categoryId, long fromUTC, long toUTC);
    Money getAccountGroupSum(long accountGroupId);
    List<DictionaryItem> getCurrencyDictionaryItems(boolean includeHidden);
    List<DictionaryItem> getAccountDictionaryItems(boolean includeHidden);
    List<DictionaryItem> getCategoryDictionaryItems(boolean includeHidden);
    List<DictionaryItem> getAccountGroupDictionaryItems(boolean includeHidden);
    List<IActionItem> getDraftActionItems();
    AccountInfo getAccountInfo(long accountId);
    void commitActionItems(List<IActionItem> items);
    void deleteActionItems(List<IActionItem> items);
    void insertTransaction(long categoryId, long accountId, long sum10000, String product, long createdAtUTC);
    void insertTransfer(long accountIdFrom, long accountIdTo, long sum10000From, long sum10000To, long createdAtUTC);
    void updateTransaction(long transactionId, long categoryId, long accountId, long sum10000, String product, long createdAtUTC);
    void updateTransfer(long transferId, long accountIdFrom, long accountIdTo, long sum10000From, long sum10000To, long createdAtUTC);
    List<IActionItem> getActionItems(long fromUTC, long toUTC);
    CurrencyCard getCurrencyCard(long currencyId);
    AccountCard getAccountCard(long accountId);
    CategoryCard getCategoryCard(long categoryId);
    AccountGroupCard getAccountGroupCard(long accountGroupId);
    void insertCurrency(String name, String symbol, boolean isVisible, String comment);
    void insertAccount(String name, long currencyId, long sum10000, boolean isVisible, String comment, List<Long> accountGroupIds);
    void insertCategory(String name, Long parentId, boolean isVisible, String comment);
    void insertAccountGroup(String name, boolean isVisible, String comment, List<Long> accountIds);
    void updateCurrency(long currencyId, String name, String symbol, boolean isVisible, String comment);
    void updateAccount(long accountId, String name, long currencyId, long sum10000, boolean isVisible, String comment, List<Long> accountGroupIds);
    boolean updateCategory(long categoryId, String name, Long parentId, boolean isVisible, String comment);
    void updateAccountGroup(long accountGroupId, String name, boolean isVisible, String comment, List<Long> accountIds);
    boolean deleteCurrency(long currencyId);
    boolean deleteAccount(long accountId);
    boolean deleteCategory(long categoryId);
    boolean deleteAccountGroup(long accountGroupId);
}
