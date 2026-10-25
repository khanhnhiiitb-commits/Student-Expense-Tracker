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

        // 2. Thiết lập RecyclerView
        rcvCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, new ArrayList<>(), item -> {
            Toast.makeText(CategoryActivity.this, "Đã chọn: " + item.categoryName, Toast.LENGTH_SHORT).show();
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

        // Chỉ để onResume() đảm nhiệm việc tải dữ liệu, tránh race condition ở onCreate
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories(currentType);
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
                    // Dùng LinkedHashMap lọc trùng lặp theo categoryName ngay tại Client
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
            return; // Đã có dữ liệu thì dừng ngay lập tức
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

    // Hàm tiện ích khởi tạo CategoryEntity theo constructor (Long userId, String categoryName, String type)
    private CategoryEntity createCategory(String name, String type) {
        return new CategoryEntity(null, name, type);
    }
}