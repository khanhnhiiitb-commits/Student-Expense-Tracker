package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.CategoryStat;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.ReportCategoryAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class ReportFragment extends Fragment {

    private static final int PERIOD_WEEK = 0;
    private static final int PERIOD_MONTH = 1;
    private static final int PERIOD_YEAR = 2;

    private static final int[] CHART_COLORS = {
            Color.parseColor("#1E88E5"), Color.parseColor("#F59E0B"),
            Color.parseColor("#16A34A"), Color.parseColor("#DC2626"),
            Color.parseColor("#8B5CF6"), Color.parseColor("#EC4899"),
            Color.parseColor("#14B8A6"), Color.parseColor("#F97316"),
            Color.parseColor("#64748B")
    };

    private final DecimalFormat moneyFormat =
            new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(new Locale("vi", "VN")));

    // Kỳ đang xem: mặc định là tháng hiện tại
    private int periodType = PERIOD_MONTH;
    private final Calendar anchor = Calendar.getInstance();

    private TextView tabWeek, tabMonth, tabYear, tvPeriod, tvSummarySubtitle;
    private TextView tvTotalIncome, tvTotalExpense, tvNetResult, tvEmptyExpense;
    private PieChart pieChart;
    private BarChart barChart;
    private RecyclerView rvCategoryLegend;
    private ReportCategoryAdapter categoryAdapter;

    public ReportFragment() {
        super(R.layout.fragment_report);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        ImageView btnPrev = view.findViewById(R.id.btnPrevPeriod);
        ImageView btnNext = view.findViewById(R.id.btnNextPeriod);
        tabWeek = view.findViewById(R.id.tabWeek);
        tabMonth = view.findViewById(R.id.tabMonth);
        tabYear = view.findViewById(R.id.tabYear);
        tvPeriod = view.findViewById(R.id.tvPeriod);
        tvSummarySubtitle = view.findViewById(R.id.tvSummarySubtitle);
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        tvNetResult = view.findViewById(R.id.tvNetResult);
        tvEmptyExpense = view.findViewById(R.id.tvEmptyExpense);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        rvCategoryLegend = view.findViewById(R.id.rvCategoryLegend);

        categoryAdapter = new ReportCategoryAdapter();
        rvCategoryLegend.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategoryLegend.setAdapter(categoryAdapter);
        setupPieChart();
        setupBarChart();

        btnBack.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });

        tabWeek.setOnClickListener(v -> selectPeriodType(PERIOD_WEEK));
        tabMonth.setOnClickListener(v -> selectPeriodType(PERIOD_MONTH));
        tabYear.setOnClickListener(v -> selectPeriodType(PERIOD_YEAR));
        btnPrev.setOnClickListener(v -> shiftPeriod(-1));
        btnNext.setOnClickListener(v -> shiftPeriod(1));

        updateTabsUi();
        loadReport();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Quay lại tab thống kê sau khi thêm/sửa/xóa giao dịch thì tải lại số liệu
        if (tvTotalIncome != null) loadReport();
    }

    // ===================== Chọn kỳ =====================

    private void selectPeriodType(int type) {
        if (periodType == type) return;
        periodType = type;
        updateTabsUi();
        loadReport();
    }

    private void shiftPeriod(int step) {
        switch (periodType) {
            case PERIOD_WEEK:  anchor.add(Calendar.WEEK_OF_YEAR, step); break;
            case PERIOD_YEAR:  anchor.add(Calendar.YEAR, step); break;
            default:           anchor.add(Calendar.MONTH, step); break;
        }
        loadReport();
    }

    private void updateTabsUi() {
        styleTab(tabWeek, periodType == PERIOD_WEEK);
        styleTab(tabMonth, periodType == PERIOD_MONTH);
        styleTab(tabYear, periodType == PERIOD_YEAR);
    }

    private void styleTab(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.bg_tab_selected : 0);
        tab.setTextColor(requireContext().getColor(selected ? R.color.primary : R.color.text_sub));
        tab.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    /** Trả về [start, endExclusive) theo millis của kỳ đang chọn. */
    private long[] getRange() {
        Calendar start = (Calendar) anchor.clone();
        start.setFirstDayOfWeek(Calendar.MONDAY);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end;
        switch (periodType) {
            case PERIOD_WEEK:
                start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                end = (Calendar) start.clone();
                end.add(Calendar.DAY_OF_YEAR, 7);
                break;
            case PERIOD_YEAR:
                start.set(Calendar.DAY_OF_YEAR, 1);
                end = (Calendar) start.clone();
                end.add(Calendar.YEAR, 1);
                break;
            default:
                start.set(Calendar.DAY_OF_MONTH, 1);
                end = (Calendar) start.clone();
                end.add(Calendar.MONTH, 1);
                break;
        }
        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    private String getPeriodLabel(long startMillis, long endExclusiveMillis) {
        Calendar s = Calendar.getInstance();
        s.setTimeInMillis(startMillis);
        switch (periodType) {
            case PERIOD_WEEK:
                Calendar e = Calendar.getInstance();
                e.setTimeInMillis(endExclusiveMillis - 1);
                return String.format(Locale.getDefault(), "%02d/%02d - %02d/%02d/%d",
                        s.get(Calendar.DAY_OF_MONTH), s.get(Calendar.MONTH) + 1,
                        e.get(Calendar.DAY_OF_MONTH), e.get(Calendar.MONTH) + 1, e.get(Calendar.YEAR));
            case PERIOD_YEAR:
                return "Năm " + s.get(Calendar.YEAR);
            default:
                return String.format(Locale.getDefault(), "Tháng %02d/%d",
                        s.get(Calendar.MONTH) + 1, s.get(Calendar.YEAR));
        }
    }

    // ===================== 6.1.3 + 6.1.4: Tải & tính toán =====================

    private void loadReport() {
        if (getContext() == null) return;

        final long[] range = getRange();
        final String label = getPeriodLabel(range[0], range[1]);
        tvPeriod.setText(label);
        tvSummarySubtitle.setText(label + " · Tất cả nguồn tiền");

        final Context appContext = requireContext().getApplicationContext();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(appContext);

            SharedPreferences prefs = appContext.getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String email = prefs.getString("USER_EMAIL", "");
            UserEntity user = db.userDao().getUserByEmail(email);
            long userId = user != null ? user.userId : -1;

            List<TransactionEntity> incomes = db.transactionDao().getIncomeByDateRange(userId, range[0], range[1]);
            List<TransactionEntity> expenses = db.transactionDao().getExpenseByDateRange(userId, range[0], range[1]);

            // 6.1.3 – Tổng thu, tổng chi, số dư (BigDecimal để không sai số)
            BigDecimal totalIncome = sum(incomes);
            BigDecimal totalExpense = sum(expenses);
            BigDecimal balance = totalIncome.subtract(totalExpense);

            // 6.1.4 – Gom chi tiêu theo danh mục
            Map<Long, BigDecimal> byCategory = new HashMap<>();
            for (TransactionEntity tx : expenses) {
                if (tx.amount == null) continue;
                BigDecimal old = byCategory.get(tx.categoryID);
                byCategory.put(tx.categoryID, old == null ? tx.amount : old.add(tx.amount));
            }

            List<CategoryStat> stats = new ArrayList<>();
            for (Map.Entry<Long, BigDecimal> e : byCategory.entrySet()) {
                CategoryEntity cat = db.categoryDao().getCategoryById(e.getKey());
                String name = cat != null ? cat.categoryName : "Khác";
                double percent = totalExpense.signum() > 0
                        ? e.getValue().multiply(BigDecimal.valueOf(100))
                        .divide(totalExpense, 4, java.math.RoundingMode.HALF_UP).doubleValue()
                        : 0;
                stats.add(new CategoryStat(e.getKey(), name, e.getValue(), percent, 0));
            }
            // Sắp xếp giảm dần theo số tiền, rồi gán màu theo thứ tự
            Collections.sort(stats, (a, b) -> b.totalAmount.compareTo(a.totalAmount));
            List<CategoryStat> colored = new ArrayList<>();
            for (int i = 0; i < stats.size(); i++) {
                CategoryStat s = stats.get(i);
                colored.add(new CategoryStat(s.categoryId, s.categoryName, s.totalAmount,
                        s.percent, CHART_COLORS[i % CHART_COLORS.length]));
            }

            // Dòng tiền theo thời gian (biểu đồ cột)
            final String[] labels = getBucketLabels();
            final float[] incomeBuckets = new float[labels.length];
            final float[] expenseBuckets = new float[labels.length];
            fillBuckets(incomes, range[0], incomeBuckets);
            fillBuckets(expenses, range[0], expenseBuckets);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                showSummary(totalIncome, totalExpense, balance);
                showCategoryStats(colored);
                showCashFlow(labels, incomeBuckets, expenseBuckets);
            });
        }).start();
    }

    private BigDecimal sum(List<TransactionEntity> list) {
        BigDecimal total = BigDecimal.ZERO;
        if (list != null) {
            for (TransactionEntity tx : list) {
                if (tx.amount != null) total = total.add(tx.amount);
            }
        }
        return total;
    }

    // ===================== Dòng tiền theo thời gian =====================

    /** Tuần: 7 ngày. Tháng: 5 cụm tuần (1-7, 8-14...). Năm: 12 tháng. */
    private String[] getBucketLabels() {
        switch (periodType) {
            case PERIOD_WEEK:
                return new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            case PERIOD_YEAR:
                String[] m = new String[12];
                for (int i = 0; i < 12; i++) m[i] = "T" + (i + 1);
                return m;
            default:
                return new String[]{"1-7", "8-14", "15-21", "22-28", "29+"};
        }
    }

    private void fillBuckets(List<TransactionEntity> list, long rangeStart, float[] out) {
        if (list == null) return;
        Calendar c = Calendar.getInstance();
        for (TransactionEntity tx : list) {
            if (tx.amount == null) continue;
            c.setTimeInMillis(tx.transactionDate);
            int idx;
            switch (periodType) {
                case PERIOD_WEEK:
                    idx = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // T2=0 ... CN=6
                    break;
                case PERIOD_YEAR:
                    idx = c.get(Calendar.MONTH);
                    break;
                default:
                    idx = (c.get(Calendar.DAY_OF_MONTH) - 1) / 7;
                    break;
            }
            if (idx >= 0 && idx < out.length) out[idx] += tx.amount.floatValue();
        }
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setNoDataText("Chưa có dữ liệu trong kỳ này");
        barChart.setNoDataTextColor(Color.GRAY);
        barChart.setDrawGridBackground(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float v) {
                if (v >= 1_000_000f) return trimZero(v / 1_000_000f) + "tr";
                if (v >= 1_000f) return trimZero(v / 1_000f) + "k";
                return String.valueOf((int) v);
            }
        });
        barChart.getLegend().setEnabled(true);
    }

    private String trimZero(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.format(Locale.US, "%.1f", v);
    }

    private void showCashFlow(String[] labels, float[] income, float[] expense) {
        boolean hasData = false;
        for (int i = 0; i < labels.length; i++) {
            if (income[i] > 0 || expense[i] > 0) { hasData = true; break; }
        }
        if (!hasData) {
            barChart.clear();
            barChart.invalidate();
            return;
        }

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            incomeEntries.add(new BarEntry(i, income[i]));
            expenseEntries.add(new BarEntry(i, expense[i]));
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu");
        incomeSet.setColor(requireContext().getColor(R.color.income_green));
        incomeSet.setDrawValues(false);
        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi");
        expenseSet.setColor(requireContext().getColor(R.color.expense_red));
        expenseSet.setDrawValues(false);

        BarData data = new BarData(incomeSet, expenseSet);
        // (barWidth + barSpace) * 2 + groupSpace = 1
        float groupSpace = 0.1f, barSpace = 0.05f, barWidth = 0.4f;
        data.setBarWidth(barWidth);

        XAxis x = barChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setGranularity(1f);
        x.setCenterAxisLabels(true);
        x.setLabelCount(labels.length);
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setAxisMinimum(0f);
        x.setAxisMaximum(labels.length);

        barChart.setData(data);
        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.invalidate();
        barChart.animateY(500);
    }

    // ===================== Hiển thị =====================

    private void showSummary(BigDecimal income, BigDecimal expense, BigDecimal balance) {
        tvTotalIncome.setText(moneyFormat.format(income) + " đ");
        tvTotalExpense.setText(moneyFormat.format(expense) + " đ");

        String sign = balance.signum() > 0 ? "+" : "";
        tvNetResult.setText("Thu - Chi: " + sign + moneyFormat.format(balance) + " đ");
        tvNetResult.setTextColor(requireContext().getColor(
                balance.signum() >= 0 ? R.color.income_green : R.color.expense_red));
    }

    private void showCategoryStats(List<CategoryStat> stats) {
        categoryAdapter.setData(stats);

        if (stats.isEmpty()) {
            tvEmptyExpense.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            rvCategoryLegend.setVisibility(View.GONE);
            return;
        }
        tvEmptyExpense.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        rvCategoryLegend.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (CategoryStat s : stats) {
            entries.add(new PieEntry(s.totalAmount.floatValue(), s.categoryName));
            colors.add(s.color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.animateY(600);
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false); // đã có danh sách chú thích bên dưới
        pieChart.setDrawEntryLabels(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setNoDataText("Chưa có khoản chi trong kỳ này");
    }
}
