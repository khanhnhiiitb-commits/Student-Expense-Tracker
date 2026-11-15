package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class ReportFragment extends Fragment {
    private enum Period { WEEK, MONTH, YEAR, CUSTOM }
    private Period period = Period.MONTH;
    private final Calendar cursor = Calendar.getInstance();
    private Calendar customStart, customEnd;
    private TextView tvPeriod, tvSubtitle, tvIncome, tvExpense, tvNet, tvEmpty;
    private TextView tabWeek, tabMonth, tabYear;
    private PieChart pieChart;
    private BarChart barChart;
    private RecyclerView legend;
    private final DecimalFormat money = new DecimalFormat("#,##0");
    private int requestVersion = 0;

    public ReportFragment() { super(R.layout.fragment_report); }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        tvPeriod = view.findViewById(R.id.tvPeriod);
        tvSubtitle = view.findViewById(R.id.tvSummarySubtitle);
        tvIncome = view.findViewById(R.id.tvTotalIncome);
        tvExpense = view.findViewById(R.id.tvTotalExpense);
        tvNet = view.findViewById(R.id.tvNetResult);
        tvEmpty = view.findViewById(R.id.tvEmptyExpense);
        tabWeek = view.findViewById(R.id.tabWeek);
        tabMonth = view.findViewById(R.id.tabMonth);
        tabYear = view.findViewById(R.id.tabYear);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        legend = view.findViewById(R.id.rvCategoryLegend);
        legend.setLayoutManager(new LinearLayoutManager(requireContext()));
        legend.setNestedScrollingEnabled(false);
        view.<ImageView>findViewById(R.id.btnBack).setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
            if (nav != null) nav.setSelectedItemId(R.id.nav_home);
        });
        tabWeek.setOnClickListener(v -> select(Period.WEEK));
        tabMonth.setOnClickListener(v -> select(Period.MONTH));
        tabYear.setOnClickListener(v -> select(Period.YEAR));
        view.findViewById(R.id.btnPrevPeriod).setOnClickListener(v -> shift(-1));
        view.findViewById(R.id.btnNextPeriod).setOnClickListener(v -> shift(1));
        view.findViewById(R.id.btnDateRange).setOnClickListener(v -> chooseRange());
        // Dữ liệu được tải trong onResume để luôn cập nhật sau khi quay lại từ màn hình thêm/sửa giao dịch.
    }

    @Override public void onResume() {
        super.onResume();
        if (getView() != null) refresh();
    }

    @Override public void onDestroyView() {
        ++requestVersion; // Hủy hiệu lực của kết quả truy vấn đang chờ.
        super.onDestroyView();
    }

    private void select(Period next) { period = next; cursor.setTimeInMillis(System.currentTimeMillis()); refresh(); }
    private void shift(int delta) {
        if (period == Period.CUSTOM) { Toast.makeText(requireContext(), "Chọn khoảng ngày mới để thay đổi", Toast.LENGTH_SHORT).show(); return; }
        cursor.add(period == Period.WEEK ? Calendar.WEEK_OF_YEAR : period == Period.MONTH ? Calendar.MONTH : Calendar.YEAR, delta);
        refresh();
    }
    private void chooseRange() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, y, m, d) -> {
            Calendar start = Calendar.getInstance(); start.clear(); start.set(y, m, d);
            new DatePickerDialog(requireContext(), (picker2, y2, m2, d2) -> {
                Calendar end = Calendar.getInstance(); end.clear(); end.set(y2, m2, d2);
                if (end.before(start)) { Toast.makeText(requireContext(), "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu", Toast.LENGTH_SHORT).show(); return; }
                customStart = start; customEnd = end; period = Period.CUSTOM; refresh();
            }, y, m, d).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }
    private static Calendar startOfDay(Calendar c) {
        Calendar result = (Calendar) c.clone();
        result.set(Calendar.HOUR_OF_DAY, 0); result.set(Calendar.MINUTE, 0);
        result.set(Calendar.SECOND, 0); result.set(Calendar.MILLISECOND, 0);
        return result;
    }
    private Calendar[] bounds() {
        Calendar start = startOfDay(cursor), end;
        if (period == Period.CUSTOM && customStart != null && customEnd != null) {
            start = startOfDay(customStart); end = startOfDay(customEnd); end.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            if (period == Period.WEEK) {
                int offset = (start.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Monday first
                start.add(Calendar.DAY_OF_MONTH, -offset);
                end = (Calendar) start.clone(); end.add(Calendar.DAY_OF_MONTH, 7);
            } else if (period == Period.MONTH) {
                start.set(Calendar.DAY_OF_MONTH, 1);
                end = (Calendar) start.clone(); end.add(Calendar.MONTH, 1);
            } else {
                start.set(Calendar.DAY_OF_YEAR, 1);
                end = (Calendar) start.clone(); end.add(Calendar.YEAR, 1);
            }
        }
        return new Calendar[]{start, end};
    }
    private String label(Calendar start, Calendar end) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        Calendar last = (Calendar) end.clone(); last.add(Calendar.MILLISECOND, -1);
        if (period == Period.MONTH) return "Tháng " + (start.get(Calendar.MONTH) + 1) + "/" + start.get(Calendar.YEAR);
        if (period == Period.YEAR) return "Năm " + start.get(Calendar.YEAR);
        return df.format(start.getTime()) + " - " + df.format(last.getTime());
    }
    // BUG_FR08_01: đổi nền và màu chữ thật sự, không chỉ thay đổi selected state.
    private void updatePeriodTabs() {
        styleTab(tabWeek, period == Period.WEEK);
        styleTab(tabMonth, period == Period.MONTH);
        styleTab(tabYear, period == Period.YEAR);
    }

    private void styleTab(TextView tab, boolean active) {
        tab.setSelected(active);
        tab.setBackground(active ? ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_selected) : null);
        tab.setTextColor(ContextCompat.getColor(requireContext(), active ? R.color.primary : R.color.text_sub));
        tab.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void refresh() {
        if (!isAdded() || getView() == null) return;
        Calendar[] range = bounds();
        long start = range[0].getTimeInMillis(), end = range[1].getTimeInMillis();
        String periodLabel = label(range[0], range[1]);
        tvPeriod.setText(periodLabel); tvSubtitle.setText(periodLabel + " · Tất cả nguồn tiền");
        updatePeriodTabs();
        int currentRequest = ++requestVersion;
        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(appContext);
                SharedPreferences prefs = appContext.getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
                String email = prefs.getString("USER_EMAIL", "");
                UserEntity user = email.isEmpty() ? null : db.userDao().getUserByEmail(email);
                if (user == null) {
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        if (isAdded() && currentRequest == requestVersion) Toast.makeText(requireContext(), "Không tìm thấy tài khoản đăng nhập", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                List<TransactionEntity> incomes = db.transactionDao().getIncomeByDateRange(user.userId, start, end);
                List<TransactionEntity> expenses = db.transactionDao().getExpenseByDateRange(user.userId, start, end);
                Map<Long, String> categoryNames = new LinkedHashMap<>();
                for (TransactionEntity tx : expenses) if (!categoryNames.containsKey(tx.categoryID)) {
                    CategoryEntity cat = db.categoryDao().getCategoryById(tx.categoryID);
                    categoryNames.put(tx.categoryID, cat == null ? "Khác" : cat.categoryName);
                }
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    if (isAdded() && getView() != null && currentRequest == requestVersion)
                        render(incomes, expenses, categoryNames, start, end);
                });
            } catch (Exception ex) {
                android.util.Log.e("ReportFragment", "Load report failed", ex);
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    if (isAdded() && currentRequest == requestVersion) Toast.makeText(requireContext(), "Không thể tải báo cáo", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private static BigDecimal total(List<TransactionEntity> txs) {
        BigDecimal sum = BigDecimal.ZERO;
        for (TransactionEntity tx : txs) if (tx.amount != null) sum = sum.add(tx.amount);
        return sum;
    }
    private void render(List<TransactionEntity> incomes, List<TransactionEntity> expenses,
                        Map<Long, String> names, long start, long end) {
        BigDecimal income = total(incomes), expense = total(expenses), net = income.subtract(expense);
        tvIncome.setText(money.format(income) + " đ");
        tvExpense.setText(money.format(expense) + " đ");
        tvNet.setText("Thu - Chi: " + money.format(net) + " đ");
        tvNet.setTextColor(requireContext().getColor(net.signum() < 0 ? R.color.expense_red : R.color.income_green));
        Map<Long, BigDecimal> categoryTotals = new LinkedHashMap<>();
        for (TransactionEntity tx : expenses) if (tx.amount != null)
            categoryTotals.put(tx.categoryID, categoryTotals.getOrDefault(tx.categoryID, BigDecimal.ZERO).add(tx.amount));
        ArrayList<PieEntry> slices = new ArrayList<>();
        ArrayList<String> legendLines = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> item : categoryTotals.entrySet()) {
            if (item.getValue().signum() <= 0) continue;
            String name = names.getOrDefault(item.getKey(), "Khác");
            slices.add(new PieEntry(item.getValue().floatValue(), name));
            legendLines.add(name + ": " + money.format(item.getValue()) + " đ");
        }
        boolean hasExpense = !slices.isEmpty();
        tvEmpty.setVisibility(hasExpense ? View.GONE : View.VISIBLE);
        pieChart.setVisibility(hasExpense ? View.VISIBLE : View.GONE);
        legend.setAdapter(new LegendAdapter(legendLines));
        if (hasExpense) {
            PieDataSet set = new PieDataSet(slices, "Danh mục");
            set.setColors(ColorTemplate.MATERIAL_COLORS);
            set.setSliceSpace(2f);
            PieData data = new PieData(set); data.setDrawValues(false);
            pieChart.setData(data); pieChart.getDescription().setEnabled(false);
            pieChart.getLegend().setEnabled(false); pieChart.setDrawEntryLabels(false);
            pieChart.setUsePercentValues(false); pieChart.invalidate();
        } else pieChart.clear();
        // BUG-FR08-02: luôn cập nhật biểu đồ cùng lần tải với tổng thu/chi.
        if (incomes.isEmpty() && expenses.isEmpty()) {
            barChart.clear();
            barChart.setVisibility(View.GONE);
        } else {
            barChart.setVisibility(View.VISIBLE);
            drawBars(incomes, expenses, start, end);
        }
    }
    private void drawBars(List<TransactionEntity> incomes, List<TransactionEntity> expenses, long start, long end) {
        Calendar c = Calendar.getInstance(); c.setTimeInMillis(start);
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Long> boundaries = new ArrayList<>();
        boolean byMonth = period == Period.YEAR;
        boolean byDay = period == Period.WEEK || period == Period.CUSTOM;
        // Custom ranges longer than 60 days aggregate by month to avoid an unreadable chart.
        if (period == Period.CUSTOM && (end - start) > 60L * 86400000L) { byMonth = true; byDay = false; }
        SimpleDateFormat df = new SimpleDateFormat(byMonth ? "MM/yy" : byDay ? "dd/MM" : "dd/MM", Locale.getDefault());
        while (c.getTimeInMillis() < end && labels.size() < 370) {
            boundaries.add(c.getTimeInMillis()); labels.add(df.format(c.getTime()));
            if (byMonth) c.add(Calendar.MONTH, 1);
            else if (byDay) c.add(Calendar.DAY_OF_MONTH, 1);
            else c.add(Calendar.DAY_OF_MONTH, 1);
        }
        boundaries.add(end);
        ArrayList<BarEntry> inBars = new ArrayList<>(), outBars = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            long a = boundaries.get(i), b = boundaries.get(i + 1);
            inBars.add(new BarEntry(i, sumBetween(incomes, a, b)));
            outBars.add(new BarEntry(i, sumBetween(expenses, a, b)));
        }
        BarDataSet inSet = new BarDataSet(inBars, "Thu"); inSet.setColor(requireContext().getColor(R.color.income_green));
        BarDataSet outSet = new BarDataSet(outBars, "Chi"); outSet.setColor(requireContext().getColor(R.color.expense_red));
        BarData data = new BarData(inSet, outSet); data.setBarWidth(0.42f); data.setDrawValues(false);
        barChart.setData(data); barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        XAxis axis = barChart.getXAxis(); axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setValueFormatter(new IndexAxisValueFormatter(labels)); axis.setGranularity(1f);
        axis.setLabelRotationAngle(-45f);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMinimum(-0.5f);
        barChart.getXAxis().setAxisMaximum(labels.size() - 0.5f);
        barChart.groupBars(-0.5f, 0.12f, 0.02f); // 2 * (0.42 + 0.02) + 0.12 = 1
        barChart.setVisibleXRangeMaximum(Math.min(8, labels.size()));
        barChart.moveViewToX(Math.max(0, labels.size() - 8));
        barChart.invalidate();
    }
    private static float sumBetween(List<TransactionEntity> list, long a, long b) {
        BigDecimal sum = BigDecimal.ZERO;
        for (TransactionEntity tx : list) if (tx.transactionDate >= a && tx.transactionDate < b && tx.amount != null)
            sum = sum.add(tx.amount);
        return sum.floatValue();
    }
    private static class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.Holder> {
        private final List<String> lines;
        LegendAdapter(List<String> lines) { this.lines = lines; }
        @NonNull @Override public Holder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int type) {
            TextView text = new TextView(parent.getContext());
            int padding = (int) (8 * parent.getResources().getDisplayMetrics().density);
            text.setPadding(padding, padding, padding, padding);
            return new Holder(text);
        }
        @Override public void onBindViewHolder(@NonNull Holder holder, int position) { holder.text.setText(lines.get(position)); }
        @Override public int getItemCount() { return lines.size(); }
        static class Holder extends RecyclerView.ViewHolder {
            final TextView text;
            Holder(TextView text) { super(text); this.text = text; }
        }
    }
}
