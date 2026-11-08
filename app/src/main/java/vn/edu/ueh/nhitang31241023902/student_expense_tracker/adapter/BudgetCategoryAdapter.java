
package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BudgetWarningUtils;

public class BudgetCategoryAdapter
        extends RecyclerView.Adapter<BudgetCategoryAdapter.ViewHolder> {

    private final Context context;
    private List<BudgetDisplayModel> list = new ArrayList<>();
    private final DecimalFormat formatter =
            new DecimalFormat("#,###");

    public BudgetCategoryAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<BudgetDisplayModel> newList) {
        this.list = newList != null
                ? newList
                : new ArrayList<>();

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_budget_category,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        BudgetDisplayModel model = list.get(position);

        // 1. Tên danh mục
        holder.tvCategoryName.setText(model.categoryName);

        // 2. Số tiền đã chi / ngân sách
        String amountInfo =
                formatter.format(model.spentAmount)
                        + " / "
                        + formatter.format(model.targetAmount)
                        + " đ";

        holder.tvCategoryAmountInfo.setText(amountInfo);

        // 3. Tính phần trăm
        double percentage = model.targetAmount > 0
                ? (double) model.spentAmount
                  / model.targetAmount * 100
                : 0;

        holder.tvPercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0f%%",
                        percentage
                )
        );

        // 4. Cập nhật thanh tiến độ
        int progress = (int) Math.max(
                0,
                Math.min(percentage, 100)
        );

        holder.pbCategoryProgress.setProgress(progress);

        // 5. Xác định mức cảnh báo
        BudgetWarningUtils.WarningLevel level =
                BudgetWarningUtils.checkBudget(
                        model.targetAmount,
                        model.spentAmount
                );

        int color = BudgetWarningUtils.getColor(level);

        holder.tvPercentage.setTextColor(color);

        holder.pbCategoryProgress.setProgressTintList(
                ColorStateList.valueOf(color)
        );

        // 6. Hiển thị cảnh báo
        if (level == BudgetWarningUtils.WarningLevel.NORMAL) {
            holder.tvCategoryWarning.setVisibility(View.GONE);
            holder.tvCategoryWarning.setText("");
        } else {
            holder.tvCategoryWarning.setVisibility(View.VISIBLE);

            holder.tvCategoryWarning.setText(
                    BudgetWarningUtils.getMessage(level)
            );

            holder.tvCategoryWarning.setTextColor(color);
        }

        // 7. Icon danh mục
        holder.tvCategoryIcon.setText(
                getCategoryEmoji(model.categoryName)
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String getCategoryEmoji(String categoryName) {
        if (categoryName == null) return "📦";

        String name = categoryName
                .toLowerCase(Locale.ROOT)
                .trim();

        if (name.contains("ăn uống")) return "🍜";
        if (name.contains("học tập")) return "📚";
        if (name.contains("đi lại")) return "🚌";
        if (name.contains("nhà ở")) return "🏠";
        if (name.contains("mua sắm")) return "🛍️";
        if (name.contains("giải trí")) return "🎮";
        if (name.contains("sức khỏe")
                || name.contains("y tế")) return "💊";
        if (name.contains("hóa đơn")) return "🧾";
        if (name.contains("khác")) return "📦";

        return "📦";
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCategoryName;
        TextView tvCategoryAmountInfo;
        TextView tvPercentage;
        TextView tvCategoryWarning;
        TextView tvCategoryIcon;
        ProgressBar pbCategoryProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategoryName =
                    itemView.findViewById(R.id.tvCategoryName);

            tvCategoryAmountInfo =
                    itemView.findViewById(R.id.tvCategoryAmountInfo);

            tvPercentage =
                    itemView.findViewById(R.id.tvPercentage);

            tvCategoryWarning =
                    itemView.findViewById(R.id.tvCategoryWarning);

            tvCategoryIcon =
                    itemView.findViewById(R.id.tvCategoryIcon);

            pbCategoryProgress =
                    itemView.findViewById(R.id.pbCategoryProgress);
        }
    }

    public static class BudgetDisplayModel {

        public long categoryId;
        public String categoryName;
        public long targetAmount;
        public long spentAmount;

        public BudgetDisplayModel(
                long categoryId,
                String categoryName,
                long targetAmount,
                long spentAmount
        ) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.targetAmount = targetAmount;
            this.spentAmount = spentAmount;
        }
    }
}
