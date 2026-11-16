package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity;

/**
 * Màn hình chính của Quản trị viên.
 * Hiện mới là khung: UC14 (quản lý tài khoản) và UC15 (theo dõi hệ thống) sẽ làm sau.
 */
public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);

        // Chặn truy cập nếu không phải admin
        if (!UserEntity.ROLE_ADMIN.equals(prefs.getString("USER_ROLE", ""))) {
            logout();
            return;
        }

        setContentView(R.layout.activity_admin_main);

        TextView tvAdminWelcome = findViewById(R.id.tvAdminWelcome);
        tvAdminWelcome.setText("Xin chào, " + prefs.getString("USER_NAME", "Quản trị viên"));

        findViewById(R.id.cardManageUsers).setOnClickListener(v ->
                startActivity(new Intent(this, AdminUserActivity.class)));
        findViewById(R.id.cardSystemMonitor).setOnClickListener(v -> showSystemOverview());
        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> logout());
    }

    /** UC15 - Tổng quan hệ thống: số người dùng, số giao dịch. */
    private void showSystemOverview() {
        new Thread(() -> {
            vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase db =
                    vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase.getDatabase(getApplicationContext());
            final int users = db.userDao().countStudents();
            final int tx = db.transactionDao().countAll();
            runOnUiThread(() -> new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Tổng quan hệ thống")
                    .setMessage("Số tài khoản sinh viên: " + users + "\nTổng số giao dịch đã ghi nhận: " + tx)
                    .setPositiveButton("Đóng", null)
                    .show());
        }).start();
    }

    private void logout() {
        getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
