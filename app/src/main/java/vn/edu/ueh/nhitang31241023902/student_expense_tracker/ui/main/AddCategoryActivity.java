package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao.CategoryDao;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText edtCategoryName;

    // Khai báo đúng kiểu TextView theo XML
    private TextView tabTypeExpense;
    private TextView tabTypeIncome;

    private TextView btnBack;
    private AppCompatButton btnCreateCategoryBottom;

    private String selectedType = "Expense"; // Mặc định ban đầu là Chi tiêu
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nhận dữ liệu loại danh mục được truyền sang từ CategoryActivity (nếu có)
        String typeFromIntent = getIntent().getStringExtra("CATEGORY_TYPE");
        if (typeFromIntent != null) {
            selectedType = typeFromIntent;
        }

        // Khởi tạo Database
        db = AppDatabase.getDatabase(getApplicationContext());

        // Ánh xạ các View chuẩn theo XML
        edtCategoryName = findViewById(R.id.edtCategoryName);
        tabTypeExpense = findViewById(R.id.tabTypeExpense);
        tabTypeIncome = findViewById(R.id.tabTypeIncome);
        btnBack = findViewById(R.id.btnBack);
        btnCreateCategoryBottom = findViewById(R.id.btnCreateCategoryBottom);

        // Thiết lập trạng thái giao diện Tab ban đầu dựa trên selectedType
        if (selectedType.equals("Expense")) {
            selectExpenseType();
        } else {
            selectIncomeType();
        }

        // Bắt sự kiện chọn tab Chi tiêu / Thu nhập
        tabTypeExpense.setOnClickListener(v -> selectExpenseType());
        tabTypeIncome.setOnClickListener(v -> selectIncomeType());

        // Quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());

        // Nút lưu vào Database ở đáy màn hình
        btnCreateCategoryBottom.setOnClickListener(v -> saveCategory());
    }

    // Giao diện chọn Chi tiêu
    private void selectExpenseType() {
        selectedType = "Expense";
        tabTypeExpense.setBackgroundColor(0xFF2563EB); // Xanh dương chủ đạo
        tabTypeExpense.setTextColor(0xFFFFFFFF);      // Chữ trắng
        tabTypeIncome.setBackgroundColor(0x00000000);  // Trong suốt
        tabTypeIncome.setTextColor(0xFF64748B);       // Chữ xám
    }

    // Giao diện chọn Thu nhập
    private void selectIncomeType() {
        selectedType = "Income";
        tabTypeIncome.setBackgroundColor(0xFF2563EB);  // Xanh dương chủ đạo
        tabTypeIncome.setTextColor(0xFFFFFFFF);       // Chữ trắng
        tabTypeExpense.setBackgroundColor(0x00000000); // Trong suốt
        tabTypeExpense.setTextColor(0xFF64748B);      // Chữ xám
    }

    // Xử lý logic lưu danh mục vào Room Database
    private void saveCategory() {
        String categoryName = edtCategoryName.getText().toString().trim();

        // Kiểm tra tên rỗng
        if (categoryName.isEmpty()) {
            edtCategoryName.setError("Vui lòng nhập tên danh mục");
            edtCategoryName.requestFocus();
            return;
        }

        // Giới hạn ký tự
        if (categoryName.length() > 50) {
            edtCategoryName.setError("Tên danh mục không được vượt quá 50 ký tự");
            return;
        }

        // Khóa nút bấm tránh click nhiều lần liên tiếp
        btnCreateCategoryBottom.setEnabled(false);

        executorService.execute(() -> {
            CategoryDao categoryDao = db.categoryDao();

            // Lấy userId hiện tại từ SharedPreferences
            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("USER_EMAIL", "");
            int currentUserId = -1;
            if (!userEmail.isEmpty()) {
                UserEntity user = db.userDao().getUserByEmail(userEmail);
                if (user != null) {
                    currentUserId = user.userId;
                }
            }

            // Kiểm tra trùng tên danh mục trong cơ sở dữ liệu
            int count = categoryDao.countCategoryByName(categoryName, selectedType, currentUserId);

            if (count > 0) {
                runOnUiThread(() -> {
                    Toast.makeText(AddCategoryActivity.this, "Danh mục này đã tồn tại!", Toast.LENGTH_SHORT).show();
                    btnCreateCategoryBottom.setEnabled(true);
                });
                return;
            }

            // Tạo đối tượng CategoryEntity mới (gắn userId tùy chỉnh)
            CategoryEntity category = new CategoryEntity((long) currentUserId, categoryName, selectedType);

            // Chèn vào Room Database
            categoryDao.insertCategory(category);

            runOnUiThread(() -> {
                Toast.makeText(AddCategoryActivity.this, "Thêm danh mục thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Đóng Activity và quay về màn hình danh sách danh mục
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}