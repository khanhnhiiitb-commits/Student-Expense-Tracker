package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BCryptUtils;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    private Button btnRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo database SQLite thông qua Room
        db = AppDatabase.getDatabase(getApplicationContext());

        // Ánh xạ giao diện
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginNow = findViewById(R.id.tvLoginNow);

        // Xử lý nút đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndRegister();
            }
        });

        // Quay lại màn hình đăng nhập
        tvLoginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void validateAndRegister() {

        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        // 1. Kiểm tra họ tên
        if (!ValidationUtils.isValidFullName(fullName)) {
            edtFullName.setError("Họ tên phải có ít nhất 2 ký tự!");
            edtFullName.requestFocus();
            return;
        }

        // 2. Kiểm tra email
        if (!ValidationUtils.isValidEmail(email)) {
            edtEmail.setError("Email không đúng định dạng!");
            edtEmail.requestFocus();
            return;
        }

        // 3. Kiểm tra mật khẩu
        if (!ValidationUtils.isValidPassword(password)) {
            edtPassword.setError("Mật khẩu phải chứa ít nhất 6 ký tự!");
            edtPassword.requestFocus();
            return;
        }

        // 4. Kiểm tra xác nhận mật khẩu
        if (!ValidationUtils.isPasswordMatch(password, confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
            edtConfirmPassword.requestFocus();
            return;
        }

        // Đăng ký tài khoản trên luồng nền
        executeRegistration(fullName, email, password);
    }

    private void executeRegistration(
            String fullName,
            String email,
            String password
    ) {

        btnRegister.setEnabled(false);

        new Thread(() -> {

            try {
                // 1. Kiểm tra email đã tồn tại chưa
                UserEntity existingUser = db.userDao().getUserByEmail(email);

                if (existingUser != null) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Email này đã được sử dụng!",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

                    return;
                }

                // 2. Mã hóa mật khẩu bằng BCrypt
                String hashedPassword = BCryptUtils.hashPassword(password);

                // 3. Tạo tài khoản mới
                UserEntity newUser = new UserEntity(
                        fullName,
                        email,
                        hashedPassword,
                        System.currentTimeMillis()
                );

                // 4. Lưu tài khoản vào SQLite
                long userId = db.userDao().insertUser(newUser);

                if (userId <= 0) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Không thể tạo tài khoản. Vui lòng thử lại!",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

                    return;
                }

                // 5. Lưu thông tin người dùng sau khi đăng ký thành công
                SharedPreferences prefs = getSharedPreferences(
                        "ExpenseAppPrefs",
                        Context.MODE_PRIVATE
                );

                SharedPreferences.Editor editor = prefs.edit();

                editor.putString("USER_EMAIL", email);
                editor.putString("USER_NAME", fullName);
                editor.putBoolean("IS_LOGGED_IN", true);

                editor.apply();

                // 6. Chuyển sang màn hình chính
                runOnUiThread(() -> {

                    Toast.makeText(
                            RegisterActivity.this,
                            "Đăng ký tài khoản thành công!",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            RegisterActivity.this,
                            vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main.MainActivity.class
                    );

                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);

                    Toast.makeText(
                            RegisterActivity.this,
                            "Đăng ký thất bại. Vui lòng thử lại!",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

        }).start();
    }
}