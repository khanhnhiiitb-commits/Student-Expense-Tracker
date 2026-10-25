package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    // Interface mở rộng để lắng nghe sự kiện Click, Edit, Delete
    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryEntity item);
    }

    public interface OnCategoryActionListener {
        void onEdit(CategoryEntity item);
        void onDelete(CategoryEntity item);
    }

    private Context context;
    private List<CategoryEntity> list = new ArrayList<>();
    private OnCategoryClickListener listener;
    private OnCategoryActionListener actionListener;

    public CategoryAdapter(Context context, List<CategoryEntity> initialList, OnCategoryClickListener listener) {
        this.context = context;
        if (initialList != null) {
            this.list.addAll(initialList);
        }
        this.listener = listener;
    }

    public void setActionListener(OnCategoryActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setData(List<CategoryEntity> newList) {
        this.list.clear();
        if (newList != null) {
            this.list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryEntity item = list.get(position);
        if (item == null) return;

        holder.tvName.setText(item.categoryName);

        // 1. Gán Emoji và màu nền tương ứng theo tên danh mục
        bindCategoryIcon(holder.tvIcon, item.categoryName);

        // 2. Cập nhật trạng thái hiển thị
        if (item.userId == null || item.userId <= 0) {
            holder.tvSubtitle.setText("Mặc định · Cố định hệ thống");
            // Ẩn nút sửa/xóa với danh mục mặc định của hệ thống
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
        } else {
            holder.tvSubtitle.setText("Tùy chỉnh · Danh mục của bạn");
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setVisibility(View.VISIBLE);

            // Bắt sự kiện sửa/xóa
            holder.btnEdit.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onEdit(item);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onDelete(item);
            });
        }

        // Bắt sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // Hàm tra cứu emoji & màu nền phù hợp cho từng danh mục
    private void bindCategoryIcon(TextView tvIcon, String name) {
        if (name == null) {
            tvIcon.setText("📦");
            tvIcon.setBackgroundColor(Color.parseColor("#F1F5F9"));
            return;
        }

        switch (name.trim()) {
            case "Ăn uống":
                tvIcon.setText("🍜");
                tvIcon.setBackgroundColor(Color.parseColor("#FEF3C7")); // Vàng nhạt
                break;
            case "Học tập":
                tvIcon.setText("📚");
                tvIcon.setBackgroundColor(Color.parseColor("#D1FAE5")); // Xanh lá nhạt
                break;
            case "Đi lại":
                tvIcon.setText("🚌");
                tvIcon.setBackgroundColor(Color.parseColor("#FFEDD5")); // Cam nhạt
                break;
            case "Nhà ở":
                tvIcon.setText("🏠");
                tvIcon.setBackgroundColor(Color.parseColor("#EDE9FE")); // Tím nhạt
                break;
            case "Mua sắm":
                tvIcon.setText("🛍️");
                tvIcon.setBackgroundColor(Color.parseColor("#FCE7F3")); // Hồng nhạt
                break;
            case "Giải trí":
                tvIcon.setText("🎮");
                tvIcon.setBackgroundColor(Color.parseColor("#E0E7FF")); // Lam nhạt
                break;
            case "Sức khỏe":
                tvIcon.setText("💊");
                tvIcon.setBackgroundColor(Color.parseColor("#FEE2E2")); // Đỏ nhạt
                break;
            case "Hóa đơn và dịch vụ":
                tvIcon.setText("🧾");
                tvIcon.setBackgroundColor(Color.parseColor("#FEF9C3")); // Vàng chanh
                break;
            case "Chi tiêu khác":
                tvIcon.setText("📦");
                tvIcon.setBackgroundColor(Color.parseColor("#F1F5F9"));
                break;
            case "Lương / Làm thêm":
                tvIcon.setText("💰");
                tvIcon.setBackgroundColor(Color.parseColor("#DCFCE7")); // Xanh bạc hà
                break;
            case "Trợ cấp gia đình":
                tvIcon.setText("👨‍👩‍👧");
                tvIcon.setBackgroundColor(Color.parseColor("#E0F2FE")); // Xanh biển nhạt
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

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon;
        TextView tvName;
        TextView tvSubtitle;
        TextView btnEdit;
        TextView btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvSubtitle = itemView.findViewById(R.id.tvCategorySubtitle);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}