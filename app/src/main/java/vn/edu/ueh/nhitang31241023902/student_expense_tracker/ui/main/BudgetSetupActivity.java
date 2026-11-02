package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.BudgetSetupAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.BudgetEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class BudgetSetupActivity extends AppCompatActivity {
    private RecyclerView rvSetup;
    private BudgetSetupAdapter adapter;
    private int currentMonth, currentYear;
    private long currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setup);

        currentMonth = getIntent().getIntExtra("MONTH", 1);
        currentYear = getIntent().getIntExtra("YEAR", 2026);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvSetup = findViewById(R.id.rvSetupBudgetCategories);
        rvSetup.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BudgetSetupAdapter();
        rvSetup.setAdapter(adapter);

        findViewById(R.id.btnSaveBudgetLimit).setOnClickListener(v -> saveBudgets());

        loadData();
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("USER_EMAIL", "");

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            UserEntity user = db.userDao().getUserByEmail(email);
            if (user == null) return;
            currentUserId = user.userId;

            // 1. Chỉ lấy danh mục CHI TIÊU
            List<CategoryEntity> expenses = db.categoryDao().getCategoriesByType("Expense", currentUserId);

            // 2. Lấy ngân sách đã thiết lập trong tháng này
            List<BudgetEntity> budgets = db.budgetDao().getAllBudgetsForMonth(currentUserId, currentMonth, currentYear);
            Map<Long, Long> budgetMap = new HashMap<>();
            for (BudgetEntity b : budgets) {
                budgetMap.put(b.categoryID, b.amount);
            }

            runOnUiThread(() -> adapter.setData(expenses, budgetMap));
        }).start();
    }

    private void saveBudgets() {
        Map<Long, Long> budgetMap = adapter.getBudgetMap();
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            for (Map.Entry<Long, Long> entry : budgetMap.entrySet()) {
                long catId = entry.getKey();
                long amount = entry.getValue();

                BudgetEntity existing = db.budgetDao().getBudgetForCategoryAndMonth(currentUserId, catId, currentMonth, currentYear);
                if (existing != null) {
                    if (amount > 0) {
                        existing.amount = amount;
                        db.budgetDao().updateBudget(existing);
                    } else {
                        db.budgetDao().deleteBudget(existing); // Nếu sửa thành 0 thì xóa luôn
                    }
                } else if (amount > 0) {
                    db.budgetDao().insertBudget(new BudgetEntity(currentUserId, catId, amount, currentMonth, currentYear));
                }
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Lưu ngân sách thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Đóng màn hình, quay lại BudgetFragment
            });
        }).start();
    }
}