package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao.CategoryDao;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class AddEditCategoryActivity extends AppCompatActivity {

    private EditText edtCategoryName;
    private TextView tabCatExpense;
    private TextView tabCatIncome;
    private AppCompatButton btnSaveCategory;

    private String selectedType = "Expense"; // Mặc định là Chi tiêu
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        // Nhận dữ liệu loại danh mục nếu được truyền sang từ CategoryActivity
        String typeFromIntent = getIntent().getStringExtra("CATEGORY_TYPE");
        if (typeFromIntent != null) {
            selectedType = typeFromIntent;
        }

        // Khởi tạo Database
        db = AppDatabase.getDatabase(getApplicationContext());

        edtCategoryName = findViewById(R.id.edtCategoryName);
        tabCatExpense = findViewById(R.id.tabTypeExpense);
        tabCatIncome = findViewById(R.id.tabTypeIncome);
        ImageButton btnBackCategory = findViewById(R.id.btnBack);
        btnSaveCategory = findViewById(R.id.btnCreateCategoryBottom);

        // Thiết lập trạng thái hiển thị ban đầu của 2 tab
        switch (selectedType) {
            case "Income":
                selectIncomeType();
                break;
            case "Expense":
            default:
                selectExpenseType();
                break;
        }

        // Sự kiện chuyển Tab
        tabCatExpense.setOnClickListener(v -> selectExpenseType());
        tabCatIncome.setOnClickListener(v -> selectIncomeType());

        // Sự kiện quay lại
        btnBackCategory.setOnClickListener(v -> finish());

        // Sự kiện lưu danh mục
        btnSaveCategory.setOnClickListener(v -> saveCategory());
    }

    private void selectExpenseType() {
        selectedType = "Expense";
        tabCatExpense.setBackgroundColor(0xFF2563EB); // Màu xanh dương
        tabCatExpense.setTextColor(0xFFFFFFFF);      // Chữ trắng
        tabCatIncome.setBackgroundColor(0x00000000);  // Trong suốt
        tabCatIncome.setTextColor(0xFF64748B);       // Chữ xám
    }

    private void selectIncomeType() {
        selectedType = "Income";
        tabCatIncome.setBackgroundColor(0xFF2563EB);  // Màu xanh dương
        tabCatIncome.setTextColor(0xFFFFFFFF);       // Chữ trắng
        tabCatExpense.setBackgroundColor(0x00000000); // Trong suốt
        tabCatExpense.setTextColor(0xFF64748B);      // Chữ xám
    }

    private void saveCategory() {
        String categoryName = edtCategoryName.getText().toString().trim();

        if (categoryName.isEmpty()) {
            edtCategoryName.setError("Vui lòng nhập tên danh mục");
            edtCategoryName.requestFocus();
            return;
        }

        if (categoryName.length() > 50) {
            edtCategoryName.setError("Tên danh mục không được vượt quá 50 ký tự");
            return;
        }

        btnSaveCategory.setEnabled(false);

        executorService.execute(() -> {
            CategoryDao categoryDao = db.categoryDao();

            // Lấy currentUserId từ SharedPreferences
            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("USER_EMAIL", "");
            long currentUserId = -1;
            if (!userEmail.isEmpty()) {
                UserEntity user = db.userDao().getUserByEmail(userEmail);
                if (user != null) {
                    currentUserId = user.userId;
                }
            }

            // Kiểm tra trùng tên danh mục
            int count = categoryDao.countCategoryByName(categoryName, selectedType, (int) currentUserId);
            if (count > 0) {
                runOnUiThread(() -> {
                    Toast.makeText(AddEditCategoryActivity.this, "Danh mục này đã tồn tại!", Toast.LENGTH_SHORT).show();
                    btnSaveCategory.setEnabled(true);
                });
                return;
            }

            // Tạo và lưu Entity theo đúng 3 tham số (userId, categoryName, type)
            CategoryEntity category = new CategoryEntity(currentUserId, categoryName, selectedType);
            categoryDao.insertCategory(category);

            runOnUiThread(() -> {
                Toast.makeText(AddEditCategoryActivity.this, "Thêm danh mục thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
