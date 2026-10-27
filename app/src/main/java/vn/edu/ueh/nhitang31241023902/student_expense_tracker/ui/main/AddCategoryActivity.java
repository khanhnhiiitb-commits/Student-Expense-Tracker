package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao.CategoryDao;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText edtCategoryName;
    private TextView tabTypeExpense, tabTypeIncome;
    private ImageButton btnBack;
    private AppCompatButton btnCreateCategoryBottom;
    private RecyclerView rcvIconPicker, rcvColorPicker;

    private String selectedType = "Expense";
    private String selectedIcon = "📦";
    private String selectedColorHex = "#2563EB";

    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Danh sách Icon và Màu sắc mẫu
    private final List<String> iconList = Arrays.asList("🍔", "📚", "🚗", "🏠", "🎮", "💊", "👗", "💰", "🎁", "✈️", "📦");
    private final List<String> colorList = Arrays.asList("#2563EB", "#16A34A", "#EF4444", "#F59E0B", "#8B5CF6", "#EC4899", "#06B6D4", "#64748B");

    private long editCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_category);

        db = AppDatabase.getDatabase(getApplicationContext());

        // Ánh xạ
        edtCategoryName = findViewById(R.id.edtCategoryName);
        tabTypeExpense = findViewById(R.id.tabTypeExpense);
        tabTypeIncome = findViewById(R.id.tabTypeIncome);
        btnBack = findViewById(R.id.btnBack);
        btnCreateCategoryBottom = findViewById(R.id.btnCreateCategoryBottom);
        rcvIconPicker = findViewById(R.id.rcvIconPicker);
        rcvColorPicker = findViewById(R.id.rcvColorPicker);

        setupPickers();

        // Nhận dữ liệu loại danh mục hoặc ID chỉnh sửa
        editCategoryId = getIntent().getLongExtra("EDIT_CATEGORY_ID", -1);
        if (editCategoryId > 0) {
            String name = getIntent().getStringExtra("CATEGORY_NAME");
            String type = getIntent().getStringExtra("CATEGORY_TYPE");
            if (name != null) edtCategoryName.setText(name);
            if (type != null) selectedType = type;
            btnCreateCategoryBottom.setText("Cập nhật danh mục");
        } else {
            String typeFromIntent = getIntent().getStringExtra("CATEGORY_TYPE");
            if (typeFromIntent != null) selectedType = typeFromIntent;
        }

        if ("Income".equals(selectedType)) selectIncomeType();
        else selectExpenseType();

        tabTypeExpense.setOnClickListener(v -> selectExpenseType());
        tabTypeIncome.setOnClickListener(v -> selectIncomeType());
        btnBack.setOnClickListener(v -> finish());
        btnCreateCategoryBottom.setOnClickListener(v -> saveCategory());
    }

    private void setupPickers() {
        // Cài đặt Adapter cho Icon
        SimpleStringPickerAdapter iconAdapter = new SimpleStringPickerAdapter(iconList, icon -> selectedIcon = icon);
        rcvIconPicker.setAdapter(iconAdapter);

        // Cài đặt Adapter cho Color
        SimpleColorPickerAdapter colorAdapter = new SimpleColorPickerAdapter(colorList, color -> selectedColorHex = color);
        rcvColorPicker.setAdapter(colorAdapter);
    }

    private void selectExpenseType() {
        selectedType = "Expense";
        tabTypeExpense.setBackgroundColor(0xFF2563EB);
        tabTypeExpense.setTextColor(0xFFFFFFFF);
        tabTypeIncome.setBackgroundColor(0x00000000);
        tabTypeIncome.setTextColor(0xFF64748B);
    }

    private void selectIncomeType() {
        selectedType = "Income";
        tabTypeIncome.setBackgroundColor(0xFF2563EB);
        tabTypeIncome.setTextColor(0xFFFFFFFF);
        tabTypeExpense.setBackgroundColor(0x00000000);
        tabTypeExpense.setTextColor(0xFF64748B);
    }

    private void saveCategory() {
        String categoryName = edtCategoryName.getText().toString().trim();
        if (categoryName.isEmpty()) {
            edtCategoryName.setError("Vui lòng nhập tên danh mục");
            return;
        }

        // Mẹo nhỏ: Ghép icon trực tiếp vào tên danh mục để không phải thêm cột icon vào CSDL
        // Ví dụ: "🍔 Ăn uống"
        String finalCategoryName = selectedIcon + " " + categoryName;

        btnCreateCategoryBottom.setEnabled(false);

        executorService.execute(() -> {
            CategoryDao categoryDao = db.categoryDao();
            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            long currentUserId = -1;
            UserEntity user = db.userDao().getUserByEmail(prefs.getString("USER_EMAIL", ""));
            if (user != null) currentUserId = user.userId;

            if (editCategoryId > 0) {
                // Sửa danh mục
                CategoryEntity cat = categoryDao.getCategoryById(editCategoryId);
                if (cat != null) {
                    cat.categoryName = finalCategoryName;
                    cat.type = selectedType;
                    categoryDao.updateCategory(cat);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                // Thêm mới
                CategoryEntity category = new CategoryEntity(currentUserId, finalCategoryName, selectedType);
                categoryDao.insertCategory(category);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Thêm danh mục thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    // ==========================================
    // CÁC LỚP ADAPTER CHO BIỂU TƯỢNG VÀ MÀU SẮC
    // ==========================================
    public static class SimpleStringPickerAdapter extends RecyclerView.Adapter<SimpleStringPickerAdapter.ViewHolder> {
        private final List<String> items;
        private final OnItemClickListener listener;
        private int selectedPos = 0;

        public interface OnItemClickListener { void onItemClick(String item); }

        public SimpleStringPickerAdapter(List<String> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setTextSize(24);
            tv.setPadding(24, 16, 24, 16);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TextView tv = (TextView) holder.itemView;
            tv.setText(items.get(position));
            tv.setBackgroundColor(selectedPos == position ? Color.parseColor("#E2E8F0") : Color.TRANSPARENT);
            tv.setOnClickListener(v -> {
                int oldPos = selectedPos;
                selectedPos = holder.getAdapterPosition();
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPos);
                listener.onItemClick(items.get(position));
            });
        }
        @Override public int getItemCount() { return items.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder { ViewHolder(android.view.View v) { super(v); } }
    }

    public static class SimpleColorPickerAdapter extends RecyclerView.Adapter<SimpleColorPickerAdapter.ViewHolder> {
        private final List<String> colors;
        private final OnColorClickListener listener;
        private int selectedPos = 0;

        public interface OnColorClickListener { void onColorClick(String color); }

        public SimpleColorPickerAdapter(List<String> colors, OnColorClickListener listener) {
            this.colors = colors;
            this.listener = listener;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = new android.view.View(parent.getContext());
            // Tạo kích thước ô vuông chọn màu
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(90, 90);
            params.setMargins(16, 0, 16, 0);
            view.setLayoutParams(params);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String colorHex = colors.get(position);
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(Color.parseColor(colorHex));
            if (selectedPos == position) shape.setStroke(6, Color.parseColor("#0F172A"));
            holder.itemView.setBackground(shape);
            holder.itemView.setOnClickListener(v -> {
                int oldPos = selectedPos;
                selectedPos = holder.getAdapterPosition();
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPos);
                listener.onColorClick(colorHex);
            });
        }
        @Override public int getItemCount() { return colors.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder { ViewHolder(android.view.View v) { super(v); } }
    }
}