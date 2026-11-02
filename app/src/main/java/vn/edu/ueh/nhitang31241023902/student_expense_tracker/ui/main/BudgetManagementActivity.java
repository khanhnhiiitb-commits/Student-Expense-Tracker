package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.BudgetCategoryAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.BudgetEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class BudgetManagementActivity extends AppCompatActivity {
    private LinearLayout btnBack;
    private ImageView btnPreviousMonth, btnNextMonth;
    private TextView tvCurrentMonth, tvTotalPercentage, tvTotalAmountSpent, tvTotalBudgetDetail, btnEditLimit;
    private ProgressBar pbTotalBudget;
    private RecyclerView rvBudgetCategories;
    private MaterialButton btnSetupLimit;
    private BudgetCategoryAdapter adapter;
    private int currentMonth, currentYear;
    private long currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_overview);
        initViews();
        setupCurrentMonth();
        setupListeners();
        setupRecyclerView();
    }

    @Override
    protected void onResume() { super.onResume(); loadBudgetData(); }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvTotalPercentage = findViewById(R.id.tvTotalPercentage);
        tvTotalAmountSpent = findViewById(R.id.tvTotalAmountSpent);
        tvTotalBudgetDetail = findViewById(R.id.tvTotalBudgetDetail);
        pbTotalBudget = findViewById(R.id.pbTotalBudget);
        rvBudgetCategories = findViewById(R.id.rvBudgetCategories);
        btnSetupLimit = findViewById(R.id.btnSetupLimit);
        btnEditLimit = findViewById(R.id.btnEditLimit);
    }

    private void setupCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) + 1;
        currentYear = cal.get(Calendar.YEAR);
        updateMonthDisplay();
    }

    private void updateMonthDisplay() { tvCurrentMonth.setText("Tháng " + currentMonth + "/" + currentYear); }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnPreviousMonth.setOnClickListener(v -> {
            currentMonth--; if (currentMonth < 1) { currentMonth = 12; currentYear--; }
            updateMonthDisplay(); loadBudgetData();
        });
        btnNextMonth.setOnClickListener(v -> {
            currentMonth++; if (currentMonth > 12) { currentMonth = 1; currentYear++; }
            updateMonthDisplay(); loadBudgetData();
        });
        View.OnClickListener setupAction = v -> Toast.makeText(this, "Tính năng đang cập nhật", Toast.LENGTH_SHORT).show();
        btnSetupLimit.setOnClickListener(setupAction);
        btnEditLimit.setOnClickListener(setupAction);
    }

    private void setupRecyclerView() {
        rvBudgetCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BudgetCategoryAdapter(this);
        rvBudgetCategories.setAdapter(adapter);
    }

    private void loadBudgetData() {
        SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("USER_EMAIL", "");
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            UserEntity user = db.userDao().getUserByEmail(userEmail);
            if (user == null) return;
            currentUserId = user.userId;

            List<BudgetEntity> budgets = db.budgetDao().getAllBudgetsForMonth(currentUserId, currentMonth, currentYear);
            long totalTarget = 0; long totalSpent = 0;
            List<BudgetCategoryAdapter.BudgetDisplayModel> displayList = new ArrayList<>();

            for (BudgetEntity budget : budgets) {
                totalTarget += budget.amount;
                CategoryEntity category = db.categoryDao().getCategoryById(budget.categoryID);
                String catName = (category != null) ? category.categoryName : "Danh mục";

                long spent = 0; // Giả lập chưa tính được spent thực tế từ DB để tránh lỗi thiếu hàm DateUtils
                totalSpent += spent;
                displayList.add(new BudgetCategoryAdapter.BudgetDisplayModel(budget.categoryID, catName, budget.amount, spent));
            }

            long finalTotalTarget = totalTarget; long finalTotalSpent = totalSpent;
            long remaining = finalTotalTarget - finalTotalSpent;
            int finalTotalPercent = finalTotalTarget > 0 ? (int) ((double) finalTotalSpent / finalTotalTarget * 100) : 0;
            DecimalFormat formatter = new DecimalFormat("#,###");

            runOnUiThread(() -> {
                adapter.setData(displayList);
                tvTotalPercentage.setText(finalTotalPercent + "%");
                tvTotalAmountSpent.setText(formatter.format(finalTotalSpent) + " đ");
                pbTotalBudget.setProgress(Math.min(finalTotalPercent, 100));
                tvTotalBudgetDetail.setText("Hạn mức " + formatter.format(finalTotalTarget) + " đ · Còn " + formatter.format(remaining) + " đ");
            });
        }).start();
    }
}