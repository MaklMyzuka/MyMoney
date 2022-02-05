package muzdima.mymoney.repository;

import static android.database.sqlite.SQLiteDatabase.CREATE_IF_NECESSARY;
import static android.database.sqlite.SQLiteDatabase.NO_LOCALIZED_COLLATORS;
import static android.database.sqlite.SQLiteDatabase.OPEN_READWRITE;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import muzdima.mymoney.BuildConfig;
import muzdima.mymoney.R;
import muzdima.mymoney.repository.model.AccountCard;
import muzdima.mymoney.repository.model.AccountGroupCard;
import muzdima.mymoney.repository.model.AccountInfo;
import muzdima.mymoney.repository.model.CategoryCard;
import muzdima.mymoney.repository.model.CurrencyCard;
import muzdima.mymoney.repository.model.DictionaryItem;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.MoneyListItem;
import muzdima.mymoney.repository.model.SpinnerItem;
import muzdima.mymoney.repository.model.TransactionItem;
import muzdima.mymoney.repository.model.TransferItem;
import muzdima.mymoney.utils.DateTime;
import muzdima.mymoney.utils.ErrorDialog;
import muzdima.mymoney.utils.Excel;
import muzdima.mymoney.utils.ImportExport;
import muzdima.mymoney.utils.Restart;

public class Repository implements IRepository {
    private static final IRepository repository = new Repository();
    private static final TableDef[] tables = {
            new TableDef("currency", new ColumnDef[]{
                    new ColumnDef("id", "INTEGER", true, 1),
                    new ColumnDef("name", "TEXT", true, 0),
                    new ColumnDef("symbol", "TEXT", true, 0),
                    new ColumnDef("is_visible", "INTEGER", true, 0),
                    new ColumnDef("comment", "TEXT", false, 0),
            }),
            new TableDef("account", new ColumnDef[]{
                    new ColumnDef("id", "INTEGER", true, 1),
                    new ColumnDef("name", "TEXT", true, 0),
                    new ColumnDef("currency_id", "INTEGER", true, 0),
                    new ColumnDef("sum10000", "INTEGER", true, 0),
                    new ColumnDef("is_visible", "INTEGER", true, 0),
                    new ColumnDef("comment", "TEXT", false, 0),
            }),
            new TableDef("account_group", new ColumnDef[]{
                    new ColumnDef("id", "INTEGER", true, 1),
                    new ColumnDef("name", "TEXT", true, 0),
                    new ColumnDef("is_visible", "INTEGER", true, 0),
                    new ColumnDef("comment", "TEXT", false, 0),
            }),
            new TableDef("account_group_accounts", new ColumnDef[]{
                    new ColumnDef("account_group_id", "INTEGER", true, 1),
                    new ColumnDef("account_id", "INTEGER", true, 2),
            }),
            new TableDef("category", new ColumnDef[]{
                    new ColumnDef("id", "INTEGER", true, 1),
                    new ColumnDef("name", "TEXT", true, 0),
                    new ColumnDef("parent_id", "INTEGER", false, 0),
                    new ColumnDef("is_visible", "INTEGER", true, 0),
                    new ColumnDef("comment", "TEXT", false, 0),
            }),
            new TableDef("transfer", new ColumnDef[]{
                    new ColumnDef("id", "INTEGER", true, 1),
                    new ColumnDef("account_id_from", "INTEGER", true, 0),
                    new ColumnDef("account_id_to", "INTEGER", true, 0),
                    new ColumnDef("sum10000_from", "INTEGER", true, 0),
                    new ColumnDef("sum10000_to", "INTEGER", true, 0),
                    new ColumnDef("created_at_utc", "INTEGER", true, 0),
                    new ColumnDef("is_committed", "INTEGER", true, 0),
            }),
            new TableDef("transaction", new ColumnDef[]{
                    new ColumnDef("id", "INTEGER", true, 1),
                    new ColumnDef("category_id", "INTEGER", true, 0),
                    new ColumnDef("account_id", "INTEGER", true, 0),
                    new ColumnDef("sum10000", "INTEGER", true, 0),
                    new ColumnDef("product", "TEXT", false, 0),
                    new ColumnDef("created_at_utc", "INTEGER", true, 0),
                    new ColumnDef("is_committed", "INTEGER", true, 0),
            }),
            new TableDef("settings", new ColumnDef[]{
                    new ColumnDef("key", "TEXT", true, 1),
                    new ColumnDef("value", "TEXT", false, 0),
            }),
    };


    private Context context;
    private SQLiteDatabase database = null;

    public static IRepository getRepository() {
        return repository;
    }

    public String getFilePath() {
        return context.getFilesDir()
                + File.separator
                + context.getString(R.string.repository_file_name);
    }

