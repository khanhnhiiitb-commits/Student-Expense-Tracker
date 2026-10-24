package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvTotalBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);

        // TODO: Nhận dữ liệu User từ màn hình Đăng nhập (LoginActivity) truyền sang
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_NAME")) {
            String fullName = intent.getStringExtra("USER_NAME");
            tvWelcome.setText("Chào, " + fullName + "!");
        }
    }
}