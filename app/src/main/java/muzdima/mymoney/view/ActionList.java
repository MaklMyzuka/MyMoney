package muzdima.mymoney.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.model.IActionItem;
import muzdima.mymoney.repository.model.Money;
import muzdima.mymoney.repository.model.TransactionItem;
import muzdima.mymoney.repository.model.TransferItem;
import muzdima.mymoney.utils.DateTime;

public abstract class ActionList extends RecyclerView {

    private final Set<IActionItem> selected = Collections.newSetFromMap(new IdentityHashMap<>());
    private final ArrayList<OnCheckedChangeListener> listeners = new ArrayList<>();
    private Adapter adapter;

    public ActionList(@NonNull Context context) {
        super(context);
    }

    public ActionList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // DON'T CHANGE CONTENTS OF LIST items
    // FOR CHANGE, PASS NEW LIST INTO update(items)
    public void init(boolean selectable, List<IActionItem> items) {
        adapter = new Adapter(getContext(), selectable, items);
        setAdapter(adapter);
        setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void update(List<IActionItem> items, boolean forceRedraw) {
        adapter.update(items, forceRedraw);
    }

    protected abstract void onEdit(IActionItem item);

    public List<IActionItem> getSelected() {
        return new ArrayList<>(selected);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void toggleSelected() {
        if (adapter.items.isEmpty()) {
            return;
        }
        if (selected.size() == adapter.items.size()) {
            selected.clear();
        } else {
            selected.clear();
            selected.addAll(adapter.items);
        }
        adapter.notifyDataSetChanged();
        checkedChange();
    }

    public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void checkedChange() {
        boolean selectedAll = (!adapter.items.isEmpty() && selected.size() == adapter.items.size());
        for (OnCheckedChangeListener listener : listeners) {
            listener.onCheckedChanged(selectedAll);
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean selectedAll);
    }

    public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final Context context;
        private final boolean selectable;
        private final List<IActionItem> items;

        Adapter(Context context, boolean selectable, List<IActionItem> items) {
            this.context = context;
            this.selectable = selectable;
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).getType();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View view;
            switch (viewType) {
                case IActionItem.TRANSACTION:
                    view = inflater.inflate(R.layout.transaction_item, parent, false);
                    return new TransactionHolder(view, selectable);
                case IActionItem.TRANSFER:
                    view = inflater.inflate(R.layout.transfer_item, parent, false);
                    return new TransferHolder(view, selectable);
            }
            throw new RuntimeException("Unknown viewType");
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case IActionItem.TRANSACTION:
                    ((TransactionHolder) holder).configure((TransactionItem) items.get(position));
                    break;
                case IActionItem.TRANSFER:
                    ((TransferHolder) holder).configure(context, (TransferItem) items.get(position));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void update(List<IActionItem> items, boolean forceRedraw) {
            DiffCallback diffCallback = new DiffCallback(this.items, items, selected, forceRedraw);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            this.items.clear();
            this.items.addAll(items);
            if (selectable) {
                selected.clear();
                selected.addAll(diffCallback.selectedNew);
            }
            diffResult.dispatchUpdatesTo(this);
            if (selectable)
                checkedChange();
        }

        public class DiffCallback extends DiffUtil.Callback {
            public final Set<IActionItem> selectedOld;
            public final List<IActionItem> selectedNew = new ArrayList<>();
            private final List<IActionItem> oldList;
            private final List<IActionItem> newList;
            private final boolean forceRedraw;

            public DiffCallback(List<IActionItem> oldList, List<IActionItem> newList, Set<IActionItem> selectedOld, boolean forceRedraw) {
                this.oldList = oldList;
                this.newList = newList;
                this.selectedOld = selectedOld;
                this.forceRedraw = forceRedraw;
            }

            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                IActionItem oldItem = oldList.get(oldItemPosition);
                IActionItem newItem = newList.get(newItemPosition);
                if (oldItem.getType() == IActionItem.TRANSACTION && newItem.getType() == IActionItem.TRANSACTION) {
                    return TransactionItem.areItemsTheSame((TransactionItem) oldItem, (TransactionItem) newItem);
                }
                if (oldItem.getType() == IActionItem.TRANSFER && newItem.getType() == IActionItem.TRANSFER) {
                    return TransferItem.areItemsTheSame((TransferItem) oldItem, (TransferItem) newItem);
                }
                return false;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                IActionItem oldItem = oldList.get(oldItemPosition);
                IActionItem newItem = newList.get(newItemPosition);
                if (selectable && selectedOld.contains(oldItem)) {
                    selectedNew.add(newItem);
                }
                if (forceRedraw) {
                    return false;
                }
                if (oldItem.getType() == IActionItem.TRANSACTION && newItem.getType() == IActionItem.TRANSACTION) {
                    return TransactionItem.areContentsTheSame((TransactionItem) oldItem, (TransactionItem) newItem);
                }
                if (oldItem.getType() == IActionItem.TRANSFER && newItem.getType() == IActionItem.TRANSFER) {
                    return TransferItem.areContentsTheSame((TransferItem) oldItem, (TransferItem) newItem);
                }
                return false;
            }
        }

        public class TransactionHolder extends RecyclerView.ViewHolder {
            public CheckBox checkBox;
            public TextView product;
            public TextView category;
            public MoneyTextView sum;
            public TextView account;
            public TextView time;
            public Button button;

            public TransactionHolder(@NonNull View itemView, boolean selectable) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkBoxTransactionItem);
                product = itemView.findViewById(R.id.textViewProductTransactionItem);
                category = itemView.findViewById(R.id.textViewCategoryTransactionItem);
                sum = itemView.findViewById(R.id.textViewSumTransactionItem);
                account = itemView.findViewById(R.id.textViewAccountTransactionItem);
                time = itemView.findViewById(R.id.textViewTimeTransactionItem);
                button = itemView.findViewById(R.id.buttonTransactionItem);
                if (!selectable) {
                    checkBox.setVisibility(GONE);
                }
            }

