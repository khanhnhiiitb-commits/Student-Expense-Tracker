package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionEntity transaction);
    }

    private Context context;
    private List<TransactionEntity> transactionList = new ArrayList<>();
    private OnTransactionClickListener listener;

    public TransactionAdapter(Context context, OnTransactionClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<TransactionEntity> newList) {
        this.transactionList.clear();
        if (newList != null) {
            this.transactionList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity item = transactionList.get(position);
        if (item == null) return;

        // 1. Định dạng số tiền
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedAmount = (item.amount != null) ? formatter.format(item.amount) : "0";

        // Tùy theo cách lưu thu nhập/chi tiêu của bạn (hoặc so sánh note / category):
        // Mặc định hiển thị định dạng số tiền
        holder.tvAmount.setText(formattedAmount + " đ");

        // 2. Định dạng ngày giao dịch
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(item.transactionDate));

        if (item.note != null && !item.note.trim().isEmpty()) {
            holder.tvTitle.setText(item.note);
            holder.tvDateNote.setText(dateStr);
        } else {
            holder.tvTitle.setText("Giao dịch #" + item.categoryID);
            holder.tvDateNote.setText(dateStr);
        }

        // 3. Sự kiện click vào dòng giao dịch
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon;
        TextView tvTitle;
        TextView tvDateNote;
        TextView tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvTransactionIcon);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvDateNote = itemView.findViewById(R.id.tvTransactionDateNote);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
        }
    }
}