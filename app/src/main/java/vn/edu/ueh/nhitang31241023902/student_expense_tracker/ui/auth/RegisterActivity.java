package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Đã thêm import cho TextView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Database
        db = AppDatabase.getDatabase(getApplicationContext());

        // Ánh xạ View
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // 1. Ánh xạ nút "Đã có tài khoản? Đăng nhập"
        TextView tvLoginNow = findViewById(R.id.tvLoginNow);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndRegister();
            }
        });

        // 2. Gắn sự kiện click để quay lại màn hình Login
        tvLoginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng RegisterActivity, tự động quay về LoginActivity ở dưới
            }
        });
    }

    private void validateAndRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        // 1. Kiểm tra Họ tên
        if (!ValidationUtils.isValidFullName(fullName)) {
            edtFullName.setError("Họ tên phải có ít nhất 2 ký tự!");
            edtFullName.requestFocus();
            return;
        }

        // 2. Kiểm tra Email
        if (!ValidationUtils.isValidEmail(email)) {
            edtEmail.setError("Email không đúng định dạng!");
            edtEmail.requestFocus();
            return;
        }

        // 3. Kiểm tra Mật khẩu
        if (!ValidationUtils.isValidPassword(password)) {
            edtPassword.setError("Mật khẩu phải chứa ít nhất 6 ký tự!");
            edtPassword.requestFocus();
            return;
        }

        // 4. Kiểm tra Xác nhận mật khẩu
        if (!ValidationUtils.isPasswordMatch(password, confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
            edtConfirmPassword.requestFocus();
            return;
        }

        // --- ĐÃ VƯỢT QUA TẤT CẢ VALIDATION ---
        // Tiến hành lưu user xuống Room Database trên background thread
        executeRegistration(fullName, email, password);
    }

    private void executeRegistration(String fullName, String email, String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 1. Kiểm tra xem email đã tồn tại trong CSDL chưa
                UserEntity existingUser = db.userDao().getUserByEmail(email);
                if (existingUser != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                // 2. Băm mật khẩu bằng thuật toán BCrypt (Đảm bảo an toàn theo tiêu chuẩn RTM)
                String hashedPassword = vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BCryptUtils.hashPassword(password);

                // 3. Tạo đối tượng User mới với mật khẩu đã được mã hóa
                UserEntity newUser = new UserEntity(
                        0,
                        fullName,
                        email,
                        hashedPassword,
                        System.currentTimeMillis()
                );

                // 4. Lưu vào Room Database
                db.userDao().insertUser(newUser);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        android.content.Intent intent = new android.content.Intent(RegisterActivity.this, vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main.MainActivity.class);

                        intent.putExtra("USER_NAME", fullName);

                        startActivity(intent);
                        finish();
                    }
                });
            }
        }).start();
    }
}