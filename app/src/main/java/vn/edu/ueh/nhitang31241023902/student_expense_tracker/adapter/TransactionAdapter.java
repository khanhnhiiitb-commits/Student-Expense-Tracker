package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
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
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionEntity transaction);
    }

    private Context context;
    private List<TransactionEntity> transactionList = new ArrayList<>();
    private OnTransactionClickListener listener;
    private AppDatabase db;

    public TransactionAdapter(Context context, OnTransactionClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.db = AppDatabase.getDatabase(context); // Khởi tạo Database
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

        // 2. Định dạng ngày giao dịch
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(item.transactionDate));

        // 3. Truy vấn Category để lấy Loại (Thu/Chi) và Tên danh mục
        new Thread(() -> {
            CategoryEntity category = db.categoryDao().getCategoryById(item.categoryID);

            // Đẩy kết quả về lại luồng giao diện chính (UI Thread) để hiển thị
            new Handler(Looper.getMainLooper()).post(() -> {
                if (category != null) {

                    // --- XỬ LÝ ICON VÀ TÊN DANH MỤC ---
                    String fullName = category.categoryName != null ? category.categoryName : "";
                    String displayName = fullName;
                    int firstSpaceIndex = fullName.indexOf(" ");

                    // Kiểm tra xem đây có phải danh mục tự tạo có chứa icon ghép ở đầu không
                    if (category.userId != null && category.userId > 0 && firstSpaceIndex != -1 && firstSpaceIndex <= 2) {
                        String iconPart = fullName.substring(0, firstSpaceIndex);
                        displayName = fullName.substring(firstSpaceIndex + 1); // Bỏ icon khỏi tên

                        holder.tvIcon.setText(iconPart);
                        holder.tvIcon.setBackgroundColor(Color.parseColor("#F1F5F9")); // Nền xám cho danh mục tự tạo
                    } else {
                        // Nếu là danh mục mặc định của hệ thống
                        bindCategoryIcon(holder.tvIcon, fullName);
                    }

                    // --- XỬ LÝ TIÊU ĐỀ & GHI CHÚ ---
                    if (item.note != null && !item.note.trim().isEmpty()) {
                        holder.tvTitle.setText(item.note);
                        holder.tvDateNote.setText(dateStr + " · " + displayName);
                    } else {
                        holder.tvTitle.setText(displayName);
                        holder.tvDateNote.setText(dateStr);
                    }

                    // --- XỬ LÝ MÀU SẮC TIỀN (+/-) ---
                    if ("Income".equals(category.type)) {
                        holder.tvAmount.setText("+" + formattedAmount + " đ");
                        holder.tvAmount.setTextColor(Color.parseColor("#16A34A")); // Xanh lá
                    } else {
                        holder.tvAmount.setText("-" + formattedAmount + " đ");
                        holder.tvAmount.setTextColor(Color.parseColor("#EF4444")); // Đỏ
                    }

                } else {
                    // Fallback an toàn nếu lỡ danh mục bị xóa
                    holder.tvTitle.setText(item.note != null ? item.note : "Giao dịch #" + item.categoryID);
                    holder.tvDateNote.setText(dateStr);
                    holder.tvAmount.setText(formattedAmount + " đ");
                    holder.tvAmount.setTextColor(Color.parseColor("#0F172A")); // Đen mặc định
                    holder.tvIcon.setText("🏷️");
                    holder.tvIcon.setBackgroundColor(Color.parseColor("#F1F5F9"));
                }
            });
        }).start();

        // 4. Sự kiện click vào dòng giao dịch
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(item);
            }
        });
    }

    // Hàm tự động tra cứu icon và màu nền cho danh mục hệ thống
    private void bindCategoryIcon(TextView tvIcon, String name) {
        if (name == null) return;
        switch (name.trim()) {
            case "Ăn uống":
                tvIcon.setText("🍜");
                tvIcon.setBackgroundColor(Color.parseColor("#FEF3C7"));
                break;
            case "Học tập":
                tvIcon.setText("📚");
                tvIcon.setBackgroundColor(Color.parseColor("#D1FAE5"));
                break;
            case "Đi lại":
                tvIcon.setText("🚌");
                tvIcon.setBackgroundColor(Color.parseColor("#FFEDD5"));
                break;
            case "Nhà ở":
                tvIcon.setText("🏠");
                tvIcon.setBackgroundColor(Color.parseColor("#EDE9FE"));
                break;
            case "Mua sắm":
                tvIcon.setText("🛍️");
                tvIcon.setBackgroundColor(Color.parseColor("#FCE7F3"));
                break;
            case "Giải trí":
                tvIcon.setText("🎮");
                tvIcon.setBackgroundColor(Color.parseColor("#E0E7FF"));
                break;
            case "Sức khỏe":
                tvIcon.setText("💊");
                tvIcon.setBackgroundColor(Color.parseColor("#FEE2E2"));
                break;
            case "Hóa đơn và dịch vụ":
                tvIcon.setText("🧾");
                tvIcon.setBackgroundColor(Color.parseColor("#FEF9C3"));
                break;
            case "Chi tiêu khác":
                tvIcon.setText("📦");
                tvIcon.setBackgroundColor(Color.parseColor("#F1F5F9"));
                break;
            case "Lương / Làm thêm":
                tvIcon.setText("💰");
                tvIcon.setBackgroundColor(Color.parseColor("#DCFCE7"));
                break;
            case "Trợ cấp gia đình":
                tvIcon.setText("👨‍👩‍👧");
                tvIcon.setBackgroundColor(Color.parseColor("#E0F2FE"));
                break;
            case "Học bổng":
                tvIcon.setText("🎓");
                tvIcon.setBackgroundColor(Color.parseColor("#FEF3C7"));
                break;
            case "Thu nhập khác":
                tvIcon.setText("💵");
                tvIcon.setBackgroundColor(Color.parseColor("#F1F5F9"));
                break;
            default:
                tvIcon.setText("🏷️");
                tvIcon.setBackgroundColor(Color.parseColor("#F1F5F9"));
                break;
        }
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