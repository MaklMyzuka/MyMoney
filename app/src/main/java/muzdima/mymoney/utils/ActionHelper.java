package muzdima.mymoney.utils;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import muzdima.mymoney.activity.ActionEditorActivity;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.AccountInfo;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.TransactionItem;
import muzdima.mymoney.repository.model.TransferItem;

public class ActionHelper {
    public static IActionItem createAction(int action, long accountId){
        AccountInfo accountInfo = (action == 0 ? null : Repository.getRepository().getAccountInfo(accountId));
        IActionItem result = null;
        switch (action) {
            case 0:
                // no action
                break;
            case 1:
                // expense
                TransactionItem expense = new TransactionItem();
                expense.id = -1;
                expense.categoryId = -1;
                expense.categoryName = null;
                expense.accountId = accountInfo.id;
                expense.accountName = accountInfo.name;
                expense.sum = new Money.MoneyItem();
                expense.sum.sum10000 = -1;
                expense.sum.currencyId = accountInfo.currencyId;
                expense.sum.currencySymbol = accountInfo.currencySymbol;
                expense.product = "";
                expense.createdAtUTC = DateTime.getNowUTC();
                result = expense;
                break;
            case 2:
                // income
                TransactionItem income = new TransactionItem();
                income.id = -1;
                income.categoryId = -1;
                income.categoryName = null;
                income.accountId = accountInfo.id;
                income.accountName = accountInfo.name;
                income.sum = new Money.MoneyItem();
                income.sum.sum10000 = 1;
                income.sum.currencyId = accountInfo.currencyId;
                income.sum.currencySymbol = accountInfo.currencySymbol;
                income.product = "";
                income.createdAtUTC = DateTime.getNowUTC();
                result = income;
                break;
            case 3:
                // transfer from
                TransferItem transferFrom = new TransferItem();
                transferFrom.id = -1;
                transferFrom.accountIdFrom = accountInfo.id;
                transferFrom.accountNameFrom = accountInfo.name;
                transferFrom.sumFrom = new Money.MoneyItem();
                transferFrom.sumFrom.sum10000 = 0;
                transferFrom.sumFrom.currencyId = accountInfo.currencyId;
                transferFrom.sumFrom.currencySymbol = accountInfo.currencySymbol;
                transferFrom.accountIdTo = -1;
                transferFrom.accountNameTo = null;
                transferFrom.sumTo = new Money.MoneyItem();
                transferFrom.sumTo.sum10000 = 0;
                transferFrom.sumTo.currencyId = -1;
                transferFrom.sumTo.currencySymbol = null;
                transferFrom.createdAtUTC = DateTime.getNowUTC();
                result = transferFrom;
                break;
            case 4:
                // transfer to
                TransferItem transferTo = new TransferItem();
                transferTo.id = -1;
                transferTo.accountIdFrom = -1;
                transferTo.accountNameFrom = null;
                transferTo.sumFrom = new Money.MoneyItem();
                transferTo.sumFrom.sum10000 = 0;
                transferTo.sumFrom.currencyId = -1;
                transferTo.sumFrom.currencySymbol = null;
                transferTo.accountIdTo = accountInfo.id;
                transferTo.accountNameTo = accountInfo.name;
                transferTo.sumTo = new Money.MoneyItem();
                transferTo.sumTo.sum10000 = 0;
                transferTo.sumTo.currencyId = accountInfo.currencyId;
                transferTo.sumTo.currencySymbol = accountInfo.currencySymbol;
                transferTo.createdAtUTC = DateTime.getNowUTC();
                result = transferTo;
                break;
        }
        return result;
    }

    public static void startActionEditorActivity(Context context, IActionItem item){
        Intent intent = new Intent(context, ActionEditorActivity.class);
        switch (item.getType()) {
            case IActionItem.TRANSACTION:
                intent.putExtra("itemTransaction", (new Gson()).toJson(item));
                break;
            case IActionItem.TRANSFER:
                intent.putExtra("itemTransfer", (new Gson()).toJson(item));
                break;
        }
        context.startActivity(intent);
    }
}
