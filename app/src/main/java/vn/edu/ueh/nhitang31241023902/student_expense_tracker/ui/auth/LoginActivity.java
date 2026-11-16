package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.UUID;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.admin.AdminMainActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main.MainActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.AdminSeeder;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.DefaultCategorySeeder;
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
            boolean isAdmin = UserEntity.ROLE_ADMIN.equals(prefs.getString("USER_ROLE", UserEntity.ROLE_STUDENT));
            startActivity(new Intent(this, isAdmin ? AdminMainActivity.class : MainActivity.class));
            finish(); // Đóng màn hình Login
            return;
        }

        setContentView(R.layout.activity_login);
        db = AppDatabase.getDatabase(getApplicationContext());

        // Đảm bảo luôn có tài khoản admin mặc định
        new Thread(() -> AdminSeeder.ensureDefaultAdmin(db)).start();

        // 2. Ánh xạ các thành phần giao diện (UI Components)
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Chỉ điền sẵn email khi lần trước người dùng đã tick "Ghi nhớ"
        String savedEmail = prefs.getString("REMEMBERED_EMAIL", "");
        if (!savedEmail.isEmpty()) {
            edtLoginEmail.setText(savedEmail);
            cbRememberMe.setChecked(true);
        }

        // 3. Chuyển sang màn hình ĐĂNG KÝ
        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // 4. Chuyển sang màn hình QUÊN MẬT KHẨU
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            intent.putExtra("EMAIL", edtLoginEmail.getText().toString().trim());
            startActivity(intent);
        });

        // 5. Đăng nhập thường / Google
        btnLogin.setOnClickListener(v -> executeLogin());
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }

    // ===================== Đăng nhập email + mật khẩu =====================

    private void executeLogin() {
        String email = edtLoginEmail.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        String password = edtLoginPassword.getText().toString();
        boolean isRememberMeChecked = cbRememberMe.isChecked();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            UserEntity user = db.userDao().getUserByEmail(email);

            if (user != null && BCryptUtils.checkPassword(password, user.passwordHash)) {
                completeLogin(user, isRememberMeChecked);
            } else {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this,
                        "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /** Dùng chung cho đăng nhập thường và Google. Gọi trên background thread. */
    private void completeLogin(UserEntity user, boolean remember) {
        // Tài khoản bị khóa -> không cho đăng nhập (UC14)
        if (!user.isActive) {
            runOnUiThread(() -> Toast.makeText(LoginActivity.this,
                    "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!",
                    Toast.LENGTH_LONG).show());
            return;
        }
        final boolean isAdmin = UserEntity.ROLE_ADMIN.equals(user.role);
        if (!isAdmin) DefaultCategorySeeder.ensureDefaults(db);

        SharedPreferences.Editor editor = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE).edit();
        // IS_LOGGED_IN = tự động đăng nhập lần mở app sau (chỉ khi tick "Ghi nhớ")
        editor.putBoolean("IS_LOGGED_IN", remember);
        if (remember) editor.putString("REMEMBERED_EMAIL", user.email);
        else editor.remove("REMEMBERED_EMAIL");
        editor.putString("USER_EMAIL", user.email);
        editor.putString("USER_NAME", user.fullName);
        editor.putString("USER_ROLE", user.role);
        editor.apply();

        runOnUiThread(() -> {
            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this,
                    isAdmin ? AdminMainActivity.class : MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // ===================== Đăng nhập Google =====================

    private void signInWithGoogle() {
        String clientId = getString(R.string.google_web_client_id);
        if (clientId.startsWith("YOUR_WEB_CLIENT_ID")) {
            Toast.makeText(this, "Chưa cấu hình Google Client ID (res/values/google_config.xml)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        GetSignInWithGoogleOption googleOption =
                new GetSignInWithGoogleOption.Builder(clientId).build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();

        CredentialManager credentialManager = CredentialManager.create(this);
        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleCredential(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) return; // người dùng tự hủy
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập Google thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleGoogleCredential(Credential credential) {
        if (!(credential instanceof CustomCredential)
                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            Toast.makeText(this, "Loại thông tin đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        GoogleIdTokenCredential google = GoogleIdTokenCredential.createFrom(credential.getData());
        final String email = google.getId().trim().toLowerCase(java.util.Locale.ROOT);
        final String name = google.getDisplayName() != null ? google.getDisplayName() : email;

        new Thread(() -> {
            UserEntity user = db.userDao().getUserByEmail(email);

            if (user == null) {
                // Lần đầu đăng nhập Google -> tự tạo tài khoản sinh viên (mật khẩu ngẫu nhiên)
                user = new UserEntity(name, email,
                        BCryptUtils.hashPassword(UUID.randomUUID().toString()),
                        System.currentTimeMillis());
                db.userDao().insertUser(user);
                user = db.userDao().getUserByEmail(email);
            } else if (UserEntity.ROLE_ADMIN.equals(user.role)) {
                // Admin chỉ đăng nhập bằng mật khẩu
                runOnUiThread(() -> Toast.makeText(LoginActivity.this,
                        "Tài khoản quản trị viên không hỗ trợ đăng nhập Google!",
                        Toast.LENGTH_LONG).show());
                return;
            }

            completeLogin(user, true);
        }).start();
    }
}