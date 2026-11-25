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

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.android.material.button.MaterialButton;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.BudgetEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.RecurringExpenseProcessor;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BCryptUtils;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.ValidationUtils;

/** UC03 - Hồ sơ cá nhân: xem, đổi họ tên, đổi mật khẩu; UC02 - Đăng xuất. */
public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private TextView tvWalletScore, tvWalletScoreBadge;
    private TextView tvMonthlySavingsRate, tvMonthlySavingsBadge;
    private TextView tvSpendingStreak, tvSpendingStreakBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvWalletScore = view.findViewById(R.id.tvWalletScore);
        tvWalletScoreBadge = view.findViewById(R.id.tvWalletScoreBadge);
        tvMonthlySavingsRate = view.findViewById(R.id.tvMonthlySavingsRate);
        tvMonthlySavingsBadge = view.findViewById(R.id.tvMonthlySavingsBadge);
        tvSpendingStreak = view.findViewById(R.id.tvSpendingStreak);
        tvSpendingStreakBadge = view.findViewById(R.id.tvSpendingStreakBadge);
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

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    /**
     * Tính 3 chỉ số từ dữ liệu thật của tháng hiện tại:
     * - Tiết kiệm tháng = (thu - chi) / thu. Từ 20% trở lên là "Đạt mục tiêu".
     * - Đánh giá ví (0-100) = 60% theo tỷ lệ tiết kiệm (đạt 20% là đủ điểm)
     *   + 40% theo số danh mục không vượt ngân sách (nếu chưa đặt ngân sách thì chỉ tính phần tiết kiệm).
     * - Chuỗi ghi chép = số ngày liên tiếp có ghi giao dịch, tính đến hôm nay (hoặc hôm qua nếu hôm nay chưa ghi).
     */
    private void loadStats() {
        if (getContext() == null) return;
        final Context app = requireContext().getApplicationContext();
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(app);
            SharedPreferences prefs = app.getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            UserEntity u = db.userDao().getUserByEmail(prefs.getString("USER_EMAIL", ""));
            if (u == null) return;

            RecurringExpenseProcessor.generateDue(db, u.userId);
            List<TransactionEntity> txs = db.transactionDao().getTransactionsByUserId(u.userId);
            Map<Long, String> types = new HashMap<>();
            for (CategoryEntity c : db.categoryDao().getAllCategories()) types.put(c.categoryId, c.type);

            Calendar now = Calendar.getInstance();
            final int month = now.get(Calendar.MONTH) + 1;
            final int year = now.get(Calendar.YEAR);

            long income = 0, expense = 0;
            Map<Long, Long> spentByCat = new HashMap<>();
            Set<Long> days = new HashSet<>();
            Calendar c = Calendar.getInstance();
            for (TransactionEntity tx : txs) {
                if (tx.amount == null) continue;
                c.setTimeInMillis(tx.transactionDate);
                boolean sameMonth = c.get(Calendar.MONTH) + 1 == month && c.get(Calendar.YEAR) == year;
                days.add(startOfDay(c));
                if (!sameMonth) continue;
                long a = tx.amount.longValue();
                String type = types.get(tx.categoryID);
                if ("Income".equalsIgnoreCase(type)) {
                    income += a;
                } else if ("Expense".equalsIgnoreCase(type)) {
                    expense += a;
                    Long old = spentByCat.get(tx.categoryID);
                    spentByCat.put(tx.categoryID, (old == null ? 0 : old) + a);
                }
            }

            // Chuỗi ghi chép
            Calendar d = Calendar.getInstance();
            d.set(Calendar.HOUR_OF_DAY, 0); d.set(Calendar.MINUTE, 0);
            d.set(Calendar.SECOND, 0); d.set(Calendar.MILLISECOND, 0);
            if (!days.contains(d.getTimeInMillis())) d.add(Calendar.DAY_OF_MONTH, -1);
            int streak = 0;
            while (days.contains(d.getTimeInMillis())) {
                streak++;
                d.add(Calendar.DAY_OF_MONTH, -1);
            }

            // Tiết kiệm + Đánh giá ví
            final boolean hasData = income > 0 || expense > 0;
            final boolean hasIncome = income > 0;
            double rate = hasIncome ? (double) (income - expense) / income : 0;
            double savingsPart = hasIncome ? Math.max(0, Math.min(1, rate / 0.2)) : 0;

            List<BudgetEntity> budgets = db.budgetDao().getAllBudgetsForMonth(u.userId, month, year);
            double score;
            if (budgets != null && !budgets.isEmpty()) {
                int ok = 0;
                for (BudgetEntity b : budgets) {
                    Long spent = spentByCat.get(b.categoryID);
                    if ((spent == null ? 0 : spent) <= b.amount) ok++;
                }
                score = 100 * (0.6 * savingsPart + 0.4 * ok / budgets.size());
            } else {
                score = 100 * savingsPart;
            }

            final int finalScore = (int) Math.round(score);
            final double finalRate = rate;
            final int finalStreak = streak;

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                if (!hasData) {
                    tvWalletScore.setText("--");
                    tvWalletScoreBadge.setText("Chưa có dữ liệu");
                } else {
                    tvWalletScore.setText(String.valueOf(finalScore));
                    tvWalletScoreBadge.setText(finalScore >= 80 ? "Tốt" : finalScore >= 50 ? "Khá" : "Cần cải thiện");
                }

                if (!hasIncome) {
                    tvMonthlySavingsRate.setText("--");
                    tvMonthlySavingsBadge.setText("Chưa có thu nhập");
                } else if (finalRate < 0) {
                    tvMonthlySavingsRate.setText("0%");
                    tvMonthlySavingsBadge.setText("Chi vượt thu");
                } else {
                    tvMonthlySavingsRate.setText(Math.round(finalRate * 100) + "%");
                    tvMonthlySavingsBadge.setText(finalRate >= 0.2 ? "Đạt mục tiêu" : "Cần cải thiện");
                }

                tvSpendingStreak.setText("🔥 " + finalStreak);
                tvSpendingStreakBadge.setText(finalStreak == 0 ? "Bắt đầu nào"
                        : finalStreak < 7 ? "Đang tập" : "Kỷ luật cao");
            });
        }).start();
    }

    private static long startOfDay(Calendar src) {
        Calendar c = (Calendar) src.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
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