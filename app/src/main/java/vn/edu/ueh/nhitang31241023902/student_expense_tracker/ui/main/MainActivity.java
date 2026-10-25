package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction.AddTransactionActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalBalance;
    private Button btnLogout;
    private AppCompatButton btnOpenCategoryTemp, btnTestAddTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ View
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        btnLogout = findViewById(R.id.btnLogout);
        btnOpenCategoryTemp = findViewById(R.id.btnOpenCategoryTemp);
        btnTestAddTransaction = findViewById(R.id.btnTestAddTransaction);

        // 2. Chuyển sang màn hình CategoryActivity
        btnOpenCategoryTemp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            startActivity(intent);
        });

        // 3. Chuyển sang màn hình Thêm giao dịch
        btnTestAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        // 4. Lấy tên User từ SharedPreferences để hiển thị
        SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("USER_NAME", "Bạn");
        tvWelcome.setText("Chào, " + userName + "!");

        // 5. Xử lý Đăng xuất
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi quay lại màn hình chính (sau khi thêm giao dịch), cập nhật lại số dư
        calculateAndUpdateBalance();
    }

    private void calculateAndUpdateBalance() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("USER_EMAIL", "");

            long currentUserId = -1;
            if (!userEmail.isEmpty()) {
                UserEntity user = db.userDao().getUserByEmail(userEmail);
                if (user != null) {
                    currentUserId = user.userId;
                }
            }

            // Lấy danh sách giao dịch tương ứng
            List<TransactionEntity> transactions;
            if (currentUserId != -1) {
                transactions = db.transactionDao().getTransactionsByUserId(currentUserId);
            } else {
                transactions = db.transactionDao().getAllTransactions();
            }

            BigDecimal totalBalance = BigDecimal.ZERO;

            if (transactions != null) {
                for (TransactionEntity transaction : transactions) {
                    if (transaction.amount == null) continue;

                    CategoryEntity category = db.categoryDao().getCategoryById(transaction.categoryID);
                    if (category != null && "Income".equalsIgnoreCase(category.type)) {
                        totalBalance = totalBalance.add(transaction.amount);
                    } else {
                        // Chi tiêu (Expense) hoặc không xác định thì trừ ra
                        totalBalance = totalBalance.subtract(transaction.amount);
                    }
                }
            }

            final BigDecimal finalBalance = totalBalance;
            runOnUiThread(() -> {
                DecimalFormat formatter = new DecimalFormat("#,###");
                String formattedBalance = formatter.format(finalBalance) + " VNĐ";
                tvTotalBalance.setText(formattedBalance);
            });
        }).start();
    }
}