package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;

public class CategorySelectAdapter extends RecyclerView.Adapter<CategorySelectAdapter.ViewHolder> {

    public interface OnCategorySelectedListener {
        void onSelected(CategoryEntity category);
    }

    private Context context;
    private List<CategoryEntity> list = new ArrayList<>();
    private int selectedPosition = 0; // Mặc định chọn item đầu tiên
    private OnCategorySelectedListener listener;

    public CategorySelectAdapter(Context context, OnCategorySelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<CategoryEntity> newList) {
        this.list.clear();
        if (newList != null) {
            this.list.addAll(newList);
        }
        this.selectedPosition = 0;
        if (!this.list.isEmpty() && listener != null) {
            listener.onSelected(this.list.get(0));
        }
        notifyDataSetChanged();
    }

    public CategoryEntity getSelectedCategory() {
        if (!list.isEmpty() && selectedPosition >= 0 && selectedPosition < list.size()) {
            return list.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryEntity item = list.get(position);
        if (item == null) return;

        holder.tvName.setText(item.categoryName);
        holder.tvIcon.setText(getEmojiForCategory(item.categoryName));

        // Highlight viền nếu item đang được chọn
        if (selectedPosition == position) {
            holder.layoutBorder.setBackgroundColor(Color.parseColor("#2563EB")); // Viền xanh
            holder.tvName.setTextColor(Color.parseColor("#2563EB"));
            holder.tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.layoutBorder.setBackgroundColor(Color.parseColor("#E2E8F0")); // Viền xám
            holder.tvName.setTextColor(Color.parseColor("#64748B"));
            holder.tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            int prevPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prevPos);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String getEmojiForCategory(String name) {
        if (name == null) return "📦";
        switch (name.trim()) {
            case "Ăn uống": return "🍜";
            case "Học tập": return "📚";
            case "Đi lại": return "🚌";
            case "Nhà ở": return "🏠";
            case "Mua sắm": return "🛍️";
            case "Giải trí": return "🎮";
            case "Sức khỏe": return "💊";
            case "Hóa đơn và dịch vụ": return "🧾";
            case "Chi tiêu khác": return "📦";
            case "Lương / Làm thêm": return "💰";
            case "Trợ cấp gia đình": return "👨‍👩‍👧";
            case "Học bổng": return "🎓";
            case "Thu nhập khác": return "💵";
            default: return "🏷️";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutBorder;
        TextView tvIcon, tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBorder = itemView.findViewById(R.id.layoutIconBorder);
            tvIcon = itemView.findViewById(R.id.tvGridIcon);
            tvName = itemView.findViewById(R.id.tvGridName);
        }
    }
}