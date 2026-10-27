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
    private long editTransactionId = -1; // -1 nghĩa là đang ở chế độ Thêm mới
    private TransactionEntity existingTransaction; // Lưu trữ dữ liệu giao dịch cũ
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
        // Mặc định tải danh mục cho tab hiện tại
        loadCategories(currentType);

        // --- BẮT ĐẦU ĐOẠN CODE THÊM MỚI ---
        editTransactionId = getIntent().getLongExtra("EDIT_TRANSACTION_ID", -1);
        if (editTransactionId != -1) {
            btnSaveTransaction.setText("Cập nhật giao dịch");
            loadTransactionForEdit();
        }
        // --- KẾT THÚC ĐOẠN CODE THÊM MỚI ---

        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }
    private void loadTransactionForEdit() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            // Lấy giao dịch cũ từ Room (Đảm bảo TransactionDao đã có hàm getTransactionById)
            existingTransaction = db.transactionDao().getTransactionById(editTransactionId);

            if (existingTransaction != null) {
                // Lấy thêm thông tin danh mục để biết là Thu nhập hay Chi tiêu
                CategoryEntity cat = db.categoryDao().getCategoryById(existingTransaction.categoryID);

                runOnUiThread(() -> {
                    // 1. Điền lại số tiền cũ
                    if (existingTransaction.amount != null) {
                        rawAmountBuilder.setLength(0);
                        rawAmountBuilder.append(existingTransaction.amount.toBigInteger().toString());
                        updateAmountDisplay();
                    }

                    // 2. Điền lại ghi chú cũ
                    if (existingTransaction.note != null) {
                        edtTransactionNote.setText(existingTransaction.note);
                    }

                    // 3. Đặt lại ngày giao dịch cũ
                    selectedDateMillis = existingTransaction.transactionDate;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    tvTransactionDate.setText(sdf.format(new java.util.Date(selectedDateMillis)));

                    // 4. Chuyển đúng Tab (Thu/Chi) và gắn lại Danh mục
                    if (cat != null) {
                        switchTab(cat.type);
                        selectedCategoryId = cat.categoryId;
                        // Nếu Adapter có hàm setSelectedCategoryId, gọi ở đây để bôi đậm danh mục
                    }
                });
            }
        }).start();
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
        // Lưu giao dịch sau khi kiểm tra dữ liệu
            // 1. Kiểm tra số tiền
            if (rawAmountBuilder.length() == 0) {
                Toast.makeText(
                        this,
                        "Vui lòng nhập số tiền",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            BigDecimal amountDecimal;

            try {
                amountDecimal = new BigDecimal(rawAmountBuilder.toString());
            } catch (NumberFormatException e) {
                Toast.makeText(
                        this,
                        "Số tiền không hợp lệ",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (amountDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(
                        this,
                        "Số tiền phải lớn hơn 0",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // 2. Kiểm tra giới hạn số tiền
            if (rawAmountBuilder.length() > 12) {
                Toast.makeText(
                        this,
                        "Số tiền vượt quá giới hạn cho phép",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // 3. Kiểm tra danh mục đã được chọn
            CategoryEntity selectedCategory =
                    categorySelectAdapter.getSelectedCategory();

            if (selectedCategory == null || selectedCategoryId <= 0) {
                Toast.makeText(
                        this,
                        "Vui lòng chọn danh mục",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // 4. Kiểm tra loại giao dịch
            if (selectedCategory.type == null
                    || !selectedCategory.type.equals(currentType)) {
                Toast.makeText(
                        this,
                        "Danh mục không phù hợp với loại giao dịch",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // 5. Kiểm tra ghi chú
            String note = edtTransactionNote.getText().toString().trim();

            if (note.length() > 200) {
                edtTransactionNote.setError(
                        "Ghi chú không được vượt quá 200 ký tự"
                );
                edtTransactionNote.requestFocus();
                return;
            }

            // 6. Kiểm tra ngày giao dịch
            if (selectedDateMillis <= 0) {
                Toast.makeText(
                        this,
                        "Ngày giao dịch không hợp lệ",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Lưu các giá trị hiện tại trước khi chạy luồng nền
            final long categoryIdToSave = selectedCategoryId;
            final String typeToSave = currentType;
            final long dateToSave = selectedDateMillis;

            // Tránh người dùng nhấn Lưu nhiều lần
            btnSaveTransaction.setEnabled(false);

            new Thread(() -> {

                AppDatabase db = AppDatabase.getDatabase(
                        getApplicationContext()
                );

                try {
                    // 7. Kiểm tra người dùng
                    SharedPreferences prefs = getSharedPreferences(
                            "ExpenseAppPrefs",
                            Context.MODE_PRIVATE
                    );

                    String userEmail = prefs.getString("USER_EMAIL", "");

                    if (userEmail == null || userEmail.trim().isEmpty()) {
                        runOnUiThread(() -> {
                            btnSaveTransaction.setEnabled(true);

                            Toast.makeText(
                                    AddTransactionActivity.this,
                                    "Vui lòng đăng nhập trước khi lưu giao dịch",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                        return;
                    }

                    UserEntity user = db.userDao().getUserByEmail(userEmail);

                    if (user == null || user.userId <= 0) {
                        runOnUiThread(() -> {
                            btnSaveTransaction.setEnabled(true);

                            Toast.makeText(
                                    AddTransactionActivity.this,
                                    "Không tìm thấy tài khoản hợp lệ",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                        return;
                    }

                    // 8. Kiểm tra danh mục vẫn tồn tại trong database
                    CategoryEntity category =
                            db.categoryDao().getCategoryById(categoryIdToSave);

                    if (category == null) {
                        runOnUiThread(() -> {
                            btnSaveTransaction.setEnabled(true);

                            Toast.makeText(
                                    AddTransactionActivity.this,
                                    "Danh mục không còn tồn tại",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                        return;
                    }

                    // 9. Kiểm tra danh mục phù hợp với loại giao dịch
                    if (category.type == null
                            || !category.type.equals(typeToSave)) {

                        runOnUiThread(() -> {
                            btnSaveTransaction.setEnabled(true);

                            Toast.makeText(
                                    AddTransactionActivity.this,
                                    "Danh mục không phù hợp với loại giao dịch",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                        return;
                    }

                    // 10. Kiểm tra quyền sử dụng danh mục
                    if (category.userId != null
                            && category.userId != -1
                            && category.userId != user.userId) {

                        runOnUiThread(() -> {
                            btnSaveTransaction.setEnabled(true);

                            Toast.makeText(
                                    AddTransactionActivity.this,
                                    "Bạn không thể sử dụng danh mục này",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                        return;
                    }

                    // 11. Chỉ lưu khi tất cả kiểm tra đều hợp lệ

                    if (editTransactionId != -1 && existingTransaction != null) {
                        // CHẾ ĐỘ CẬP NHẬT GIAO DỊCH
                        existingTransaction.amount = amountDecimal;
                        existingTransaction.categoryID = categoryIdToSave;
                        existingTransaction.note = note;
                        existingTransaction.transactionDate = dateToSave;

                        db.transactionDao().updateTransaction(existingTransaction);

                        runOnUiThread(() -> {
                            Toast.makeText(AddTransactionActivity.this, "Đã cập nhật giao dịch thành công!", Toast.LENGTH_SHORT).show();
                            finish(); // Sửa xong thì đóng màn hình về lại trang chủ
                        });
                    } else {
                        // CHẾ ĐỘ THÊM MỚI GIAO DỊCH (Giữ nguyên code cũ của bạn)
                        TransactionEntity transaction = new TransactionEntity(
                                user.userId,
                                categoryIdToSave,
                                amountDecimal,
                                note,
                                dateToSave,
                                System.currentTimeMillis()
                        );

                        db.transactionDao().insertTransaction(transaction);

                        runOnUiThread(() -> {
                            String message = typeToSave.equals("Income")
                                    ? "Đã lưu thu nhập thành công!"
                                    : "Đã lưu chi tiêu thành công!";
                            Toast.makeText(AddTransactionActivity.this, message, Toast.LENGTH_SHORT).show();
                            resetInputFields();
                            btnSaveTransaction.setEnabled(true);
                        });
                    }

                } catch (Exception e) {

                    runOnUiThread(() -> {
                        btnSaveTransaction.setEnabled(true);

                        Toast.makeText(
                                AddTransactionActivity.this,
                                "Không thể lưu giao dịch. Vui lòng thử lại.",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                }

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