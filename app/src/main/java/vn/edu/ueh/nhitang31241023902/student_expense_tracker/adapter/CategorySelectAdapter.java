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
        void onSelected(CategoryEntity category); // Đổi tên cho nhất quán
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
        // Giữ nguyên vị trí được chọn nếu có, ngược lại chọn 0
        if (selectedPosition >= this.list.size()) {
            selectedPosition = 0;
        }
        if (!this.list.isEmpty() && listener != null) {
            listener.onSelected(this.list.get(selectedPosition));
        }
        notifyDataSetChanged();
    }

    // Hàm tự động tìm và bôi đậm danh mục dựa vào ID (Dùng khi Chỉnh sửa giao dịch cũ)
    public void setSelectedCategoryId(long categoryId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).categoryId == categoryId) {
                int oldPosition = selectedPosition;
                selectedPosition = i;
                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);

                // Kích hoạt listener để Activity nhận dữ liệu
                if (listener != null) {
                    listener.onSelected(list.get(selectedPosition));
                }
                break;
            }
        }
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
        CategoryEntity category = list.get(position);
        if (category == null) return;

        String fullName = category.categoryName != null ? category.categoryName : "";
        int firstSpaceIndex = fullName.indexOf(" ");

        if (category.userId != null && category.userId > 0 && firstSpaceIndex != -1 && firstSpaceIndex <= 2) {
            String iconPart = fullName.substring(0, firstSpaceIndex);
            String namePart = fullName.substring(firstSpaceIndex + 1);

            holder.tvIcon.setText(iconPart);
            holder.tvName.setText(namePart);
            holder.tvIcon.setBackgroundColor(Color.parseColor("#F1F5F9"));
        } else {
            holder.tvName.setText(fullName);
            bindCategoryIcon(holder.tvIcon, fullName);
        }

        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#DBEAFE"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onSelected(category);
            }
        });
    }

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
        return list.size();
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