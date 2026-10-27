package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.CategoryAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class CategoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tabExpense, tabIncome;
    private EditText edtSearchCategory;
    private AppCompatButton btnCreateCategory;
    private RecyclerView rcvCategories;

    private CategoryAdapter categoryAdapter;
    private List<CategoryEntity> fullCategoryList = new ArrayList<>();
    private String currentType = "Expense";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        // 1. Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);
        edtSearchCategory = findViewById(R.id.edtSearchCategory);
        btnCreateCategory = findViewById(R.id.btnCreateCategory);
        rcvCategories = findViewById(R.id.rcvCategories);

        // 2. Thiết lập RecyclerView & lắng nghe sự kiện
        rcvCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, new ArrayList<>(), item -> {
            Toast.makeText(CategoryActivity.this, "Đã chọn: " + item.categoryName, Toast.LENGTH_SHORT).show();
        });

        // ĐIỂM QUAN TRỌNG: Kết nối ActionListener để xử lý Sửa (TC-CAT-06) & Xóa (TC-CAT-07)
        categoryAdapter.setActionListener(new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(CategoryEntity item) {
                editCategory(item);
            }

            @Override
            public void onDelete(CategoryEntity item) {
                confirmAndDeleteCategory(item);
            }
        });

        rcvCategories.setAdapter(categoryAdapter);

        // 3. Sự kiện nút Back & Tab
        btnBack.setOnClickListener(v -> finish());
        tabExpense.setOnClickListener(v -> switchTab("Expense"));
        tabIncome.setOnClickListener(v -> switchTab("Income"));

        // 4. Mở màn hình tạo mới
        btnCreateCategory.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, AddCategoryActivity.class);
            intent.putExtra("CATEGORY_TYPE", currentType);
            startActivity(intent);
        });

        // 5. Lắng nghe tìm kiếm danh mục
        edtSearchCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories(currentType);
    }

    // Xử lý chỉnh sửa danh mục tùy chỉnh (TC-CAT-06)
    private void editCategory(CategoryEntity category) {
        if (category.userId == null || category.userId <= 0) {
            Toast.makeText(this, "Không thể chỉnh sửa danh mục mặc định của hệ thống!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CategoryActivity.this, AddCategoryActivity.class);
        intent.putExtra("EDIT_CATEGORY_ID", category.categoryId);
        intent.putExtra("CATEGORY_NAME", category.categoryName);
        intent.putExtra("CATEGORY_TYPE", category.type);
        startActivity(intent);
    }

    // Hộp thoại xác nhận và xóa an toàn (TC-CAT-07)
    private void confirmAndDeleteCategory(CategoryEntity category) {
        if (category.userId == null || category.userId <= 0) {
            Toast.makeText(this, "Không thể xóa danh mục mặc định của hệ thống!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục \"" + category.categoryName + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCategory(CategoryEntity category) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            long catId = category.categoryId;

            // Kiểm tra ràng buộc toàn vẹn: nếu đã có giao dịch gắn danh mục này thì chặn
            int txCount = db.categoryDao().countTransactionsUsingCategory(catId);
            if (txCount > 0) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Danh mục đang có " + txCount + " giao dịch liên kết. Không thể xóa!", Toast.LENGTH_LONG).show()
                );
                return;
            }

            // Thực hiện xóa khỏi Room
            db.categoryDao().deleteCategory(category);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xóa danh mục thành công!", Toast.LENGTH_SHORT).show();
                loadCategories(currentType); // Tải lại danh sách danh mục
            });
        }).start();
    }

    private void switchTab(String selectedType) {
        if (currentType.equals(selectedType)) return;
        currentType = selectedType;

        if (selectedType.equals("Expense")) {
            tabExpense.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tabExpense.setTextColor(Color.parseColor("#2563EB"));
            tabIncome.setBackgroundColor(Color.TRANSPARENT);
            tabIncome.setTextColor(Color.parseColor("#64748B"));
        } else {
            tabIncome.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tabIncome.setTextColor(Color.parseColor("#2563EB"));
            tabExpense.setBackgroundColor(Color.TRANSPARENT);
            tabExpense.setTextColor(Color.parseColor("#64748B"));
        }

        edtSearchCategory.setText("");
        loadCategories(currentType);
    }

    private void filterCategories(String query) {
        if (query.trim().isEmpty()) {
            categoryAdapter.setData(fullCategoryList);
            return;
        }

        List<CategoryEntity> filteredList = new ArrayList<>();
        for (CategoryEntity item : fullCategoryList) {
            if (item.categoryName != null && item.categoryName.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        categoryAdapter.setData(filteredList);
    }

    private void loadCategories(String type) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

            // Tự động seed dữ liệu mặc định nếu database trống
            seedDefaultCategoriesIfNeeded(db);

            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("USER_EMAIL", "");

            int currentUserId = -1;
            if (!userEmail.isEmpty()) {
                UserEntity user = db.userDao().getUserByEmail(userEmail);
                if (user != null) {
                    currentUserId = user.userId;
                }
            }

            // Truy vấn danh mục từ Room
            List<CategoryEntity> list = db.categoryDao().getCategoriesByType(type, currentUserId);

            runOnUiThread(() -> {
                fullCategoryList.clear();
                if (list != null) {
                    Map<String, CategoryEntity> uniqueMap = new LinkedHashMap<>();
                    for (CategoryEntity item : list) {
                        if (item.categoryName != null && !uniqueMap.containsKey(item.categoryName.trim())) {
                            uniqueMap.put(item.categoryName.trim(), item);
                        }
                    }
                    fullCategoryList.addAll(uniqueMap.values());
                }
                categoryAdapter.setData(fullCategoryList);
            });
        }).start();
    }

    private synchronized void seedDefaultCategoriesIfNeeded(AppDatabase db) {
        List<CategoryEntity> existing = db.categoryDao().getCategoriesByType("Expense", -1);
        if (existing != null && !existing.isEmpty()) {
            return;
        }

        List<CategoryEntity> defaults = new ArrayList<>();

        // 9 Danh mục Chi tiêu mặc định
        defaults.add(createCategory("Ăn uống", "Expense"));
        defaults.add(createCategory("Học tập", "Expense"));
        defaults.add(createCategory("Đi lại", "Expense"));
        defaults.add(createCategory("Nhà ở", "Expense"));
        defaults.add(createCategory("Mua sắm", "Expense"));
        defaults.add(createCategory("Giải trí", "Expense"));
        defaults.add(createCategory("Sức khỏe", "Expense"));
        defaults.add(createCategory("Hóa đơn và dịch vụ", "Expense"));
        defaults.add(createCategory("Chi tiêu khác", "Expense"));

        // 4 Danh mục Thu nhập mặc định
        defaults.add(createCategory("Lương / Làm thêm", "Income"));
        defaults.add(createCategory("Trợ cấp gia đình", "Income"));
        defaults.add(createCategory("Học bổng", "Income"));
        defaults.add(createCategory("Thu nhập khác", "Income"));

        for (CategoryEntity cat : defaults) {
            db.categoryDao().insertCategory(cat);
        }
    }

    private CategoryEntity createCategory(String name, String type) {
        return new CategoryEntity(null, name, type);
    }
}