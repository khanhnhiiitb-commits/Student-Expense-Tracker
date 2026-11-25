package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BCryptUtils;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.ValidationUtils;

/**
 * Quên mật khẩu (xác minh tại máy): đúng email + họ tên đã đăng ký thì được đặt mật khẩu mới.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail, edtFullName, edtNewPassword, edtConfirmNewPassword;
    private Button btnReset;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        db = AppDatabase.getDatabase(getApplicationContext());

        edtEmail = findViewById(R.id.edtForgotEmail);
        edtFullName = findViewById(R.id.edtForgotFullName);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnReset = findViewById(R.id.btnResetPassword);

        // Điền sẵn email nếu màn Login đã nhập
        String prefill = getIntent().getStringExtra("EMAIL");
        if (prefill != null) edtEmail.setText(prefill);

        findViewById(R.id.btnBackForgot).setOnClickListener(v -> finish());
        btnReset.setOnClickListener(v -> validateAndReset());
    }

    private void validateAndReset() {
        String email = edtEmail.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        String fullName = edtFullName.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString();
        String confirm = edtConfirmNewPassword.getText().toString();

        if (!ValidationUtils.isValidEmail(email)) {
            edtEmail.setError("Email không đúng định dạng!");
            edtEmail.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidFullName(fullName)) {
            edtFullName.setError("Vui lòng nhập họ tên đã đăng ký!");
            edtFullName.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidPassword(newPassword)) {
            edtNewPassword.setError("Mật khẩu phải chứa ít nhất 6 ký tự!");
            edtNewPassword.requestFocus();
            return;
        }
        if (!ValidationUtils.isPasswordMatch(newPassword, confirm)) {
            edtConfirmNewPassword.setError("Mật khẩu xác nhận không khớp!");
            edtConfirmNewPassword.requestFocus();
            return;
        }

        btnReset.setEnabled(false);
        new Thread(() -> {
            UserEntity user = db.userDao().getUserByEmail(email);

            // Báo lỗi chung chung để không lộ email nào đã đăng ký
            boolean verified = user != null
                    && !UserEntity.ROLE_ADMIN.equals(user.role)
                    && user.fullName != null
                    && user.fullName.trim().equalsIgnoreCase(fullName);

            if (!verified) {
                runOnUiThread(() -> {
                    btnReset.setEnabled(true);
                    Toast.makeText(this, "Email hoặc họ tên không khớp với tài khoản nào!",
                            Toast.LENGTH_LONG).show();
                });
                return;
            }

            db.userDao().updatePassword(email, BCryptUtils.hashPassword(newPassword));

            runOnUiThread(() -> {
                Toast.makeText(this, "Đặt lại mật khẩu thành công! Hãy đăng nhập lại.",
                        Toast.LENGTH_LONG).show();
                finish();
            });
        }).start();
    }
}