            public void configure(TransactionItem item) {
                checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (!selected.contains(item)) {
                            selected.add(item);
                            checkedChange();
                        }
                    } else {
                        if (selected.contains(item)) {
                            selected.remove(item);
                            checkedChange();
                        }
                    }
                });
                checkBox.setChecked(selected.contains(item));
                product.setText(item.product);
                category.setText(item.categoryName);
                Money.DisplayParams params = sum.getDisplayParams();
                params.bold = true;
                sum.setDisplayParams(params);
                sum.setText(item.sum);
                account.setText(HtmlCompat.fromHtml(String.format(
                        "<b>%s</b>",
                        Html.escapeHtml(item.accountName)
                ), HtmlCompat.FROM_HTML_MODE_COMPACT));
                time.setText(DateTime.printDateTime(getContext(), DateTime.convertUTCToLocal(item.createdAtUTC)));
                button.setOnClickListener(view -> onEdit(item));
            }
        }

        public class TransferHolder extends RecyclerView.ViewHolder {

            public CheckBox checkBox;
            public TextView from;
            public TextView to;
            public MoneyTextView sum;
            public TextView time;
            public Button button;

            public TransferHolder(@NonNull View itemView, boolean selectable) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkBoxTransferItem);
                from = itemView.findViewById(R.id.textViewFromTransferItem);
                to = itemView.findViewById(R.id.textViewToTransferItem);
                sum = itemView.findViewById(R.id.textViewSumTransferItem);
                time = itemView.findViewById(R.id.textViewTimeTransferItem);
                button = itemView.findViewById(R.id.buttonTransferItem);
                if (!selectable) {
                    checkBox.setVisibility(GONE);
                }
            }

            public void configure(Context context, TransferItem item) {
                checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        if (!selected.contains(item)) {
                            selected.add(item);
                            checkedChange();
                        }
                    } else {
                        if (selected.contains(item)) {
                            selected.remove(item);
                            checkedChange();
                        }
                    }
                });
                checkBox.setChecked(selected.contains(item));
                from.setText(HtmlCompat.fromHtml(String.format(
                        "%s <b>%s</b>",
                        Html.escapeHtml(context.getString(R.string.transfer_from)),
                        Html.escapeHtml(item.accountNameFrom)
                ), HtmlCompat.FROM_HTML_MODE_COMPACT));
                to.setText(HtmlCompat.fromHtml(String.format(
                        "%s <b>%s</b>",
                        Html.escapeHtml(context.getString(R.string.transfer_to)),
                        Html.escapeHtml(item.accountNameTo)
                ), HtmlCompat.FROM_HTML_MODE_COMPACT));
                Money.DisplayParams params = sum.getDisplayParams();
                params.bold = true;
                sum.setDisplayParams(params);
                sum.setText(item.sumFrom, item.sumTo);
                time.setText(DateTime.printDateTime(getContext(), DateTime.convertUTCToLocal(item.createdAtUTC)));
                button.setOnClickListener(view -> onEdit(item));
            }
        }
    }


}
