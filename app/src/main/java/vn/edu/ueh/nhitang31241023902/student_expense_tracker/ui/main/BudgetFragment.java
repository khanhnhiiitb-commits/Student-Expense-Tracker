package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BudgetWarningUtils;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.BudgetCategoryAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.BudgetEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class BudgetFragment extends Fragment {

    private View btnBack;
    private ImageView btnPreviousMonth, btnNextMonth;
    private TextView tvCurrentMonth, tvTotalPercentage, tvTotalAmountSpent, tvTotalBudgetDetail, btnEditLimit;
    private ProgressBar pbTotalBudget;
    private RecyclerView rvBudgetCategories;
    private MaterialButton btnSetupLimit;

    private BudgetCategoryAdapter adapter;
    private int currentMonth, currentYear;
    private long currentUserId = -1;
    private TextView tvBudgetWarning;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Tái sử dụng lại layout ngân sách đã có
        View view = inflater.inflate(R.layout.activity_budget_overview, container, false);

        initViews(view);
        setupCurrentMonth();
        setupListeners();
        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgetData(); // Tự động load lại dữ liệu mỗi khi người dùng bấm vào tab Ngân sách
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        tvTotalPercentage = view.findViewById(R.id.tvTotalPercentage);
        tvTotalAmountSpent = view.findViewById(R.id.tvTotalAmountSpent);
        tvTotalBudgetDetail = view.findViewById(R.id.tvTotalBudgetDetail);
        pbTotalBudget = view.findViewById(R.id.pbTotalBudget);
        rvBudgetCategories = view.findViewById(R.id.rvBudgetCategories);
        btnSetupLimit = view.findViewById(R.id.btnSetupLimit);
        btnEditLimit = view.findViewById(R.id.btnEditLimit);
        tvBudgetWarning = view.findViewById(R.id.tvBudgetWarning);
    }

    private void setupCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) + 1;
        currentYear = cal.get(Calendar.YEAR);
        updateMonthDisplay();
    }

    private void showBudgetWarning(long budget, long spent) {

        BudgetWarningUtils.WarningLevel level =
                BudgetWarningUtils.checkBudget(budget, spent);

        String message = BudgetWarningUtils.getMessage(level);

        tvBudgetWarning.setText(message);
        int color = BudgetWarningUtils.getColor(level);
        tvBudgetWarning.setTextColor(color);
        tvTotalPercentage.setTextColor(color);
        pbTotalBudget.setProgressTintList(ColorStateList.valueOf(color));

        if (level == BudgetWarningUtils.WarningLevel.NORMAL) {
            tvBudgetWarning.setVisibility(View.GONE);
        } else {
            tvBudgetWarning.setVisibility(View.VISIBLE);
        }
    }

    private void updateMonthDisplay() {
        tvCurrentMonth.setText("Tháng " + currentMonth + "/" + currentYear);
    }

    private void setupListeners() {
        // Nút Back trong Fragment thường không cần thiết vì người dùng có thể dùng Bottom Nav để chuyển tab
        if (btnBack != null) {
            btnBack.setVisibility(View.GONE);
        }

        btnPreviousMonth.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 1) {
                currentMonth = 12;
                currentYear--;
            }
            updateMonthDisplay();
            loadBudgetData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }
            updateMonthDisplay();
            loadBudgetData();
        });

        View.OnClickListener setupAction = v -> {
            Toast.makeText(requireContext(), "Tính năng Cập nhật hạn mức đang phát triển", Toast.LENGTH_SHORT).show();
            // Nếu bạn đã làm BudgetSetupActivity, hãy mở comment 3 dòng dưới đây:
            // Intent intent = new Intent(requireContext(), BudgetSetupActivity.class);
            // intent.putExtra("MONTH", currentMonth);
            // intent.putExtra("YEAR", currentYear);
            // startActivity(intent);
        };

        btnSetupLimit.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BudgetSetupActivity.class);
            intent.putExtra("MONTH", currentMonth);
            intent.putExtra("YEAR", currentYear);
            startActivity(intent);
        });
        btnEditLimit.setOnClickListener(setupAction);
    }

    private void setupRecyclerView() {
        rvBudgetCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BudgetCategoryAdapter(requireContext());
        rvBudgetCategories.setAdapter(adapter);
    }

    private void loadBudgetData() {
        if (getContext() == null) return;
        final Context appContext = requireContext().getApplicationContext();
        final int monthToLoad = currentMonth;
        final int yearToLoad = currentYear;

        SharedPreferences prefs = requireContext().getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("USER_EMAIL", "");

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(appContext);
            UserEntity user = db.userDao().getUserByEmail(userEmail);

            if (user == null) return;
            currentUserId = user.userId;

            // 1. Lấy danh sách ngân sách đã thiết lập trong tháng
            List<BudgetEntity> budgets = db.budgetDao().getAllBudgetsForMonth(currentUserId, monthToLoad, yearToLoad);

            // 2. Lấy tất cả giao dịch của người dùng
            List<TransactionEntity> allTransactions = db.transactionDao().getTransactionsByUserId(currentUserId);

            // 3. Tính tổng tiền đã chi cho từng danh mục trong tháng hiện hành
            Map<Long, Long> spentMap = new HashMap<>();
            Calendar cal = Calendar.getInstance();

            if (allTransactions != null) {
                for (TransactionEntity tx : allTransactions) {
                    if (tx.amount == null) continue;

                    cal.setTimeInMillis(tx.transactionDate);
                    int txMonth = cal.get(Calendar.MONTH) + 1;
                    int txYear = cal.get(Calendar.YEAR);

                    // Chỉ lọc các giao dịch nằm trong tháng/năm đang xem trên màn hình Ngân sách
                    if (txMonth == monthToLoad && txYear == yearToLoad) {
                        CategoryEntity cat = db.categoryDao().getCategoryById(tx.categoryID);

                        // Chỉ cộng dồn tiền nếu danh mục đó là CHI TIÊU (Expense)
                        if (cat != null && "Expense".equalsIgnoreCase(cat.type)) {
                            long txAmount = tx.amount.longValue();
                            long currentSpent = spentMap.containsKey(tx.categoryID) ? spentMap.get(tx.categoryID) : 0L;
                            spentMap.put(tx.categoryID, currentSpent + txAmount);
                        }
                    }
                }
            }

            long totalTarget = 0;
            long totalSpent = 0;
            List<BudgetCategoryAdapter.BudgetDisplayModel> displayList = new ArrayList<>();

            // 4. Ghép dữ liệu Ngân sách và Số tiền đã chi thực tế lại với nhau
            for (BudgetEntity budget : budgets) {
                totalTarget += budget.amount;
                CategoryEntity category = db.categoryDao().getCategoryById(budget.categoryID);
                String catName = (category != null) ? category.categoryName : "Danh mục";

                // Lấy số tiền đã tiêu từ Map (nếu tháng này chưa tiêu đồng nào thì = 0)
                long spent = spentMap.containsKey(budget.categoryID) ? spentMap.get(budget.categoryID) : 0L;
                totalSpent += spent;
                displayList.add(new BudgetCategoryAdapter.BudgetDisplayModel(budget.categoryID, catName, budget.amount, spent));
            }

            final long finalTotalTarget = totalTarget;
            final long finalTotalSpent = totalSpent;
            final long remaining = finalTotalTarget - finalTotalSpent;

            // Tránh lỗi chia cho 0
            final int finalTotalPercent = finalTotalTarget > 0 ? (int) ((double) finalTotalSpent / finalTotalTarget * 100) : 0;
            final DecimalFormat formatter = new DecimalFormat("#,###");

            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || monthToLoad != currentMonth || yearToLoad != currentYear) return;
                    // Cập nhật lên giao diện
                    adapter.setData(displayList);
                    tvTotalPercentage.setText(finalTotalPercent + "%");
                    tvTotalAmountSpent.setText(formatter.format(finalTotalSpent) + " đ");
                    pbTotalBudget.setProgress(Math.min(finalTotalPercent, 100));
                    tvTotalBudgetDetail.setText("Hạn mức " + formatter.format(finalTotalTarget) + " đ · Còn " + formatter.format(remaining) + " đ");
                    showBudgetWarning(finalTotalTarget, finalTotalSpent);
                });
            }
        }).start();
    }
}