    public String getTempFilePath() {
        return context.getFilesDir()
                + File.separator
                + context.getString(R.string.repository_temp_file_name);
    }

    public void open(Activity activity) {
        String error = initDatabase();
        if (error != null) {
            exitWithError(activity, error);
        }
    }

    public void openTemp(Activity activity) {
        String error = initTempDatabase();
        if (error != null) {
            activity.runOnUiThread(() -> ErrorDialog.showError(activity, error, activity::finish));
        } else {
            try {
                FileInputStream inputStream = new FileInputStream(getTempFilePath());
                FileOutputStream outputStream = new FileOutputStream(getFilePath());
                if (ImportExport.transferFile(activity, inputStream, outputStream)) {
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
            } catch (FileNotFoundException exception) {
                activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_file_not_found, activity::finish));
            } catch (Exception exception) {
                activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_open_file, activity::finish));
            }
        }
        open(activity);
    }

    public void close() {
        database.close();
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

    private void initCategory() {
        Stack<Long> parentId = new Stack<>();
        parentId.add(null);
        SQLiteStatement statement = database.compileStatement(context.getString(R.string.sql_init_category));
        String down = context.getString(R.string.sql_init_category_values_divider_down);
        String up = context.getString(R.string.sql_init_category_values_divider_up);
        String[] values = context.getResources().getStringArray(R.array.sql_init_category_values);
        for (String value : values) {
            if (up.equals(value)) {
                parentId.pop();
                continue;
            }
            if (down.equals(value)) {
                parentId.push(parentId.peek());
                continue;
            }
            parentId.pop();
            statement.bindString(1, value);
            if (parentId.peek() == null)
                statement.bindNull(2);
            else
                statement.bindLong(2, parentId.peek());
            long id = statement.executeInsert();
            parentId.push(id);
        }
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
            try {
                initCategory();
            } catch (Exception ignored) {
            }
        }
        initVersion();
    }

    private void initVersion() {
        setSettingsValue(context.getString(R.string.settings_version), String.valueOf(BuildConfig.VERSION_CODE));
    }

    private boolean isDatabaseEmpty() {
        return checkSQLFalse(R.string.sql_check_database_empty);
    }

    private int getDatabaseVersion() {
        String versionStr = null;
        try {
            versionStr = getSettingsValue(context.getString(R.string.settings_version));
        } catch (Exception ignored) {
        }
        if (versionStr == null) versionStr = "";
        int version = 1000000;
        try {
            version = Integer.parseInt(versionStr);
        } catch (NumberFormatException ignored) {
        }
        return version;
    }

    private void updateDatabase() {
        int version = getDatabaseVersion();
        int versionMajor = version / 1000000;
        int versionMinor = (version / 1000) % 1000;

        if (versionMajor == 1 && versionMinor == 0) {
            initVersion();
        }
    }

    private boolean isDatabaseVersionInvalid() {
        int version = getDatabaseVersion();
        int versionApp = BuildConfig.VERSION_CODE;
        return (version / 1000000 != versionApp / 1000000) || (version / 1000 > versionApp / 1000);
    }

    private boolean checkColumnFail(String table, String column, String type, boolean notnull, int primary) {
        return !checkSQL(R.string.sql_check_column, new Object[]{table, column, type, notnull ? 1L : 0L, (long)primary});
    }

    private boolean checkTableColumnsFail(String table, int columnsCount) {
        return !checkSQL(R.string.sql_check_table_columns, new Object[]{table, (long)columnsCount});
    }

    private boolean isDatabaseInvalid() {
        for(TableDef table : tables){
            if (checkTableColumnsFail(table.name, table.columns.length))
                return true;
            for(ColumnDef column : table.columns){
                if (checkColumnFail(table.name, column.name, column.type, column.notnull, column.primary))
                    return true;
            }
        }
        return false;
    }

    private void exitWithError(Activity activity, String message) {
        activity.runOnUiThread(() -> ErrorDialog.showError(activity, message, () -> activity.runOnUiThread(Restart::exit)));
    }

    private String initDatabase() {
        return initDatabase(getFilePath());
    }

    private String initTempDatabase() {
        return initDatabase(getTempFilePath());
    }

    private String initDatabase(String path) {
        try {
            if (database != null && database.isOpen()) {
                database.close();
            }
            database = SQLiteDatabase.openDatabase(path, null, OPEN_READWRITE | CREATE_IF_NECESSARY | NO_LOCALIZED_COLLATORS);
            execSQL(R.string.sql_init_pre);
            if (isDatabaseEmpty()) {
                initData();
            } else {
                if (isDatabaseVersionInvalid()) {
                    return context.getString(R.string.error_database_version_mismatch);
                } else if (isDatabaseInvalid()) {
                    return context.getString(R.string.error_database_invalid);
                } else {
                    updateDatabase();
                }
            }
            return null;
        } catch (Exception exception) {
            return context.getString(R.string.error_database_open);
        }
    }

    @Override
    public void init(Context context, Activity activity) {
        this.context = context;
        open(activity);
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
        Money money = getMoney(R.string.sql_get_account_sum, new String[]{String.valueOf(accountId)});
        if (money.items.isEmpty()) {
            Money.MoneyItem item = new Money.MoneyItem();
            item.sum10000 = 0;
            item.currencyId = -1;
            item.currencySymbol = "";
            return item;
        } else {
            return money.items.get(0);
        }
    }

    @Override
    public Money getCategorySum(long categoryId, long fromUTC, long toUTC) {
        return getMoney(R.string.sql_get_category_sum, new String[]{String.valueOf(categoryId), String.valueOf(fromUTC), String.valueOf(toUTC)});
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
    public void insertTransaction(long categoryId, long accountId, long sum10000, String product, long createdAtUTC, boolean intoDraft) {
        execSQL(R.string.sql_insert_transaction, new Object[]{categoryId, accountId, sum10000, product, createdAtUTC, intoDraft ? 0 : 1});
    }

    @Override
    public void insertTransfer(long accountIdFrom, long accountIdTo, long sum10000From, long sum10000To, long createdAtUTC, boolean intoDraft) {
        execSQL(R.string.sql_insert_transfer, new Object[]{accountIdFrom, accountIdTo, sum10000From, sum10000To, createdAtUTC, intoDraft ? 0 : 1});
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
    }

    @Override
    public void updateAccount(long accountId, String name, long currencyId, long sum10000, boolean isVisible, String comment, List<Long> accountGroupIds) {
        execSQL(R.string.sql_update_account, new Object[]{accountId, name, currencyId, sum10000, isVisible, comment});
        execSQL(R.string.sql_delete_account_group_account_by_account, new Object[]{accountId});
        for (Long accountGroupId : accountGroupIds) {
            insertAccountGroupAccount(accountGroupId, accountId);
        }
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
        return true;
    }

    @Override
    public void updateAccountGroup(long accountGroupId, String name, boolean isVisible, String comment, List<Long> accountIds) {
        execSQL(R.string.sql_update_account_group, new Object[]{accountGroupId, name, isVisible, comment});
        execSQL(R.string.sql_delete_account_group_account_by_account_group, new Object[]{accountGroupId});
        for (Long accountId : accountIds) {
            insertAccountGroupAccount(accountGroupId, accountId);
        }
    }

    @Override
    public boolean deleteCurrency(long currencyId) {
        try {
            execSQL(R.string.sql_delete_currency, new Object[]{currencyId});
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAccount(long accountId) {
        try {
            execSQL(R.string.sql_delete_account, new Object[]{accountId});
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteCategory(long categoryId) {
        try {
            execSQL(R.string.sql_delete_category, new Object[]{categoryId});
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAccountGroup(long accountGroupId) {
        try {
            execSQL(R.string.sql_delete_account_group, new Object[]{accountGroupId});
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private List<MoneyListItem> getMoneyListItems(@NonNull String sql, String[] args) {
        HashMap<Long, MoneyListItem> items = new HashMap<>();
        try (Cursor cursor = querySQL(sql, args)) {
            int columnTextId = cursor.getColumnIndex("text_id");
            int columnText = cursor.getColumnIndex("text");
            int columnCurrencyId = cursor.getColumnIndex("currency_id");
            int columnCurrencySymbol = cursor.getColumnIndex("currency_symbol");
            int columnSum10000 = cursor.getColumnIndex("sum10000");
            while (cursor.moveToNext()) {
                long textId = cursor.getLong(columnTextId);
                Money.MoneyItem moneyItem = new Money.MoneyItem();
                moneyItem.currencyId = cursor.getLong(columnCurrencyId);
                moneyItem.currencySymbol = cursor.getString(columnCurrencySymbol);
                moneyItem.sum10000 = cursor.getLong(columnSum10000);
                if (items.containsKey(textId)) {
                    Objects.requireNonNull(items.get(textId)).money.items.add(moneyItem);
                } else {
                    MoneyListItem listItem = new MoneyListItem();
                    listItem.text = cursor.getString(columnText);
                    listItem.money = new Money();
                    listItem.money.items.add(moneyItem);
                    items.put(textId, listItem);
                }
            }
        }
        return new ArrayList<>(items.values());
    }

    private List<MoneyListItem> getMoneyListItems(@StringRes int resId, String[] args) {
        return getMoneyListItems(context.getString(resId), args);
    }

    private List<MoneyListItem> getMoneyListItems(@StringRes int resId) {
        return getMoneyListItems(resId, new String[]{});
    }

    @Override
    public List<MoneyListItem> getAccountMoneyListItems() {
        return getMoneyListItems(R.string.sql_get_accounts_for_money_list_total);
    }

    @Override
    public List<MoneyListItem> getAccountGroupMoneyListItems() {
        return getMoneyListItems(R.string.sql_get_account_groups_for_money_list_total);
    }

    @Override
    public List<MoneyListItem> getAccountMoneyListItems(long fromUTC, long toUTC) {
        return getMoneyListItems(R.string.sql_get_accounts_for_money_list, new String[]{String.valueOf(fromUTC), String.valueOf(toUTC)});
    }

    @Override
    public List<MoneyListItem> getCategoryMoneyListItems(long fromUTC, long toUTC) {
        return getMoneyListItems(R.string.sql_get_categories_for_money_list, new String[]{String.valueOf(fromUTC), String.valueOf(toUTC)});
    }

    @Override
    public List<MoneyListItem> getAccountGroupMoneyListItems(long fromUTC, long toUTC) {
        return getMoneyListItems(R.string.sql_get_account_groups_for_money_list, new String[]{String.valueOf(fromUTC), String.valueOf(toUTC)});
    }

    @Override
    public List<SpinnerItem> getCurrencySpinnerItemsByCategory(long categoryId, long fromUTC, long toUTC) {
        return getSpinnerItems(R.string.sql_get_currencies_by_category_for_spinner, new String[]{String.valueOf(categoryId), String.valueOf(fromUTC), String.valueOf(toUTC)});
    }

    @Override
    public List<Money.MoneyItem> getCategorySumMonthlyByDays(long categoryId, long currencyId, int year, int month) {
        int days = DateTime.getLengthOfMonth(year, month);
        List<Money.MoneyItem> result = new ArrayList<>();
        for (int day = 1; day <= days; day++) {
            long fromUTC = DateTime.convertLocalToUTC(new DateTime(year, month, day, 0, 0, 0));
            long toUTC = DateTime.addDaysToUTC(fromUTC, 1);
            Money sum = getMoney(R.string.sql_get_category_sum_by_currency, new String[]{String.valueOf(categoryId), String.valueOf(currencyId), String.valueOf(fromUTC), String.valueOf(toUTC)});
            if (sum.items.isEmpty()) {
                Money.MoneyItem item = new Money.MoneyItem();
                item.sum10000 = 0;
                item.currencyId = currencyId;
                item.currencySymbol = "";
                result.add(item);
            } else {
                result.add(sum.items.get(0));
            }
        }
        return result;
    }

    private XSSFSheet excelCreateSheetWithTable(@NonNull String sql, String[] args, XSSFWorkbook workbook, String sheetName, Excel.TableColumn[] columns) {
        try (Cursor cursor = querySQL(sql, args)) {
            return Excel.createSheetWithTable(workbook, cursor, sheetName, columns);
        }
    }

    private XSSFSheet excelCreateSheetWithTable(@StringRes int resId, String[] args, XSSFWorkbook workbook, String sheetName, Excel.TableColumn[] columns) {
        return excelCreateSheetWithTable(context.getString(resId), args, workbook, sheetName, columns);
    }

    private XSSFSheet excelCreateSheetWithTable(@StringRes int resId, XSSFWorkbook workbook, String sheetName, Excel.TableColumn[] columns) {
        return excelCreateSheetWithTable(resId, new String[]{}, workbook, sheetName, columns);
    }

    @Override
    public XSSFSheet excelSheetActions(XSSFWorkbook workbook, String sheetName, Excel.TableColumn[] columns) {
        return excelCreateSheetWithTable(R.string.sql_excel_actions, new String[]{
                context.getString(R.string.excel_actions_type_initial),
                context.getString(R.string.excel_actions_type_transaction),
                context.getString(R.string.excel_actions_type_transfer)
        }, workbook, sheetName, columns);
    }

    @Override
    public XSSFSheet excelSheetCategories(XSSFWorkbook workbook, String sheetName, Excel.TableColumn[] columns) {
        return excelCreateSheetWithTable(R.string.sql_excel_categories, workbook, sheetName, columns);
    }

    private static class ColumnDef {
        public final String name;
        public final String type;
        public final boolean notnull;
        public final int primary;

        ColumnDef(String name, String type, boolean notnull, int primary) {
            this.name = name;
            this.type = type;
            this.notnull = notnull;
            this.primary = primary;
        }
    }

    private static class TableDef {
        public final String name;
        public final ColumnDef[] columns;

        TableDef(String name, ColumnDef[] columns) {
            this.name = name;
            this.columns = columns;
        }
    }

    @Override
    public boolean isDraftEmptyFastCheck(){
        return checkSQLFalse(R.string.sql_check_fast_draft);
    }
}
