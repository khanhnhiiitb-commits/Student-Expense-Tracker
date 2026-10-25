package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.CategorySelectAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView btnClose;
    private TextView tabExpense, tabIncome;
    private TextView tvAmount;
    private RecyclerView rcvTransactionCategories;
    private LinearLayout btnSelectDate;
    private TextView tvTransactionDate;
    private EditText edtTransactionNote;
    private AppCompatButton btnSaveTransaction;

    private CategorySelectAdapter categorySelectAdapter;
    private String currentType = "Expense"; // Mặc định là Expense
    private final StringBuilder rawAmountBuilder = new StringBuilder();
    private long selectedCategoryId = -1;
    private long selectedDateMillis = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initViews();
        setupKeypad();
        setupTabs();
        setupDatePicker();
        setupCategoryList();

        // Mặc định tải danh mục cho tab hiện tại
        loadCategories(currentType);

        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void initViews() {
        btnClose = findViewById(R.id.btnClose);
        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);
        tvAmount = findViewById(R.id.tvAmount);
        rcvTransactionCategories = findViewById(R.id.rcvTransactionCategories);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvTransactionDate = findViewById(R.id.tvTransactionDate);
        edtTransactionNote = findViewById(R.id.edtTransactionNote);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);

        btnClose.setOnClickListener(v -> finish());
    }

    // 1. Xử lý bàn phím số
    private void setupKeypad() {
        int[] numButtonIds = {
                R.id.btnNum1, R.id.btnNum2, R.id.btnNum3,
                R.id.btnNum4, R.id.btnNum5, R.id.btnNum6,
                R.id.btnNum7, R.id.btnNum8, R.id.btnNum9,
                R.id.btnNum0
        };

        for (int id : numButtonIds) {
            findViewById(id).setOnClickListener(v -> {
                TextView btn = (TextView) v;
                String digit = btn.getText().toString();
                // Không thêm 0 nếu đang rỗng
                if (rawAmountBuilder.length() == 0 && digit.equals("0")) return;

                if (rawAmountBuilder.length() < 12) {
                    rawAmountBuilder.append(digit);
                    updateAmountDisplay();
                }
            });
        }

        // Nút 000
        findViewById(R.id.btnNum000).setOnClickListener(v -> {
            if (rawAmountBuilder.length() > 0 && rawAmountBuilder.length() <= 9) {
                rawAmountBuilder.append("000");
                updateAmountDisplay();
            }
        });

        // Nút xóa (Backspace)
        findViewById(R.id.btnBackspace).setOnClickListener(v -> {
            if (rawAmountBuilder.length() > 0) {
                rawAmountBuilder.deleteCharAt(rawAmountBuilder.length() - 1);
                updateAmountDisplay();
            }
        });
    }

    private void updateAmountDisplay() {
        if (rawAmountBuilder.length() == 0) {
            tvAmount.setText("0 đ");
            return;
        }
        try {
            double value = Double.parseDouble(rawAmountBuilder.toString());
            DecimalFormat formatter = new DecimalFormat("#,###");
            tvAmount.setText(formatter.format(value) + " đ");
        } catch (NumberFormatException ignored) {}
    }

    // 2. Chuyển Tab Chi tiêu / Thu nhập
    private void setupTabs() {
        tabExpense.setOnClickListener(v -> switchTab("Expense"));
        tabIncome.setOnClickListener(v -> switchTab("Income"));
    }

    private void switchTab(String selectedType) {
        if (currentType.equals(selectedType)) return;
        currentType = selectedType;

        if (selectedType.equals("Expense")) {
            tabExpense.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tabExpense.setTextColor(Color.parseColor("#2563EB"));
            tabIncome.setBackgroundColor(Color.TRANSPARENT);
            tabIncome.setTextColor(Color.parseColor("#64748B"));
            tvAmount.setTextColor(Color.parseColor("#2563EB"));
        } else {
            tabIncome.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tabIncome.setTextColor(Color.parseColor("#16A34A")); // Xanh lá cho Thu nhập
            tabExpense.setBackgroundColor(Color.TRANSPARENT);
            tabExpense.setTextColor(Color.parseColor("#64748B"));
            tvAmount.setTextColor(Color.parseColor("#16A34A"));
        }

        loadCategories(currentType);
    }

    // 3. Chọn ngày giao dịch
    private void setupDatePicker() {
        Calendar calendar = Calendar.getInstance();
        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                selectedDateMillis = calendar.getTimeInMillis();

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvTransactionDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    // 4. Khởi tạo danh sách danh mục
    private void setupCategoryList() {
        categorySelectAdapter = new CategorySelectAdapter(this, category -> {
            if (category != null) {
                selectedCategoryId = category.categoryId;
            }
        });
        rcvTransactionCategories.setAdapter(categorySelectAdapter);
    }

    private void loadCategories(String type) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("USER_EMAIL", "");
            int currentUserId = -1;
            if (!userEmail.isEmpty()) {
                UserEntity user = db.userDao().getUserByEmail(userEmail);
                if (user != null) {
                    currentUserId = user.userId;
                }
            }

            List<CategoryEntity> list = db.categoryDao().getCategoriesByType(type, currentUserId);

            runOnUiThread(() -> {
                // Khử trùng theo tên danh mục
                Map<String, CategoryEntity> uniqueMap = new LinkedHashMap<>();
                if (list != null) {
                    for (CategoryEntity cat : list) {
                        if (cat.categoryName != null && !uniqueMap.containsKey(cat.categoryName.trim())) {
                            uniqueMap.put(cat.categoryName.trim(), cat);
                        }
                    }
                }
                categorySelectAdapter.setData(new java.util.ArrayList<>(uniqueMap.values()));

                CategoryEntity selected = categorySelectAdapter.getSelectedCategory();
                if (selected != null) {
                    selectedCategoryId = selected.categoryId;
                }
            });
        }).start();
    }

    // 5. Lưu giao dịch Thu nhập / Chi tiêu và tiếp tục ghi
    private void saveTransaction() {
        if (rawAmountBuilder.length() == 0) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal amountDecimal;
        try {
            amountDecimal = new BigDecimal(rawAmountBuilder.toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountDecimal.compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryId <= 0) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = edtTransactionNote.getText().toString().trim();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("USER_EMAIL", "");
            int currentUserId = -1;
            if (!userEmail.isEmpty()) {
                UserEntity user = db.userDao().getUserByEmail(userEmail);
                if (user != null) {
                    currentUserId = user.userId;
                }
            }

            // Tạo đối tượng TransactionEntity khớp đúng 6 tham số
            TransactionEntity transaction = new TransactionEntity(
                    currentUserId,              // 1. long userID
                    selectedCategoryId,         // 2. long categoryID
                    amountDecimal,              // 3. BigDecimal amount
                    note,                       // 4. String note
                    selectedDateMillis,         // 5. long transactionDate
                    System.currentTimeMillis()   // 6. long createdAt
            );

            db.transactionDao().insertTransaction(transaction);

            runOnUiThread(() -> {
                String message = currentType.equals("Income") ? "Đã lưu thu nhập thành công!" : "Đã lưu chi tiêu thành công!";
                Toast.makeText(AddTransactionActivity.this, message, Toast.LENGTH_SHORT).show();

                // Làm mới ô nhập liệu để sẵn sàng ghi tiếp
                resetInputFields();
            });
        }).start();
    }

    // 6. Đưa các ô nhập liệu về trạng thái ban đầu
    private void resetInputFields() {
        rawAmountBuilder.setLength(0);
        tvAmount.setText("0 đ");
        edtTransactionNote.setText("");
        edtTransactionNote.clearFocus();
    }
}