package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction.AddTransactionActivity;

import android.Manifest;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Chưa đăng nhập (hoặc phiên đã bị xóa) -> về màn hình đăng nhập
        final android.content.SharedPreferences sessionPrefs = getSharedPreferences("ExpenseAppPrefs", MODE_PRIVATE);
        final String sessionEmail = sessionPrefs.getString("USER_EMAIL", "");
        if (sessionEmail.isEmpty()) {
            Intent i = new Intent(this, vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        // Tài khoản bị admin khóa trong lúc đang dùng -> đăng xuất
        new Thread(() -> {
            vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity u =
                    vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase
                            .getDatabase(getApplicationContext()).userDao().getUserByEmail(sessionEmail);
            if (u != null && !u.isActive) {
                sessionPrefs.edit().clear().apply();
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Tài khoản của bạn đã bị khóa!", android.widget.Toast.LENGTH_LONG).show();
                    Intent i = new Intent(this, vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                });
            } else if (u != null) {
                // Tự ghi các khoản chi phí cố định đã đến hạn
                vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.RecurringExpenseProcessor
                        .generateDue(vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase
                                .getDatabase(getApplicationContext()), u.userId);
            }
        }).start();
        requestNotificationPermission();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        findViewById(R.id.fab_add).setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));

        // Mặc định hiển thị HomeFragment khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Xử lý chuyển đổi 4 tab còn lại
            Fragment selectedFragment = null;
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_budget) {
                selectedFragment = new BudgetFragment();
            } else if (itemId == R.id.nav_statistics) {
                selectedFragment = new ReportFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.calendar.CalendarFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private final ActivityResultLauncher<String>
            notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (!isGranted) {
                            android.util.Log.d(
                                    "NotificationPermission",
                                    "Người dùng chưa cấp quyền thông báo"
                            );
                        }
                    }
            );


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
            );
        }
    }


}