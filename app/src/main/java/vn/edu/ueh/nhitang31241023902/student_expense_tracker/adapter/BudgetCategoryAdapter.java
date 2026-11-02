package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;

public class BudgetCategoryAdapter extends RecyclerView.Adapter<BudgetCategoryAdapter.ViewHolder> {

    private final Context context;
    private List<BudgetDisplayModel> list = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public BudgetCategoryAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<BudgetDisplayModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BudgetDisplayModel model = list.get(position);

        // 1. Tên danh mục
        holder.tvCategoryName.setText(model.categoryName);

        // 2. Gộp chuỗi tiền đã tiêu / Tổng ngân sách
        String amountInfo = formatter.format(model.spentAmount) + " / " + formatter.format(model.targetAmount) + " đ";
        holder.tvCategoryAmountInfo.setText(amountInfo);

        // 3. Tính toán phần trăm
        int percent = model.targetAmount > 0 ? (int) ((double) model.spentAmount / model.targetAmount * 100) : 0;
        holder.tvPercentage.setText(percent + "%");

        // 4. Cập nhật thanh Progress
        holder.pbCategoryProgress.setProgress(Math.min(percent, 100));

        // 5. Cập nhật Icon dựa trên tên danh mục
        holder.tvCategoryIcon.setText(getCategoryEmoji(model.categoryName));    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    private String getCategoryEmoji(String categoryName) {
        if (categoryName == null) return "📦";

        String name = categoryName.toLowerCase().trim();
        if (name.contains("ăn uống")) return "🍜";
        if (name.contains("học tập")) return "📚";
        if (name.contains("đi lại")) return "🚌";
        if (name.contains("nhà ở")) return "🏠";
        if (name.contains("mua sắm")) return "🛍️";
        if (name.contains("giải trí")) return "🎮";
        if (name.contains("sức khỏe") || name.contains("y tế")) return "💊";
        if (name.contains("hóa đơn")) return "🧾";
        if (name.contains("khác")) return "📦";

        return "📦"; // Icon mặc định
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCategoryAmountInfo, tvPercentage;
        ProgressBar pbCategoryProgress;
        TextView tvCategoryIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryAmountInfo = itemView.findViewById(R.id.tvCategoryAmountInfo);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            pbCategoryProgress = itemView.findViewById(R.id.pbCategoryProgress);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
        }
    }

    // Model chứa dữ liệu để hiển thị
    public static class BudgetDisplayModel {
        public long categoryId;
        public String categoryName;
        public long targetAmount;
        public long spentAmount;

        public BudgetDisplayModel(long categoryId, String categoryName, long targetAmount, long spentAmount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.targetAmount = targetAmount;
            this.spentAmount = spentAmount;
        }
    }
}