package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.TransactionAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main.CategoryActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction.AddTransactionActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalBalance;
    private Button btnLogout;
    private AppCompatButton btnOpenCategoryTemp, btnTestAddTransaction;
    private RecyclerView rcvRecentTransactions;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        btnLogout = findViewById(R.id.btnLogout);
        btnOpenCategoryTemp = findViewById(R.id.btnOpenCategoryTemp);
        btnTestAddTransaction = findViewById(R.id.btnTestAddTransaction);
        rcvRecentTransactions = findViewById(R.id.rcvRecentTransactions);

        // Khởi tạo RecyclerView hiển thị danh sách giao dịch (TC-TRX-06)
        rcvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        // Khi người dùng bấm vào một giao dịch -> Bật hộp thoại hỏi có muốn xóa không
        transactionAdapter = new TransactionAdapter(this, transaction -> {
            showTransactionOptions(transaction); // Gọi hàm hiển thị Menu tùy chọn
        });
        rcvRecentTransactions.setAdapter(transactionAdapter);        rcvRecentTransactions.setAdapter(transactionAdapter);

        btnOpenCategoryTemp.setOnClickListener(v -> startActivity(new Intent(this, CategoryActivity.class)));
        btnTestAddTransaction.setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));

        SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        tvWelcome.setText("Chào, " + prefs.getString("USER_NAME", "Bạn") + "!");

        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // ĐIỂM THEN CHỐT: onResume() tự động chạy mỗi khi màn hình quay trở lại
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
    private void confirmAndDeleteTransaction(TransactionEntity transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                        // 1. Thực hiện xóa khỏi Room Database
                        db.transactionDao().deleteTransaction(transaction);

                        // 2. Tải và tính toán lại toàn bộ dữ liệu trên Dashboard
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show();
                            loadDashboardData(); // Hàm tự động tính lại số dư và cập nhật RecyclerView
                        });
                    }).start();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void showTransactionOptions(TransactionEntity transaction) {
        String[] options = {"✏️ Chỉnh sửa giao dịch", "🗑️ Xóa giao dịch"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tùy chọn giao dịch")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Người dùng chọn "Chỉnh sửa"
                        Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);

                        // Lưu ý: Kiểm tra lại tên khóa chính trong TransactionEntity của bạn
                        // Nếu tên biến là transactionID (viết hoa ID), hãy sửa lại thành transaction.transactionID
                        intent.putExtra("EDIT_TRANSACTION_ID", transaction.transactionID);

                        startActivity(intent);
                    } else if (which == 1) {
                        // Người dùng chọn "Xóa"
                        confirmAndDeleteTransaction(transaction);
                    }
                })
                .show();
    }
    private void loadDashboardData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String email = prefs.getString("USER_EMAIL", "");

            long currentUserId = -1;
            UserEntity user = db.userDao().getUserByEmail(email);
            if (user != null) currentUserId = user.userId;

            // 1. Lấy danh sách giao dịch
            List<TransactionEntity> list;
            if (currentUserId != -1) {
                list = db.transactionDao().getTransactionsByUserId(currentUserId);
            } else {
                list = db.transactionDao().getAllTransactions();
            }

            // 2. Tính toán chính xác tổng số dư
            BigDecimal total = BigDecimal.ZERO;
            if (list != null) {
                for (TransactionEntity tx : list) {
                    if (tx.amount == null) continue;
                    CategoryEntity cat = db.categoryDao().getCategoryById(tx.categoryID);
                    if (cat != null && "Income".equalsIgnoreCase(cat.type)) {
                        total = total.add(tx.amount); // Khoản thu cộng vào
                    } else {
                        total = total.subtract(tx.amount); // Khoản chi trừ ra
                    }
                }
            }

            BigDecimal finalTotal = total;
            runOnUiThread(() -> {
                // Hiển thị số dư có định dạng chuẩn
                DecimalFormat df = new DecimalFormat("#,###");
                tvTotalBalance.setText(df.format(finalTotal) + " VNĐ");

                // Cập nhật danh sách giao dịch lên màn hình
                transactionAdapter.setData(list);
            });
        }).start();
    }
}