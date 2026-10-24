package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main.MainActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BCryptUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText edtLoginEmail, edtLoginPassword;
    private CheckBox cbRememberMe;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kiểm tra trạng thái đã đăng nhập hay chưa (Tự động vào thẳng Trang chủ)
        SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish(); // Đóng màn hình Login
            return;
        }

        setContentView(R.layout.activity_login);
        db = AppDatabase.getDatabase(getApplicationContext());

        // 2. Ánh xạ các thành phần giao diện (UI Components)
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // 3. Xử lý sự kiện CHUYỂN SANG MÀN HÌNH ĐĂNG KÝ
        tvGoToRegister.setOnClickListener(v -> {
            // Sử dụng Intent để chuyển từ LoginActivity sang RegisterActivity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 4. Xử lý sự kiện QUÊN MẬT KHẨU (Tạm thời hiển thị thông báo)
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Tính năng khôi phục mật khẩu đang phát triển!", Toast.LENGTH_SHORT).show();
        });

        // 5. Xử lý sự kiện BẤM NÚT ĐĂNG NHẬP
        btnLogin.setOnClickListener(v -> executeLogin());
    }

    private void executeLogin() {
        String email = edtLoginEmail.getText().toString().trim();
        String password = edtLoginPassword.getText().toString();
        boolean isRememberMeChecked = cbRememberMe.isChecked();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chạy truy vấn DB trên Background Thread
        new Thread(() -> {
            UserEntity user = db.userDao().getUserByEmail(email);

            if (user != null && BCryptUtils.checkPassword(password, user.passwordHash)) {

                // Đăng nhập thành công -> Lưu dữ liệu vào SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE).edit();

                // Nếu người dùng chọn "Ghi nhớ", ta lưu cờ IS_LOGGED_IN = true
                if (isRememberMeChecked) {
                    editor.putBoolean("IS_LOGGED_IN", true);
                } else {
                    editor.putBoolean("IS_LOGGED_IN", false);
                }

                editor.putString("USER_EMAIL", user.email);
                editor.putString("USER_NAME", user.fullName);
                editor.apply();

                // Chuyển về Main Thread để cập nhật UI và chuyển trang
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Hủy màn hình Login để không back lại được
                });
            } else {
                // Sai tài khoản / mật khẩu
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}