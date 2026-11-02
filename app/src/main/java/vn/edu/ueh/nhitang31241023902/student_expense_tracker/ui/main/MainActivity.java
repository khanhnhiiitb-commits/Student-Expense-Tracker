package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction.AddTransactionActivity;
// Nếu CategoryActivity nằm ở thư mục khác, hãy import nó tại đây

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Mặc định hiển thị HomeFragment khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Bấm vào nút dấu cộng ở giữa -> Hiện Menu lựa chọn
            if (itemId == R.id.nav_add) {
                showAddOptions();
                return false; // Trả về false để thanh điều hướng không đổi màu sáng sang nút Add
            }

            // Xử lý chuyển đổi 4 tab còn lại
            Fragment selectedFragment = null;
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_budget) {
                selectedFragment = new BudgetFragment();
            } else if (itemId == R.id.nav_statistics) {
                selectedFragment = new StatisticsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
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

    // Hàm hiển thị hộp thoại lựa chọn
    private void showAddOptions() {
        String[] options = {"📝 Thêm giao dịch", "🏷️ Quản lý danh mục"};

        new AlertDialog.Builder(this)
                .setTitle("Bạn muốn tùy chỉnh gì?")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Mở màn hình Thêm giao dịch
                        startActivity(new Intent(MainActivity.this, AddTransactionActivity.class));
                    } else if (which == 1) {
                        // Mở màn hình Quản lý danh mục
                        startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                    }
                })
                .show();
    }
}