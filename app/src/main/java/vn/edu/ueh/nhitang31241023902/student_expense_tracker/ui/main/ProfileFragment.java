package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BCryptUtils;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.ValidationUtils;

/** UC03 - Hồ sơ cá nhân: xem, đổi họ tên, đổi mật khẩu; UC02 - Đăng xuất. */
public class ProfileFragment extends Fragment {

    private TextView tvProfileName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        MaterialButton btnEdit = view.findViewById(R.id.btnEditProfile);
        MaterialButton btnPass = view.findViewById(R.id.btnChangePassword);

        SharedPreferences prefs = requireActivity().getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        tvProfileName.setText(prefs.getString("USER_NAME", "Người dùng"));
        tvProfileEmail.setText(prefs.getString("USER_EMAIL", "Chưa có email"));

        btnEdit.setOnClickListener(v -> showEditNameDialog());
        btnPass.setOnClickListener(v -> showChangePasswordDialog());

        btnLogout.setOnClickListener(v -> {
            if (getActivity() == null) return;
            // Giữ lại email đã "ghi nhớ" để lần sau điền sẵn ở màn hình đăng nhập
            SharedPreferences.Editor ed = prefs.edit();
            String remembered = prefs.getString("REMEMBERED_EMAIL", "");
            ed.clear();
            if (!remembered.isEmpty()) ed.putString("REMEMBERED_EMAIL", remembered);
            ed.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private EditText newField(String hint, boolean password, int pad) {
        EditText e = new EditText(requireContext());
        e.setHint(hint);
        e.setSingleLine(true);
        if (password) e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return e;
    }

    private void showEditNameDialog() {
        final Context ctx = requireContext();
        final SharedPreferences prefs = ctx.getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        final EditText edt = newField("Họ và tên", false, 0);
        edt.setText(prefs.getString("USER_NAME", ""));
        edt.setSelection(edt.getText().length());
        LinearLayout box = wrap(edt);

        new AlertDialog.Builder(ctx)
                .setTitle("Chỉnh sửa họ tên")
                .setView(box)
                .setPositiveButton("Lưu", (d, w) -> {
                    String name = edt.getText().toString().trim();
                    if (!ValidationUtils.isValidFullName(name)) {
                        Toast.makeText(ctx, "Họ tên phải có ít nhất 2 ký tự!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final String email = prefs.getString("USER_EMAIL", "");
                    new Thread(() -> {
                        AppDatabase.getDatabase(ctx.getApplicationContext()).userDao().updateFullName(email, name);
                        prefs.edit().putString("USER_NAME", name).apply();
                        if (getActivity() != null) getActivity().runOnUiThread(() -> {
                            tvProfileName.setText(name);
                            Toast.makeText(ctx, "Đã cập nhật họ tên", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog() {
        final Context ctx = requireContext();
        final SharedPreferences prefs = ctx.getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        final EditText oldP = newField("Mật khẩu hiện tại", true, 0);
        final EditText newP = newField("Mật khẩu mới (tối thiểu 6 ký tự)", true, 0);
        final EditText confP = newField("Nhập lại mật khẩu mới", true, 0);
        LinearLayout box = wrap(oldP, newP, confP);

        new AlertDialog.Builder(ctx)
                .setTitle("Đổi mật khẩu")
                .setView(box)
                .setPositiveButton("Đổi", (d, w) -> {
                    final String o = oldP.getText().toString();
                    final String n = newP.getText().toString();
                    String c = confP.getText().toString();
                    if (!ValidationUtils.isValidPassword(n)) {
                        Toast.makeText(ctx, "Mật khẩu mới phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!ValidationUtils.isPasswordMatch(n, c)) {
                        Toast.makeText(ctx, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final String email = prefs.getString("USER_EMAIL", "");
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getDatabase(ctx.getApplicationContext());
                        UserEntity u = db.userDao().getUserByEmail(email);
                        final String msg;
                        if (u == null || !BCryptUtils.checkPassword(o, u.passwordHash)) {
                            msg = "Mật khẩu hiện tại không đúng!";
                        } else {
                            db.userDao().updatePassword(email, BCryptUtils.hashPassword(n));
                            msg = "Đổi mật khẩu thành công!";
                        }
                        if (getActivity() != null) getActivity().runOnUiThread(() ->
                                Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show());
                    }).start();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private LinearLayout wrap(View... fields) {
        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);
        for (View f : fields) box.addView(f);
        return box;
    }
}
