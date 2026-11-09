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
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.NotificationHelper;
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
import java.util.ArrayList;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.BudgetEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BudgetWarningUtils;
public class AddTransactionActivity extends AppCompatActivity {

    private TextView btnClose;
    private TextView tabExpense, tabIncome;
    private TextView tvAmount;
    private RecyclerView rcvTransactionCategories;
    private LinearLayout btnSelectDate;
    private TextView tvTransactionDate;
    private EditText edtTransactionNote;
    private AppCompatButton btnSaveTransaction;
    private int categoryLoadVersion = 0;
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


        // --- BẮT ĐẦU ĐOẠN CODE THÊM MỚI ---
        editTransactionId = getIntent()
                .getLongExtra("EDIT_TRANSACTION_ID", -1);

        if (editTransactionId != -1) {
            btnSaveTransaction.setText("Cập nhật giao dịch");
            btnSaveTransaction.setEnabled(false);
            loadTransactionForEdit();
        } else {
            loadCategories(currentType);
        }
        // --- KẾT THÚC ĐOẠN CODE THÊM MỚI ---

        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }


    private void loadTransactionForEdit() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(
                        getApplicationContext()
                );

                SharedPreferences prefs = getSharedPreferences(
                        "ExpenseAppPrefs",
                        Context.MODE_PRIVATE
                );

                String email = prefs.getString("USER_EMAIL", "");

                if (email == null || email.trim().isEmpty()) {
                    showCategoryLoadError("Không thể chỉnh sửa giao dịch");
                    return;
                }

                UserEntity user = db.userDao().getUserByEmail(email);

                if (user == null || user.userId <= 0) {
                    showEditLoadError("Không tìm thấy tài khoản");
                    return;
                }

                TransactionEntity transaction =
                        db.transactionDao()
                                .getTransactionById(editTransactionId);

                if (transaction == null
                        || transaction.userID != user.userId) {
                    showEditLoadError(
                            "Giao dịch không tồn tại hoặc không thuộc tài khoản này"
                    );
                    return;
                }

                CategoryEntity category =
                        db.categoryDao()
                                .getCategoryById(transaction.categoryID);

                if (category == null
                        || category.type == null
                        || (!"Expense".equals(category.type)
                        && !"Income".equals(category.type))) {
                    showEditLoadError(
                            "Không tìm thấy danh mục hợp lệ của giao dịch"
                    );
                    return;
                }

                if (category.userId != null
                        && category.userId != -1
                        && category.userId != user.userId) {
                    showEditLoadError(
                            "Bạn không có quyền sử dụng danh mục này"
                    );
                    return;
                }

                if (transaction.amount == null
                        || transaction.amount.signum() <= 0
                        || transaction.amount.scale() > 0
                        || transaction.amount.toPlainString()
                        .replace(".", "").length() > 12) {
                    showEditLoadError(
                            "Số tiền giao dịch không phù hợp với biểu mẫu hiện tại"
                    );
                    return;
                }

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    existingTransaction = transaction;

                    rawAmountBuilder.setLength(0);
                    rawAmountBuilder.append(
                            transaction.amount.toBigIntegerExact()
                                    .toString()
                    );
                    updateAmountDisplay();

                    edtTransactionNote.setText(
                            transaction.note != null
                                    ? transaction.note : ""
                    );

                    selectedDateMillis = transaction.transactionDate;

                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    );

                    tvTransactionDate.setText(
                            sdf.format(
                                    new java.util.Date(selectedDateMillis)
                            )
                    );

                    // Khôi phục đúng danh mục theo ID.

                    if (!currentType.equals(category.type)) {
                        switchTab(category.type);

                        categorySelectAdapter.setSelectedCategoryId(
                                category.categoryId
                        );
                    } else {
                        categorySelectAdapter.setSelectedCategoryId(
                                category.categoryId
                        );

                        loadCategories(category.type);
                    }

                    // Chưa bật Lưu ở đây.
                    // Chỉ bật sau khi danh sách danh mục tải xong.
                });

            } catch (Exception e) {
                android.util.Log.e(
                        "TransactionEdit",
                        "Lỗi tải giao dịch",
                        e
                );

                showEditLoadError(
                        "Không thể tải giao dịch để chỉnh sửa"
                );
            }
        }).start();
    }

    private void showEditLoadError(String message) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            btnSaveTransaction.setEnabled(false);

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Không thể chỉnh sửa giao dịch")
                    .setMessage(message)
                    .setPositiveButton(
                            "Đóng",
                            (dialog, which) -> finish()
                    )
                    .setOnCancelListener(dialog -> finish())
                    .show();
        });
    }


    // Gọi trên background thread, TRƯỚC khi ghi giao dịch vào Room.
    // Chỉ cảnh báo khi danh mục mới tăng qua một mức cảnh báo.
    private String evaluateBudgetAlert(AppDatabase db, long userId,
                                       long categoryId, long transactionDate, long newAmount,
                                       TransactionEntity oldTransaction) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(transactionDate);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        BudgetEntity budget = db.budgetDao().getBudgetForCategoryAndMonth(userId, categoryId, month, year);
        if (budget == null || budget.amount <= 0) return null;

        long spentBefore = 0;
        List<TransactionEntity> transactions = db.transactionDao().getTransactionsByUserId(userId);
        for (TransactionEntity tx : transactions) {
            if (tx.categoryID != categoryId || tx.amount == null) continue;
            cal.setTimeInMillis(tx.transactionDate);
            if (cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year) {
                spentBefore = Math.addExact(spentBefore, tx.amount.longValueExact());
            }
        }

        long spentAfter = spentBefore;
        // Nếu đang sửa một giao dịch thuộc cùng danh mục/tháng, bỏ khoản cũ trước khi cộng khoản mới.
        if (oldTransaction != null && oldTransaction.categoryID == categoryId && oldTransaction.amount != null) {
            cal.setTimeInMillis(oldTransaction.transactionDate);
            if (cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.YEAR) == year) {
                spentAfter = Math.subtractExact(spentAfter, oldTransaction.amount.longValueExact());
            }
        }
        spentAfter = Math.addExact(spentAfter, newAmount);

        BudgetWarningUtils.WarningLevel before = BudgetWarningUtils.checkBudget(budget.amount, spentBefore);
        BudgetWarningUtils.WarningLevel after = BudgetWarningUtils.checkBudget(budget.amount, spentAfter);
        if (!BudgetWarningUtils.shouldAlert(before, after)) return null;
        DecimalFormat fmt = new DecimalFormat("#,###");
        return BudgetWarningUtils.getMessage(after) + "\nĐã chi: "
                + fmt.format(spentAfter) + " / " + fmt.format(budget.amount) + " đ";
    }

    private void showAlertIfNeeded(String warning) {
        if (warning == null || isFinishing() || isDestroyed()) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cảnh báo ngân sách")
                .setMessage(warning)
                .setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss())
                .show();
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
            BigDecimal value =
                    new BigDecimal(rawAmountBuilder.toString());

            DecimalFormat formatter =
                    new DecimalFormat("#,###");

            tvAmount.setText(
                    formatter.format(value) + " đ"
            );

        } catch (NumberFormatException e) {
            tvAmount.setText("0 đ");
        }
    }


    // 2. Chuyển Tab Chi tiêu / Thu nhập
    private void setupTabs() {
        tabExpense.setOnClickListener(v -> switchTab("Expense"));
        tabIncome.setOnClickListener(v -> switchTab("Income"));
    }


    private void switchTab(String selectedType) {
        if (currentType.equals(selectedType)) {
            return;
        }

        currentType = selectedType;

        categorySelectAdapter.clearSelection();
        selectedCategoryId = -1;

        if ("Expense".equals(selectedType)) {
            tabExpense.setBackgroundColor(Color.WHITE);
            tabExpense.setTextColor(Color.parseColor("#2563EB"));

            tabIncome.setBackgroundColor(Color.TRANSPARENT);
            tabIncome.setTextColor(Color.parseColor("#64748B"));

            tvAmount.setTextColor(Color.parseColor("#2563EB"));
        } else {
            tabIncome.setBackgroundColor(Color.WHITE);
            tabIncome.setTextColor(Color.parseColor("#16A34A"));

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
        categorySelectAdapter = new CategorySelectAdapter(
                this,
                category -> {
                    selectedCategoryId =
                            category != null ? category.categoryId : -1;
                }
        );

        rcvTransactionCategories.setAdapter(categorySelectAdapter);
    }


    private void loadCategories(String type) {
        final int requestVersion = ++categoryLoadVersion;

        if (editTransactionId != -1) {
            btnSaveTransaction.setEnabled(false);
        }

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(
                        getApplicationContext()
                );

                SharedPreferences prefs = getSharedPreferences(
                        "ExpenseAppPrefs",
                        Context.MODE_PRIVATE
                );

                String email = prefs.getString("USER_EMAIL", "");
                if (email == null || email.trim().isEmpty()) {
                    showEditLoadError("Vui lòng đăng nhập lại");
                    return;
                }

                UserEntity user =
                        db.userDao().getUserByEmail(email);

                if (user == null) {
                    showEditLoadError("Không tìm thấy tài khoản");
                    return;
                }

                List<CategoryEntity> categories =
                        db.categoryDao().getCategoriesByType(
                                type,
                                user.userId
                        );

                runOnUiThread(() -> {
                    if (requestVersion != categoryLoadVersion
                            || !type.equals(currentType)
                            || isFinishing()
                            || isDestroyed()) {
                        return;
                    }

                    Map<String, CategoryEntity> uniqueMap =
                            new LinkedHashMap<>();

                    if (categories != null) {
                        for (CategoryEntity cat : categories) {
                            if (cat.categoryName != null) {
                                String key = cat.categoryName.trim();

                                if (!uniqueMap.containsKey(key)) {
                                    uniqueMap.put(key, cat);
                                }
                            }
                        }
                    }

                    categorySelectAdapter.setData(
                            new ArrayList<>(uniqueMap.values())
                    );

                    CategoryEntity selected =
                            categorySelectAdapter.getSelectedCategory();

                    selectedCategoryId = selected != null
                            ? selected.categoryId : -1;


                    if (editTransactionId != -1) {
                        boolean validSelection =
                                existingTransaction != null
                                        && selected != null
                                        && selected.categoryId == selectedCategoryId
                                        && type.equals(selected.type);

                        btnSaveTransaction.setEnabled(validSelection);
                    }

                });

            } catch (Exception e) {
                android.util.Log.e(
                        "CategoryLoad",
                        "Không thể tải danh mục",
                        e
                );

                runOnUiThread(() -> {
                    if (requestVersion != categoryLoadVersion
                            || isFinishing()
                            || isDestroyed()) {
                        return;
                    }

                    btnSaveTransaction.setEnabled(false);
                    Toast.makeText(
                            this,
                            "Không thể tải danh mục. Vui lòng thử lại",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        }).start();
    }


    private void showCategoryLoadError(String message) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            btnSaveTransaction.setEnabled(false);

            if (editTransactionId != -1) {
                showEditLoadError(message);
            } else {
                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // 5. Lưu giao dịch Thu nhập / Chi tiêu và tiếp tục ghi
    private void saveTransaction() {
        // --- XỬ LÝ LỖI ĐỊNH DẠNG SỐ (TEST CASE 4.1) ---
        if (rawAmountBuilder.length() == 0) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal amountDecimal;
        try {
            amountDecimal = new BigDecimal(rawAmountBuilder.toString());
            if (amountDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("Negative or Zero");
            }
        } catch (NumberFormatException e) {
            // Verification Passed: Bắt ngoại lệ NumberFormatException
            Toast.makeText(this, "Số tiền không hợp lệ. Phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- KẾT THÚC XỬ LÝ LỖI SỐ ---

        // 2. Kiểm tra giới hạn số tiền
        if (rawAmountBuilder.length() > 12) {
            Toast.makeText(this, "Số tiền vượt quá giới hạn cho phép", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra danh mục đã được chọn
        CategoryEntity selectedCategory = categorySelectAdapter.getSelectedCategory();
        if (selectedCategory == null || selectedCategoryId <= 0) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Kiểm tra loại giao dịch
        if (selectedCategory.type == null || !selectedCategory.type.equals(currentType)) {
            Toast.makeText(this, "Danh mục không phù hợp với loại giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Kiểm tra ghi chú
        String note = edtTransactionNote.getText().toString().trim();
        if (note.length() > 200) {
            edtTransactionNote.setError("Ghi chú không được vượt quá 200 ký tự");
            edtTransactionNote.requestFocus();
            return;
        }

        // 6. Kiểm tra ngày giao dịch
        if (selectedDateMillis <= 0) {
            Toast.makeText(this, "Ngày giao dịch không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu các giá trị hiện tại trước khi chạy luồng nền
        final long categoryIdToSave = selectedCategoryId;
        final String typeToSave = currentType;
        final long dateToSave = selectedDateMillis;

        // Tránh người dùng nhấn Lưu nhiều lần
        btnSaveTransaction.setEnabled(false);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

            try {
                // 7. Kiểm tra người dùng
                SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
                String userEmail = prefs.getString("USER_EMAIL", "");

                if (userEmail == null || userEmail.trim().isEmpty()) {
                    runOnUiThread(() -> {
                        btnSaveTransaction.setEnabled(true);
                        Toast.makeText(AddTransactionActivity.this, "Vui lòng đăng nhập trước khi lưu giao dịch", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                UserEntity user = db.userDao().getUserByEmail(userEmail);
                if (user == null || user.userId <= 0) {
                    runOnUiThread(() -> {
                        btnSaveTransaction.setEnabled(true);
                        Toast.makeText(AddTransactionActivity.this, "Không tìm thấy tài khoản hợp lệ", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // 8. Kiểm tra danh mục vẫn tồn tại trong database
                CategoryEntity category = db.categoryDao().getCategoryById(categoryIdToSave);
                if (category == null) {
                    runOnUiThread(() -> {
                        btnSaveTransaction.setEnabled(true);
                        Toast.makeText(AddTransactionActivity.this, "Danh mục không còn tồn tại", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // 9. Kiểm tra danh mục phù hợp với loại giao dịch
                if (category.type == null || !category.type.equals(typeToSave)) {
                    runOnUiThread(() -> {
                        btnSaveTransaction.setEnabled(true);
                        Toast.makeText(AddTransactionActivity.this, "Danh mục không phù hợp với loại giao dịch", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // 10. Kiểm tra quyền sử dụng danh mục
                if (category.userId != null && category.userId != -1 && category.userId != user.userId) {
                    runOnUiThread(() -> {
                        btnSaveTransaction.setEnabled(true);
                        Toast.makeText(AddTransactionActivity.this, "Bạn không thể sử dụng danh mục này", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // 11. Lấy bản ghi cũ từ DB để tránh sửa nhầm người dùng hoặc dữ liệu lỗi thời.
                TransactionEntity oldTx = null;
                if (editTransactionId != -1) {
                    oldTx = db.transactionDao().getTransactionById(editTransactionId);
                    if (oldTx == null || oldTx.userID != user.userId) {
                        runOnUiThread(() -> {
                            btnSaveTransaction.setEnabled(true);
                            Toast.makeText(this, "Giao dịch không tồn tại hoặc không thuộc tài khoản này", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                }

                // Tính trạng thái trước/sau trên dữ liệu hiện tại, trước khi ghi.
                String warning = null;

                if ("Expense".equalsIgnoreCase(typeToSave)) {
                    warning = getBudgetWarningSafely(
                            db,
                            user.userId,
                            categoryIdToSave,
                            dateToSave,
                            amountDecimal,
                            oldTx
                    );
                }

                final String finalWarning = warning;

                if (oldTx != null) {
                    oldTx.amount = amountDecimal;
                    oldTx.categoryID = categoryIdToSave;
                    oldTx.note = note;
                    oldTx.transactionDate = dateToSave;
                    int updated = db.transactionDao().updateTransaction(oldTx);
                    if (updated != 1) throw new IllegalStateException("Không cập nhật được giao dịch");

                    if (finalWarning != null) {
                        NotificationHelper.showBudgetNotification(
                                getApplicationContext(),
                                finalWarning
                        );
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã cập nhật giao dịch thành công!", Toast.LENGTH_SHORT).show();
                        if (finalWarning != null) {
                            // Không đóng Activity cho tới khi người dùng đọc cảnh báo.
                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Cảnh báo ngân sách")
                                    .setMessage(finalWarning)
                                    .setPositiveButton("Đã hiểu", (dialog, which) -> finish())
                                    .setOnCancelListener(dialog -> finish())
                                    .show();
                        } else {
                            finish();
                        }
                    });
                } else {
                    TransactionEntity transaction = new TransactionEntity(
                            user.userId, categoryIdToSave, amountDecimal, note,
                            dateToSave, System.currentTimeMillis());
                    db.transactionDao().insertTransaction(transaction);
                    runOnUiThread(() -> {
                        String message = "Income".equalsIgnoreCase(typeToSave)
                                ? "Đã lưu thu nhập thành công!" : "Đã lưu chi tiêu thành công!";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        resetInputFields();
                        btnSaveTransaction.setEnabled(true);

                        if (finalWarning != null) {
                            NotificationHelper.showBudgetNotification(
                                    getApplicationContext(),
                                    finalWarning
                            );
                        }

                        showAlertIfNeeded(finalWarning);

                    });
                }

            } catch (android.database.sqlite.SQLiteException e) {
                runOnUiThread(() -> {
                    btnSaveTransaction.setEnabled(true);
                    Toast.makeText(AddTransactionActivity.this, "Lỗi cơ sở dữ liệu: Không thể lưu giao dịch. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSaveTransaction.setEnabled(true);
                    Toast.makeText(AddTransactionActivity.this, "Không thể lưu giao dịch. Đã xảy ra lỗi bất ngờ.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private String getBudgetWarningSafely(
            AppDatabase db,
            long userId,
            long categoryId,
            long date,
            BigDecimal amount,
            TransactionEntity oldTx
    ) {
        try {
            return evaluateBudgetAlert(
                    db,
                    userId,
                    categoryId,
                    date,
                    amount.longValueExact(),
                    oldTx
            );
        } catch (Exception e) {
            android.util.Log.e(
                    "BudgetWarning",
                    "Không thể tính cảnh báo ngân sách",
                    e
            );
            return null;
        }
    }

    // 6. Đưa các ô nhập liệu về trạng thái ban đầu
    private void resetInputFields() {
        rawAmountBuilder.setLength(0);
        tvAmount.setText("0 đ");
        edtTransactionNote.setText("");
        edtTransactionNote.clearFocus();
    }
}