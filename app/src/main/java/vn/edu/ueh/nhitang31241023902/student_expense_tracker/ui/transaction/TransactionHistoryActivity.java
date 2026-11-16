package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.TransactionAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

/**
 * UC12 - Lịch sử giao dịch: xem theo tháng, lọc Tất cả / Chi tiêu / Thu nhập,
 * tìm kiếm theo ghi chú hoặc tên danh mục, bấm vào dòng để sửa hoặc xóa.
 */
public class TransactionHistoryActivity extends AppCompatActivity {

    private static final int FILTER_ALL = 0, FILTER_EXPENSE = 1, FILTER_INCOME = 2;

    private TextView tvCurrentMonth, tvTotalIncome, tvTotalExpense;
    private TextView tabAll, tabExpense, tabIncome;
    private View layoutEmpty;
    private RecyclerView rcv;
    private TransactionAdapter adapter;

    private int month, year;
    private int filter = FILTER_ALL;
    private String query = "";
    private int loadVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvTotalIncome = findViewById(R.id.tvTotalIncomeHistory);
        tvTotalExpense = findViewById(R.id.tvTotalExpenseHistory);
        tabAll = findViewById(R.id.tabAll);
        tabExpense = findViewById(R.id.tabExpenseOnly);
        tabIncome = findViewById(R.id.tabIncomeOnly);
        layoutEmpty = findViewById(R.id.layoutEmptyState);
        rcv = findViewById(R.id.rcvTransactionHistory);

        Calendar cal = Calendar.getInstance();
        month = cal.get(Calendar.MONTH) + 1;
        year = cal.get(Calendar.YEAR);

        rcv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, this::showOptions);
        rcv.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSearchTransaction).setOnClickListener(v -> showSearchDialog());
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            if (--month < 1) { month = 12; year--; }
            load();
        });
        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            if (++month > 12) { month = 1; year++; }
            load();
        });
        tabAll.setOnClickListener(v -> setFilter(FILTER_ALL));
        tabExpense.setOnClickListener(v -> setFilter(FILTER_EXPENSE));
        tabIncome.setOnClickListener(v -> setFilter(FILTER_INCOME));
        updateTabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void setFilter(int f) {
        filter = f;
        updateTabs();
        load();
    }

    private void updateTabs() {
        tabAll.setSelected(filter == FILTER_ALL);
        tabExpense.setSelected(filter == FILTER_EXPENSE);
        tabIncome.setSelected(filter == FILTER_INCOME);
    }

    private void showSearchDialog() {
        final EditText edt = new EditText(this);
        edt.setHint("Ghi chú hoặc tên danh mục");
        edt.setInputType(InputType.TYPE_CLASS_TEXT);
        edt.setText(query);
        edt.setSelection(edt.getText().length());
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        edt.setPadding(pad, pad, pad, pad);
        new AlertDialog.Builder(this)
                .setTitle("Tìm kiếm giao dịch")
                .setView(edt)
                .setPositiveButton("Tìm", (d, w) -> {
                    query = edt.getText().toString().trim();
                    load();
                })
                .setNeutralButton("Xóa tìm kiếm", (d, w) -> {
                    query = "";
                    load();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void load() {
        tvCurrentMonth.setText("Tháng " + month + ", " + year);
        final int version = ++loadVersion;
        final int m = month, y = year, f = filter;
        final String q = query.toLowerCase(Locale.getDefault());
        final Context appContext = getApplicationContext();
        final String email = getSharedPreferences("ExpenseAppPrefs", MODE_PRIVATE).getString("USER_EMAIL", "");

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(appContext);
            UserEntity user = db.userDao().getUserByEmail(email);
            if (user == null) return;

            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(y, m - 1, 1, 0, 0, 0);
            long start = c.getTimeInMillis();
            c.add(Calendar.MONTH, 1);
            long end = c.getTimeInMillis();

            List<TransactionEntity> all = db.transactionDao().getTransactionsByDateRange(user.userId, start, end);
            Map<Long, CategoryEntity> cats = new HashMap<>();
            for (CategoryEntity ce : db.categoryDao().getAllCategories()) cats.put(ce.categoryId, ce);

            BigDecimal income = BigDecimal.ZERO, expense = BigDecimal.ZERO;
            List<TransactionEntity> shown = new ArrayList<>();
            for (TransactionEntity tx : all) {
                if (tx.amount == null) continue;
                CategoryEntity cat = cats.get(tx.categoryID);
                boolean isIncome = cat != null && "Income".equalsIgnoreCase(cat.type);
                if (isIncome) income = income.add(tx.amount); else expense = expense.add(tx.amount);

                if (f == FILTER_EXPENSE && isIncome) continue;
                if (f == FILTER_INCOME && !isIncome) continue;
                if (!q.isEmpty()) {
                    String note = tx.note != null ? tx.note.toLowerCase(Locale.getDefault()) : "";
                    String name = cat != null && cat.categoryName != null
                            ? cat.categoryName.toLowerCase(Locale.getDefault()) : "";
                    if (!note.contains(q) && !name.contains(q)) continue;
                }
                shown.add(tx);
            }

            final BigDecimal fi = income, fe = expense;
            runOnUiThread(() -> {
                if (version != loadVersion || isFinishing() || isDestroyed()) return;
                DecimalFormat df = new DecimalFormat("#,###");
                tvTotalIncome.setText("+" + df.format(fi) + " đ");
                tvTotalExpense.setText("-" + df.format(fe) + " đ");
                adapter.setData(shown, cats);
                layoutEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
                rcv.setVisibility(shown.isEmpty() ? View.GONE : View.VISIBLE);
            });
        }).start();
    }

    private void showOptions(TransactionEntity tx) {
        String[] options = {"✏️ Chỉnh sửa giao dịch", "🗑️ Xóa giao dịch"};
        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn giao dịch")
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        Intent i = new Intent(this, AddTransactionActivity.class);
                        i.putExtra("EDIT_TRANSACTION_ID", tx.transactionID);
                        startActivity(i);
                    } else {
                        confirmDelete(tx);
                    }
                })
                .show();
    }

    private void confirmDelete(TransactionEntity tx) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa", (d, w) -> new Thread(() -> {
                    AppDatabase.getDatabase(getApplicationContext()).transactionDao().deleteTransaction(tx);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show();
                        load();
                    });
                }).start())
                .setNegativeButton("Hủy", null)
                .show();
    }
}
