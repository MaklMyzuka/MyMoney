package muzdima.mymoney.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import muzdima.mymoney.R;
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
import muzdima.mymoney.repository.model.TransactionItem;
import muzdima.mymoney.repository.model.TransferItem;
import muzdima.mymoney.utils.DateTime;

public class Repository implements IRepository {
    private static final IRepository repository = new Repository();
    private Context context;
    private SQLiteDatabase database = null;

    public static IRepository getRepository() {
        return repository;
    }

    private String getRepositoryFileName() {
        return context.getString(R.string.repository_file_name);
    }

    private String getExternalFilePath() {
        return context.getExternalFilesDir(null)
                + File.separator
                + getRepositoryFileName();
    }

    private String getInternalFilePath() {
        return context.getFilesDir()
                + File.separator
                + getRepositoryFileName();
    }

    private void copyFile(String srcPath, String dstPath) throws IOException {
        File srcFile = new File(srcPath);
        File dstFile = new File(dstPath);

        if (dstFile.getParentFile() == null) {
            throw new RuntimeException(context.getString(R.string.error_open_destination_file) + dstFile);
        }

        if (!dstFile.getParentFile().exists()) {
            if (!dstFile.getParentFile().mkdir()) {
                throw new RuntimeException(context.getString(R.string.error_create_destination_directories) + dstFile.getParentFile());
            }
        }

        try (FileChannel inChannel = new FileInputStream(srcFile).getChannel();
             FileChannel outChannel = new FileOutputStream(dstFile).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    @Override
    public ImportResult importDatabase() {
        ImportResult result = new ImportResult();
        result.exception = null;
        result.filePath = getExternalFilePath();
        try {
            database.close();
            copyFile(getExternalFilePath(), getInternalFilePath());
            initDatabase();
        } catch (Exception exception) {
            result.exception = exception;
        }
        return result;
    }

    @Override
    public ExportResult exportDatabase() {
        ExportResult result = new ExportResult();
        result.exception = null;
        result.filePath = getExternalFilePath();
        try {
            database.close();
            copyFile(getInternalFilePath(), getExternalFilePath());
            initDatabase();
        } catch (Exception exception) {
            result.exception = exception;
        }
        return result;
    }

    private void initCategory() {
        long parent_id;
        SQLiteStatement statement = database.compileStatement(context.getString(R.string.sql_init_category));
        statement.bindString(1, "Расходы");
        statement.bindNull(2);
        parent_id = statement.executeInsert();
        statement.bindLong(2, parent_id);
        statement.bindString(1, "Питание");
        statement.executeInsert();
        statement.bindString(1, "Коммуналка");
        statement.executeInsert();
        statement.bindString(1, "Медицина");
        statement.executeInsert();
        statement.bindString(1, "Авто");
        parent_id = statement.executeInsert();
        statement.bindLong(2, parent_id);
        statement.bindString(1, "Топливо");
        statement.executeInsert();
        statement.bindString(1, "Ремонты");
        statement.executeInsert();
        statement.bindString(1, "Доходы");
        statement.bindNull(2);
        parent_id = statement.executeInsert();
        statement.bindLong(2, parent_id);
        statement.bindString(1, "Пенсия");
        statement.executeInsert();
        statement.bindString(1, "Зарплата");
        statement.executeInsert();
        statement.bindString(1, "Подарки");
        statement.executeInsert();
        statement.bindString(1, "Аренда");
        statement.executeInsert();
    }

    private void execSQL(@NonNull String sql, Object[] bindArgs) {
        String[] commands = sql.split(";");
        for (String command : commands)
            if (!command.isEmpty())
                if (bindArgs == null) {
                    database.execSQL(command);
                } else {
                    database.execSQL(command, bindArgs);
                }
    }

    private void bindStatement(@NonNull SQLiteStatement statement, Object[] bindArgs) {
        if (bindArgs == null) return;
        int index = 1;
        for (Object arg : bindArgs) {
            if (arg == null) {
                statement.bindNull(index);
            } else if (arg.getClass() == String.class) {
                statement.bindString(index, (String) arg);
            } else if (arg.getClass() == Long.class) {
                statement.bindLong(index, (long) arg);
            } else if (arg.getClass() == byte[].class) {
                statement.bindBlob(index, (byte[]) arg);
            } else if (arg.getClass() == Double.class) {
                statement.bindDouble(index, (double) arg);
            } else
                throw new SQLException("cant bind " + arg.getClass().getName());
            index++;
        }
    }

    private SQLiteStatement compileStatement(@NonNull String sql, Object[] bindArgs) {
        SQLiteStatement statement = database.compileStatement(sql);
        bindStatement(statement, bindArgs);
        return statement;
    }

    private boolean checkSQL(@NonNull String sql, Object[] bindArgs) {
        try (SQLiteStatement statement = compileStatement(sql, bindArgs)) {
            return statement.simpleQueryForLong() != 0;
        }
    }

    private Cursor querySQL(@NonNull String sql, String[] args) {
        return database.rawQuery(sql, args);
    }

    private void execSQL(@StringRes int resId, Object[] bindArgs) {
        execSQL(context.getString(resId), bindArgs);
    }

    private SQLiteStatement compileStatement(@StringRes int resId, Object[] bindArgs) {
        return compileStatement(context.getString(resId), bindArgs);
    }

    private boolean checkSQL(@StringRes int resId, Object[] bindArgs) {
        return checkSQL(context.getString(resId), bindArgs);
    }

    private Cursor querySQL(@StringRes int resId, String[] args) {
        return querySQL(context.getString(resId), args);
    }

    private void execSQL(@StringRes int resId) {
        execSQL(resId, null);
    }

    private boolean checkSQLFalse(@StringRes int resId) {
        return !checkSQL(resId, null);
    }

    private Cursor querySQL(@StringRes int resId) {
        return querySQL(resId, new String[]{});
    }

    private void initData() {
        execSQL(R.string.sql_init);
        if (checkSQLFalse(R.string.sql_check_currency)) {
            execSQL(R.string.sql_init_currency);
        }
        if (checkSQLFalse(R.string.sql_check_account)) {
            execSQL(R.string.sql_init_account);
        }
        if (checkSQLFalse(R.string.sql_check_account_group)) {
            execSQL(R.string.sql_init_account_group);
        }
        if (checkSQLFalse(R.string.sql_check_category)) {
            initCategory();
        }
    }

    private void initDatabase() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        database = SQLiteDatabase.openOrCreateDatabase(getInternalFilePath(), null);
        initData();
    }

    @Override
    public void init(Context context) {
        this.context = context;
        initDatabase();
    }

    @Override
    public String getSettingsValue(String key) {
        if (!checkSQL(R.string.sql_check_settings, new Object[]{key})) {
            return null;
        }
        try (SQLiteStatement statement = compileStatement(R.string.sql_get_settings, new Object[]{key})) {
            return statement.simpleQueryForString();
        }
    }

    @Override
    public void setSettingsValue(String key, String value) {
        if (checkSQL(R.string.sql_check_settings, new Object[]{key})) {
            execSQL(R.string.sql_update_settings, new Object[]{key, value});
        } else {
            execSQL(R.string.sql_insert_settings, new Object[]{key, value});
        }
    }

    private List<SpinnerItem> getSpinnerItems(@NonNull String sql, String[] args) {
        List<SpinnerItem> items = new ArrayList<>();
        try (Cursor cursor = querySQL(sql, args)) {
            int columnId = cursor.getColumnIndex("id");
            int columnText = cursor.getColumnIndex("text");
            int columnIsVisible = cursor.getColumnIndex("is_visible");
            while (cursor.moveToNext()) {
                SpinnerItem item = new SpinnerItem();
                item.id = cursor.getLong(columnId);
                item.text = cursor.getString(columnText);
                item.isVisible = (cursor.getInt(columnIsVisible) == 1);
                items.add(item);
            }
        }
        return items;
    }

    private List<SpinnerItem> getSpinnerItems(@StringRes int resId, String[] args) {
        return getSpinnerItems(context.getString(resId), args);
    }

    private List<SpinnerItem> getSpinnerItems(@StringRes int resId) {
        return getSpinnerItems(resId, new String[]{});
    }

    private Money getMoney(@NonNull String sql, String[] args) {
        Money money = new Money();
        try (Cursor cursor = querySQL(sql, args)) {
            int columnCurrencyId = cursor.getColumnIndex("currency_id");
            int columnCurrencySymbol = cursor.getColumnIndex("currency_symbol");
            int columnSum10000 = cursor.getColumnIndex("sum10000");
            while (cursor.moveToNext()) {
                Money.MoneyItem item = new Money.MoneyItem();
                item.currencyId = cursor.getLong(columnCurrencyId);
                item.currencySymbol = cursor.getString(columnCurrencySymbol);
                item.sum10000 = cursor.getLong(columnSum10000);
                money.items.add(item);
            }
        }
        return money;
    }

    private Money getMoney(@StringRes int resId, String[] args) {
        return getMoney(context.getString(resId), args);
    }

    @Override
    public List<SpinnerItem> getCurrencySpinnerItems() {
        return getSpinnerItems(R.string.sql_get_currencies_for_spinner);
    }

    @Override
    public List<SpinnerItem> getAccountSpinnerItems() {
        return getSpinnerItems(R.string.sql_get_accounts_for_spinner);
    }

    @Override
    public List<SpinnerItem> getCategorySpinnerItems() {
        return getSpinnerItems(R.string.sql_get_categories_for_spinner);
    }

    @Override
    public List<SpinnerItem> getAccountGroupSpinnerItems() {
        return getSpinnerItems(R.string.sql_get_account_groups_for_spinner);
    }

    @Override
    public Money.MoneyItem getAccountSum(long accountId) {
        return getMoney(R.string.sql_get_account_sum, new String[]{String.valueOf(accountId)}).items.get(0);
    }

    @Override
    public Money getCategorySum(long categoryId, long fromUTC, long toUTC){
        return getMoney(R.string.sql_get_category_sum, new String[]{String.valueOf(categoryId), String.valueOf(fromUTC), String.valueOf(toUTC) });
    }

    @Override
    public Money getAccountGroupSum(long accountGroupId) {
        return getMoney(R.string.sql_get_account_group_sum, new String[]{String.valueOf(accountGroupId)});
    }

    private List<DictionaryItem> getDictionaryItems(@NonNull String sql, String[] args) {
        List<DictionaryItem> items = new ArrayList<>();
        try (Cursor cursor = querySQL(sql, args)) {
            int columnId = cursor.getColumnIndex("id");
            int columnName = cursor.getColumnIndex("name");
            int columnIsVisible = cursor.getColumnIndex("is_visible");
            int columnComment = cursor.getColumnIndex("comment");
            while (cursor.moveToNext()) {
                DictionaryItem item = new DictionaryItem();
                item.id = cursor.getLong(columnId);
                item.name = cursor.getString(columnName);
                item.isVisible = (cursor.getInt(columnIsVisible) == 1);
                item.comment = cursor.getString(columnComment);
                items.add(item);
            }
        }
        return items;
    }

    private List<DictionaryItem> getDictionaryItems(@StringRes int resId, boolean includeHidden, String[] args) {
        return getDictionaryItems(context.getString(resId).replace("%condition%", includeHidden ? "1 = 1" : "[is_visible] = 1"), args);
    }

    private List<DictionaryItem> getDictionaryItems(@StringRes int resId, boolean includeHidden) {
        return getDictionaryItems(resId, includeHidden, new String[]{});
    }

    private List<IActionItem> getActionItems(@NonNull String sql, String[] args) {
        List<IActionItem> items = new ArrayList<>();
        try (Cursor cursor = querySQL(sql, args)) {
            int columnIsTransaction = cursor.getColumnIndex("is_transaction");
            int columnId = cursor.getColumnIndex("id");
            int columnCreatedAtUTC = cursor.getColumnIndex("created_at_utc");
            int columnTransactionCategoryId = cursor.getColumnIndex("transaction_category_id");
            int columnTransactionAccountId = cursor.getColumnIndex("transaction_account_id");
            int columnTransactionSum10000 = cursor.getColumnIndex("transaction_sum10000");
            int columnTransactionProduct = cursor.getColumnIndex("transaction_product");
            int columnTransactionAccountName = cursor.getColumnIndex("transaction_account_name");
            int columnTransactionAccountCurrencyId = cursor.getColumnIndex("transaction_account_currency_id");
            int columnTransactionAccountCurrencySymbol = cursor.getColumnIndex("transaction_account_currency_symbol");
            int columnTransactionCategoryName = cursor.getColumnIndex("transaction_category_name");
            int columnTransferAccountIdFrom = cursor.getColumnIndex("transfer_account_id_from");
            int columnTransferAccountIdTo = cursor.getColumnIndex("transfer_account_id_to");
            int columnTransferSum10000From = cursor.getColumnIndex("transfer_sum10000_from");
            int columnTransferSum10000To = cursor.getColumnIndex("transfer_sum10000_to");
            int columnTransferAccountFromName = cursor.getColumnIndex("transfer_account_from_name");
            int columnTransferAccountFromCurrencyId = cursor.getColumnIndex("transfer_account_from_currency_id");
            int columnTransferAccountToName = cursor.getColumnIndex("transfer_account_to_name");
            int columnTransferAccountToCurrencyId = cursor.getColumnIndex("transfer_account_to_currency_id");
            int columnTransferAccountFromCurrencySymbol = cursor.getColumnIndex("transfer_account_from_currency_symbol");
            int columnTransferAccountToCurrencySymbol = cursor.getColumnIndex("transfer_account_to_currency_symbol");
            while (cursor.moveToNext()) {
                if (cursor.getInt(columnIsTransaction) == 1) {
                    TransactionItem item = new TransactionItem();
                    item.id = cursor.getLong(columnId);
                    item.createdAtUTC = cursor.getLong(columnCreatedAtUTC);
                    item.categoryId = cursor.getLong(columnTransactionCategoryId);
                    item.accountId = cursor.getLong(columnTransactionAccountId);
                    item.sum = new Money.MoneyItem();
                    item.sum.sum10000 = cursor.getLong(columnTransactionSum10000);
                    item.sum.currencySymbol = cursor.getString(columnTransactionAccountCurrencySymbol);
                    item.sum.currencyId = cursor.getLong(columnTransactionAccountCurrencyId);
                    item.product = cursor.getString(columnTransactionProduct);
                    item.accountName = cursor.getString(columnTransactionAccountName);
                    item.categoryName = cursor.getString(columnTransactionCategoryName);
                    items.add(item);
                } else {
                    TransferItem item = new TransferItem();
                    item.id = cursor.getLong(columnId);
                    item.createdAtUTC = cursor.getLong(columnCreatedAtUTC);
                    item.accountIdFrom = cursor.getLong(columnTransferAccountIdFrom);
                    item.accountIdTo = cursor.getLong(columnTransferAccountIdTo);
                    item.sumFrom = new Money.MoneyItem();
                    item.sumFrom.sum10000 = cursor.getLong(columnTransferSum10000From);
                    item.sumFrom.currencySymbol = cursor.getString(columnTransferAccountFromCurrencySymbol);
                    item.sumFrom.currencyId = cursor.getLong(columnTransferAccountFromCurrencyId);
                    item.sumTo = new Money.MoneyItem();
                    item.sumTo.sum10000 = cursor.getLong(columnTransferSum10000To);
                    item.sumTo.currencySymbol = cursor.getString(columnTransferAccountToCurrencySymbol);
                    item.sumTo.currencyId = cursor.getLong(columnTransferAccountToCurrencyId);
                    item.accountNameFrom = cursor.getString(columnTransferAccountFromName);
                    item.accountNameTo = cursor.getString(columnTransferAccountToName);
                    items.add(item);
                }
            }
        }
        return items;
    }

    private List<IActionItem> getActionItems(@StringRes int resId, String[] args) {
        return getActionItems(context.getString(resId), args);
    }

    private List<IActionItem> getActionItems(@StringRes int resId) {
        return getActionItems(resId, new String[]{});
    }

    @Override
    public List<DictionaryItem> getCurrencyDictionaryItems(boolean includeHidden) {
        return getDictionaryItems(R.string.sql_get_currencies_for_dictionary, includeHidden);
    }

    @Override
    public List<DictionaryItem> getAccountDictionaryItems(boolean includeHidden) {
        return getDictionaryItems(R.string.sql_get_accounts_for_dictionary, includeHidden);
    }

    @Override
    public List<DictionaryItem> getCategoryDictionaryItems(boolean includeHidden) {
        return getDictionaryItems(R.string.sql_get_categories_for_dictionary, includeHidden);
    }

    @Override
    public List<DictionaryItem> getAccountGroupDictionaryItems(boolean includeHidden) {
        return getDictionaryItems(R.string.sql_get_account_groups_for_dictionary, includeHidden);
    }

    @Override
    public List<IActionItem> getDraftActionItems() {
        return getActionItems(R.string.sql_get_draft);
    }

    @Override
    public AccountInfo getAccountInfo(long accountId) {
        AccountInfo result = new AccountInfo();
        result.id = -1;
        result.name = null;
        result.currencyId = -1;
        result.currencySymbol = null;
        try (Cursor cursor = querySQL(R.string.sql_get_account_info, new String[]{String.valueOf(accountId)})) {
            int columnId = cursor.getColumnIndex("id");
            int columnName = cursor.getColumnIndex("name");
            int columnCurrencyId = cursor.getColumnIndex("currency_id");
            int columnCurrencySymbol = cursor.getColumnIndex("currency_symbol");
            while (cursor.moveToNext()) {
                result.id = cursor.getLong(columnId);
                result.name = cursor.getString(columnName);
                result.currencyId = cursor.getLong(columnCurrencyId);
                result.currencySymbol = cursor.getString(columnCurrencySymbol);
            }
        }
        return result;
    }

    private void commitTransaction(long id) {
        execSQL(R.string.sql_commit_transaction, new Object[]{id});
    }

    private void commitTransfer(long id) {
        execSQL(R.string.sql_commit_transfer, new Object[]{id});
    }

    private void deleteTransaction(long id) {
        execSQL(R.string.sql_delete_transaction, new Object[]{id});
    }

    private void deleteTransfer(long id) {
        execSQL(R.string.sql_delete_transfer, new Object[]{id});
    }

    @Override
    public void commitActionItems(List<IActionItem> items) {
        for (IActionItem item : items) {
            switch (item.getType()) {
                case IActionItem.TRANSACTION:
                    commitTransaction(item.getId());
                    break;
                case IActionItem.TRANSFER:
                    commitTransfer(item.getId());
                    break;
            }
        }
    }

    @Override
    public void deleteActionItems(List<IActionItem> items) {
        for (IActionItem item : items) {
            switch (item.getType()) {
                case IActionItem.TRANSACTION:
                    deleteTransaction(item.getId());
                    break;
                case IActionItem.TRANSFER:
                    deleteTransfer(item.getId());
                    break;
            }
        }
    }

    @Override
    public void insertTransaction(long categoryId, long accountId, long sum10000, String product, long createdAtUTC) {
        execSQL(R.string.sql_insert_transaction, new Object[]{categoryId, accountId, sum10000, product, createdAtUTC});
    }

    @Override
    public void insertTransfer(long accountIdFrom, long accountIdTo, long sum10000From, long sum10000To, long createdAtUTC) {
        execSQL(R.string.sql_insert_transfer, new Object[]{accountIdFrom, accountIdTo, sum10000From, sum10000To, createdAtUTC});
    }

    @Override
    public void updateTransaction(long transactionId, long categoryId, long accountId, long sum10000, String product, long createdAtUTC) {
        execSQL(R.string.sql_update_transaction, new Object[]{transactionId, categoryId, accountId, sum10000, product, createdAtUTC});
    }

    @Override
    public void updateTransfer(long transferId, long accountIdFrom, long accountIdTo, long sum10000From, long sum10000To, long createdAtUTC) {
        execSQL(R.string.sql_update_transfer, new Object[]{transferId, accountIdFrom, accountIdTo, sum10000From, sum10000To, createdAtUTC});
    }

    @Override
    public List<IActionItem> getActionItems(long fromUTC, long toUTC) {
        return getActionItems(R.string.sql_get_actions_by_time, new String[]{String.valueOf(fromUTC), String.valueOf(toUTC)});
    }

    @Override
    public CurrencyCard getCurrencyCard(long currencyId) {
        CurrencyCard result = new CurrencyCard();
        result.id = -1;
        result.name = null;
        result.symbol = null;
        result.isVisible = false;
        result.comment = null;
        try (Cursor cursor = querySQL(R.string.sql_get_currency_card, new String[]{String.valueOf(currencyId)})) {
            int columnId = cursor.getColumnIndex("id");
            int columnName = cursor.getColumnIndex("name");
            int columnSymbol = cursor.getColumnIndex("symbol");
            int columnIsVisible = cursor.getColumnIndex("is_visible");
            int columnComment = cursor.getColumnIndex("comment");
            while (cursor.moveToNext()) {
                result.id = cursor.getLong(columnId);
                result.name = cursor.getString(columnName);
                result.symbol = cursor.getString(columnSymbol);
                result.isVisible = (cursor.getInt(columnIsVisible) == 1);
                result.comment = cursor.getString(columnComment);
            }
        }
        return result;
    }

    @Override
    public AccountCard getAccountCard(long accountId) {
        AccountCard result = new AccountCard();
        result.id = -1;
        result.name = null;
        result.currencyId = -1;
        result.sum10000 = 0;
        result.isVisible = false;
        result.comment = null;
        result.accountGroupIds = new ArrayList<>();
        try (Cursor cursor = querySQL(R.string.sql_get_account_card, new String[]{String.valueOf(accountId)})) {
            int columnId = cursor.getColumnIndex("id");
            int columnName = cursor.getColumnIndex("name");
            int columnCurrencyId = cursor.getColumnIndex("currency_id");
            int columnSum10000 = cursor.getColumnIndex("sum10000");
            int columnIsVisible = cursor.getColumnIndex("is_visible");
            int columnComment = cursor.getColumnIndex("comment");
            while (cursor.moveToNext()) {
                result.id = cursor.getLong(columnId);
                result.name = cursor.getString(columnName);
                result.currencyId = cursor.getLong(columnCurrencyId);
                result.sum10000 = cursor.getLong(columnSum10000);
                result.isVisible = (cursor.getInt(columnIsVisible) == 1);
                result.comment = cursor.getString(columnComment);
            }
        }
        try (Cursor cursor = querySQL(R.string.sql_get_account_groups_by_account, new String[]{String.valueOf(accountId)})) {
            int columnAccountGroupId = cursor.getColumnIndex("account_group_id");
            while (cursor.moveToNext()) {
                result.accountGroupIds.add(cursor.getLong(columnAccountGroupId));
            }
        }
        return result;
    }

    @Override
    public CategoryCard getCategoryCard(long categoryId) {
        CategoryCard result = new CategoryCard();
        result.id = -1;
        result.name = null;
        result.parentId = null;
        result.isVisible = false;
        result.comment = null;
        try (Cursor cursor = querySQL(R.string.sql_get_category_card, new String[]{String.valueOf(categoryId)})) {
            int columnId = cursor.getColumnIndex("id");
            int columnName = cursor.getColumnIndex("name");
            int columnParentId = cursor.getColumnIndex("parent_id");
            int columnIsVisible = cursor.getColumnIndex("is_visible");
            int columnComment = cursor.getColumnIndex("comment");
            while (cursor.moveToNext()) {
                result.id = cursor.getLong(columnId);
                result.name = cursor.getString(columnName);
                result.parentId = cursor.isNull(columnParentId) ? null : cursor.getLong(columnParentId);
                result.isVisible = (cursor.getInt(columnIsVisible) == 1);
                result.comment = cursor.getString(columnComment);
            }
        }
        return result;
    }

    @Override
    public AccountGroupCard getAccountGroupCard(long accountGroupId) {
        AccountGroupCard result = new AccountGroupCard();
        result.id = -1;
        result.name = null;
        result.isVisible = false;
        result.comment = null;
        result.accountIds = new ArrayList<>();
        try (Cursor cursor = querySQL(R.string.sql_get_account_group_card, new String[]{String.valueOf(accountGroupId)})) {
            int columnId = cursor.getColumnIndex("id");
            int columnName = cursor.getColumnIndex("name");
            int columnIsVisible = cursor.getColumnIndex("is_visible");
            int columnComment = cursor.getColumnIndex("comment");
            while (cursor.moveToNext()) {
                result.id = cursor.getLong(columnId);
                result.name = cursor.getString(columnName);
                result.isVisible = (cursor.getInt(columnIsVisible) == 1);
                result.comment = cursor.getString(columnComment);
            }
        }
        try (Cursor cursor = querySQL(R.string.sql_get_accounts_by_account_group, new String[]{String.valueOf(accountGroupId)})) {
            int columnAccountId = cursor.getColumnIndex("account_id");
            while (cursor.moveToNext()) {
                result.accountIds.add(cursor.getLong(columnAccountId));
            }
        }
        return result;
    }

    private void insertAccountGroupAccount(long accountGroupId, long accountId) {
        execSQL(R.string.sql_insert_account_group_account, new Object[]{accountGroupId, accountId});
    }

    @Override
    public void insertCurrency(String name, String symbol, boolean isVisible, String comment) {
        execSQL(R.string.sql_insert_currency, new Object[]{name, symbol, isVisible, comment});
    }

    @Override
    public void insertAccount(String name, long currencyId, long sum10000, boolean isVisible, String comment, List<Long> accountGroupIds) {
        execSQL(R.string.sql_insert_account, new Object[]{name, currencyId, sum10000, isVisible, comment});
        long accountId = -1;
        try (Cursor cursor = querySQL(R.string.sql_last_insert_rowid)) {
            while (cursor.moveToNext()) {
                accountId = cursor.getInt(0);
            }
        }
        for (Long accountGroupId : accountGroupIds) {
            insertAccountGroupAccount(accountGroupId, accountId);
        }
    }

    @Override
    public void insertCategory(String name, Long parentId, boolean isVisible, String comment) {
        execSQL(R.string.sql_insert_category, new Object[]{name, parentId, isVisible, comment});
    }

    @Override
    public void insertAccountGroup(String name, boolean isVisible, String comment, List<Long> accountIds) {
        execSQL(R.string.sql_insert_account_group, new Object[]{name, isVisible, comment});
        long accountGroupId = -1;
        try (Cursor cursor = querySQL(R.string.sql_last_insert_rowid)) {
            while (cursor.moveToNext()) {
                accountGroupId = cursor.getInt(0);
            }
        }
        for (Long accountId : accountIds) {
            insertAccountGroupAccount(accountGroupId, accountId);
        }
    }

    @Override
    public void updateCurrency(long currencyId, String name, String symbol, boolean isVisible, String comment) {
        execSQL(R.string.sql_update_currency, new Object[]{currencyId, name, symbol, isVisible, comment});
        initData();
    }

    @Override
    public void updateAccount(long accountId, String name, long currencyId, long sum10000, boolean isVisible, String comment, List<Long> accountGroupIds) {
        execSQL(R.string.sql_update_account, new Object[]{accountId, name, currencyId, sum10000, isVisible, comment});
        execSQL(R.string.sql_delete_account_group_account_by_account, new Object[]{accountId});
        for (Long accountGroupId : accountGroupIds) {
            insertAccountGroupAccount(accountGroupId, accountId);
        }
        initData();
    }

    private boolean checkForLoopsBeforeUpdateCategory(long categoryId, Long parentId) {
        return checkSQL(R.string.sql_check_update_category_loop, new Object[]{categoryId, parentId});
    }

    @Override
    public boolean updateCategory(long categoryId, String name, Long parentId, boolean isVisible, String comment) {
        if (!checkForLoopsBeforeUpdateCategory(categoryId, parentId)) {
            return false;
        }
        execSQL(R.string.sql_update_category, new Object[]{categoryId, name, parentId, isVisible, comment});
        initData();
        return true;
    }

    @Override
    public void updateAccountGroup(long accountGroupId, String name, boolean isVisible, String comment, List<Long> accountIds) {
        execSQL(R.string.sql_update_account_group, new Object[]{accountGroupId, name, isVisible, comment});
        execSQL(R.string.sql_delete_account_group_account_by_account_group, new Object[]{accountGroupId});
        for (Long accountId : accountIds) {
            insertAccountGroupAccount(accountGroupId, accountId);
        }
        initData();
    }

    @Override
    public boolean deleteCurrency(long currencyId) {
        try {
            execSQL(R.string.sql_delete_currency, new Object[]{currencyId});
        } catch (Exception exception) {
            return false;
        }
        initData();
        return true;
    }

    @Override
    public boolean deleteAccount(long accountId) {
        try {
            execSQL(R.string.sql_delete_account, new Object[]{accountId});
        } catch (Exception exception) {
            return false;
        }
        initData();
        return true;
    }

    @Override
    public boolean deleteCategory(long categoryId) {
        try {
            execSQL(R.string.sql_delete_category, new Object[]{categoryId});
        } catch (Exception exception) {
            return false;
        }
        initData();
        return true;
    }

    @Override
    public boolean deleteAccountGroup(long accountGroupId) {
        try {
            execSQL(R.string.sql_delete_account_group, new Object[]{accountGroupId});
        } catch (Exception exception) {
            return false;
        }
        initData();
        return true;
    }
}
