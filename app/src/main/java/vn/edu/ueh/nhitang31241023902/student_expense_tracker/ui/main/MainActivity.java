package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction.AddTransactionActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần giao diện
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnLogout = findViewById(R.id.btnLogout);

        AppCompatButton btnOpenCategoryTemp =
                findViewById(R.id.btnOpenCategoryTemp);

        AppCompatButton btnTestAddTransaction =
                findViewById(R.id.btnTestAddTransaction);

        // Lấy tên người dùng đã đăng nhập
        SharedPreferences prefs = getSharedPreferences(
                "ExpenseAppPrefs",
                Context.MODE_PRIVATE
        );

        String userName = prefs.getString("USER_NAME", "Bạn");
        tvWelcome.setText("Chào, " + userName + "!");

        // Chuyển sang màn hình Danh mục
        btnOpenCategoryTemp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    CategoryActivity.class
            );

            startActivity(intent);
        });

        // Chuyển sang màn hình Thêm giao dịch
        btnTestAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddTransactionActivity.class
            );

            startActivity(intent);
        });

        // Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();

            editor.clear();
            editor.apply();

            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });
    }
}