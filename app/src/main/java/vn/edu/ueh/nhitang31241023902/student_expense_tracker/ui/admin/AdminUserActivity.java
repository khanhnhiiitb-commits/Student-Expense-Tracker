package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.admin;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

/** UC14 - Quản lý tài khoản người dùng: xem danh sách, tìm kiếm, khóa / mở khóa. */
public class AdminUserActivity extends AppCompatActivity {

    private final List<UserEntity> allUsers = new ArrayList<>();
    private final List<UserEntity> shown = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final List<String> labels = new ArrayList<>();
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chỉ admin mới được vào
        String role = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE).getString("USER_ROLE", "");
        if (!UserEntity.ROLE_ADMIN.equals(role)) { finish(); return; }

        float d = getResources().getDisplayMetrics().density;
        int pad = (int) (16 * d);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getColor(vn.edu.ueh.nhitang31241023902.student_expense_tracker.R.color.screen_bg));
        root.setPadding(pad, (int) (40 * d), pad, pad);

        TextView title = new TextView(this);
        title.setText("Quản lý tài khoản người dùng");
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getColor(vn.edu.ueh.nhitang31241023902.student_expense_tracker.R.color.text_main));
        root.addView(title);

        EditText edtSearch = new EditText(this);
        edtSearch.setHint("Tìm theo họ tên hoặc email");
        edtSearch.setSingleLine(true);
        root.addView(edtSearch);

        tvCount = new TextView(this);
        tvCount.setTextColor(getColor(vn.edu.ueh.nhitang31241023902.student_expense_tracker.R.color.text_sub));
        root.addView(tvCount);

        ListView list = new ListView(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(s.toString()); }
        });
        list.setOnItemClickListener((p, v, pos, id) -> confirmToggle(shown.get(pos), edtSearch));

        loadUsers(edtSearch);
    }

    private void loadUsers(EditText edtSearch) {
        new Thread(() -> {
            List<UserEntity> users = AppDatabase.getDatabase(getApplicationContext()).userDao().getAllStudents();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                allUsers.clear();
                allUsers.addAll(users);
                applyFilter(edtSearch.getText().toString());
            });
        }).start();
    }

    private void applyFilter(String q) {
        String key = q.trim().toLowerCase(Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        shown.clear();
        labels.clear();
        for (UserEntity u : allUsers) {
            String name = u.fullName != null ? u.fullName : "";
            String email = u.email != null ? u.email : "";
            if (!key.isEmpty() && !name.toLowerCase(Locale.getDefault()).contains(key)
                    && !email.toLowerCase(Locale.getDefault()).contains(key)) continue;
            shown.add(u);
            labels.add((u.isActive ? "🟢 " : "🔒 ") + name + "\n" + email
                    + "\nTạo ngày " + sdf.format(new Date(u.createdAt))
                    + (u.isActive ? "" : "  ·  ĐÃ KHÓA"));
        }
        adapter.notifyDataSetChanged();
        tvCount.setText(shown.size() + " tài khoản");
    }

    private void confirmToggle(UserEntity u, EditText edtSearch) {
        final boolean lock = u.isActive;
        new AlertDialog.Builder(this)
                .setTitle(lock ? "Khóa tài khoản?" : "Mở khóa tài khoản?")
                .setMessage((lock ? "Người dùng sẽ không thể đăng nhập: " : "Cho phép đăng nhập lại: ") + u.email)
                .setPositiveButton(lock ? "Khóa" : "Mở khóa", (d, w) -> new Thread(() -> {
                    AppDatabase.getDatabase(getApplicationContext()).userDao().updateActive(u.userId, !lock);
                    runOnUiThread(() -> {
                        Toast.makeText(this, lock ? "Đã khóa tài khoản" : "Đã mở khóa tài khoản", Toast.LENGTH_SHORT).show();
                        loadUsers(edtSearch);
                    });
                }).start())
                .setNegativeButton("Hủy", null)
                .show();
    }
}
