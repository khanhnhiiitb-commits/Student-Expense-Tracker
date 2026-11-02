package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;

public class BudgetSetupAdapter extends RecyclerView.Adapter<BudgetSetupAdapter.ViewHolder> {

    private List<CategoryEntity> categoryList;
    private final Map<Long, Long> budgetMap = new HashMap<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public void setData(List<CategoryEntity> categories, Map<Long, Long> existingBudgets) {
        this.categoryList = categories;
        this.budgetMap.clear();
        if (existingBudgets != null) this.budgetMap.putAll(existingBudgets);
        notifyDataSetChanged();
    }

    public Map<Long, Long> getBudgetMap() {
        return budgetMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_setup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryEntity category = categoryList.get(position);

        // 1. Làm sạch tên danh mục (loại bỏ mã màu như #2563EB và emoji thừa nếu có)
        holder.tvCategoryName.setText(getCleanCategoryName(category.categoryName));

        // 2. Trích xuất hoặc gán đúng Emoji hiển thị vào khung tròn bên trái
        holder.tvCategoryIcon.setText(getCategoryEmoji(category.categoryName));
        try {
            String rawName = category.categoryName;
            if (rawName != null && rawName.contains("#")) {
                int colorIndex = rawName.indexOf("#");
                String hexColor = rawName.substring(colorIndex, colorIndex + 7); // Lấy mã #XXXXXX
                holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor(hexColor));
            } else {
                holder.cardView.setCardBackgroundColor(android.graphics.Color.WHITE); // Mặc định nếu không có màu
            }
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.WHITE);
        }
        if (holder.textWatcher != null) {
            holder.edtBudgetAmount.removeTextChangedListener(holder.textWatcher);
        }

        Long currentAmount = budgetMap.get(category.categoryId);
        if (currentAmount != null && currentAmount > 0) {
            holder.edtBudgetAmount.setText(formatter.format(currentAmount));
        } else {
            holder.edtBudgetAmount.setText("");
        }

        holder.textWatcher = new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    holder.edtBudgetAmount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[,.]", "");
                    long parsed = 0;

                    try {
                        if (!cleanString.isEmpty()) {
                            parsed = Long.parseLong(cleanString);
                            String formatted = formatter.format(parsed);

                            current = formatted;
                            holder.edtBudgetAmount.setText(formatted);
                            holder.edtBudgetAmount.setSelection(formatted.length());
                        } else {
                            current = "";
                        }
                    } catch (NumberFormatException ignored) {}

                    budgetMap.put(category.categoryId, parsed);
                    holder.edtBudgetAmount.addTextChangedListener(this);
                }
            }
        };
        holder.edtBudgetAmount.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    // Hàm lọc sạch tên (xóa mã màu hex như #2563EB)
    private String getCleanCategoryName(String rawName) {
        if (rawName == null) return "";
        String cleaned = rawName.replaceAll("#[0-9A-Fa-f]{6}", "") // Xóa mã màu hex
                .replaceAll("^[\\p{So}\\p{Cn}\\s]+", "") // Xóa emoji thừa ở đầu
                .trim();
        return cleaned.isEmpty() ? rawName : cleaned;
    }

    // Hàm lấy emoji thông minh
    private String getCategoryEmoji(String rawName) {
        if (rawName == null) return "📦";

        String name = rawName.toLowerCase().trim();
        if (name.contains("ăn uống")) return "🍜";
        if (name.contains("học tập")) return "📚";
        if (name.contains("đi lại")) return "🚌";
        if (name.contains("nhà ở")) return "🏠";
        if (name.contains("mua sắm")) return "🛍️";
        if (name.contains("giải trí")) return "🎮";
        if (name.contains("sức khỏe") || name.contains("y tế")) return "💊";
        if (name.contains("hóa đơn")) return "🧾";

        return "📦";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        EditText edtBudgetAmount;
        TextWatcher textWatcher;
        TextView tvCategoryIcon;
        com.google.android.material.card.MaterialCardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            edtBudgetAmount = itemView.findViewById(R.id.edtBudgetAmount);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            cardView = (com.google.android.material.card.MaterialCardView) itemView;
        }
    }
}