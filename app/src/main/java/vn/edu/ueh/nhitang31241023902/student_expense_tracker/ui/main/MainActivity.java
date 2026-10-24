package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Lấy tên User từ SharedPreferences để hiển thị
        SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("USER_NAME", "Bạn");
        tvWelcome.setText("Chào, " + userName + "!");

        // Xử lý Đăng xuất
        btnLogout.setOnClickListener(v -> {
            // Xóa toàn bộ dữ liệu phiên đăng nhập
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Chuyển hướng về màn hình Đăng nhập
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}