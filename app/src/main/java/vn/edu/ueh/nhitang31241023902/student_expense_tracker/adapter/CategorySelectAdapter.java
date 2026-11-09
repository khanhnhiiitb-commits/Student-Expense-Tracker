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
    private int selectedPosition = RecyclerView.NO_POSITION;
    private long selectedCategoryId = -1;
    private OnCategorySelectedListener listener;

    public CategorySelectAdapter(Context context, OnCategorySelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<CategoryEntity> newList) {
        list.clear();

        if (newList != null) {
            list.addAll(newList);
        }

        selectedPosition = RecyclerView.NO_POSITION;

        // Ưu tiên khôi phục danh mục theo ID.
        if (selectedCategoryId > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).categoryId == selectedCategoryId) {
                    selectedPosition = i;
                    break;
                }
            }
        } else if (!list.isEmpty()) {
            // Giữ nghiệp vụ: tự chọn danh mục đầu tiên khi thêm mới.
            selectedPosition = 0;
            selectedCategoryId = list.get(0).categoryId;
        }

        notifyDataSetChanged();

        CategoryEntity selected = getSelectedCategory();
        if (listener != null) {
            listener.onSelected(selected);
        }
    }

    // Hàm tự động tìm và bôi đậm danh mục dựa vào ID (Dùng khi Chỉnh sửa giao dịch cũ)
    public void setSelectedCategoryId(long categoryId) {
        selectedCategoryId = categoryId;
        selectedPosition = RecyclerView.NO_POSITION;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).categoryId == categoryId) {
                selectedPosition = i;
                break;
            }
        }

        notifyDataSetChanged();

        if (listener != null) {
            listener.onSelected(getSelectedCategory());
        }
    }

    public void clearSelection() {
        int oldPosition = selectedPosition;

        selectedCategoryId = -1;
        selectedPosition = RecyclerView.NO_POSITION;

        if (oldPosition != RecyclerView.NO_POSITION
                && oldPosition < list.size()) {
            notifyItemChanged(oldPosition);
        }

        if (listener != null) {
            listener.onSelected(null);
        }
    }


    public CategoryEntity getSelectedCategory() {
        if (selectedPosition >= 0
                && selectedPosition < list.size()) {
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

        // TÁCH CHUỖI ĐỂ LẤY MÀU CHO DANH MỤC TÙY CHỈNH
        if (category.userId != null && category.userId > 0) {
            String[] parts = fullName.split(" ", 3);

            if (parts.length == 3 && parts[1].startsWith("#")) {
                // Đã lưu theo chuẩn: Icon + Màu + Tên
                holder.tvIcon.setText(parts[0]);
                holder.tvName.setText(parts[2]);
                try {
                    holder.tvIcon.setBackgroundColor(Color.parseColor(parts[1]));
                } catch (IllegalArgumentException e) {
                    holder.tvIcon.setBackgroundColor(Color.parseColor(getDynamicColor(category.categoryId)));
                }
            } else if (parts.length >= 2) {
                // Form cũ (chỉ Icon + Tên)
                holder.tvIcon.setText(parts[0]);
                holder.tvName.setText(fullName.substring(parts[0].length()).trim());
                holder.tvIcon.setBackgroundColor(Color.parseColor(getDynamicColor(category.categoryId)));
            } else {
                holder.tvIcon.setText("🏷️");
                holder.tvName.setText(fullName);
                holder.tvIcon.setBackgroundColor(Color.parseColor(getDynamicColor(category.categoryId)));
            }
        } else {
            // Danh mục mặc định của hệ thống
            holder.tvName.setText(fullName);
            bindCategoryIcon(holder.tvIcon, fullName);
        }

        // Bôi màu nền để highlight nếu item đang được chọn
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#DBEAFE"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Xử lý sự kiện click chọn danh mục
        holder.itemView.setOnClickListener(v -> {
            int newPosition = holder.getBindingAdapterPosition();

            if (newPosition == RecyclerView.NO_POSITION) {
                return;
            }

            int oldPosition = selectedPosition;

            selectedPosition = newPosition;
            selectedCategoryId = list.get(newPosition).categoryId;

            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition);
            }

            notifyItemChanged(newPosition);

            if (listener != null) {
                listener.onSelected(list.get(newPosition));
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

    private final String[] customColors = {
            "#2563EB", // 1. Xanh dương đậm
            "#16A34A", // 2. Xanh lá
            "#EF4444", // 3. Đỏ
            "#F59E0B", // 4. Cam vàng
            "#8B5CF6", // 5. Tím
            "#EC4899", // 6. Hồng
            "#06B6D4", // 7. Xanh lơ (Cyan)
            "#64748B"  // 8. Xám (Slate)
    };

    private String getDynamicColor(long categoryId) {
        // Dùng phép chia lấy dư để chọn 1 màu trong mảng dựa vào ID
        int index = (int) (Math.abs(categoryId) % customColors.length);
        return customColors[index];
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