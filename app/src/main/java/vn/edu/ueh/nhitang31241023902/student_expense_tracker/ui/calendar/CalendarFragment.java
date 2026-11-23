package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.CalendarDayAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.TransactionAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction.AddTransactionActivity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.RecurringExpenseProcessor;

/** Tab Lịch: xem thu/chi theo ngày trong tháng, kèm tổng kết tháng. */
public class CalendarFragment extends Fragment {

    private static final int GRID_CELLS = 42; // 6 hàng x 7 cột

    private TextView tvMonthTitle, tvMonthIncome, tvMonthExpense, tvMonthTotal;
    private TextView tvSelectedDay, tvDayEmpty;
    private RecyclerView rcvDayTransactions;

    private CalendarDayAdapter dayAdapter;
    private TransactionAdapter transactionAdapter;

    /** Ngày 1 của tháng đang xem, 00:00. */
    private Calendar monthStart = firstOfMonth(Calendar.getInstance());
    private long selectedDayStart = startOfDay(System.currentTimeMillis());

    // Dữ liệu của tháng đang hiển thị (để chọn ngày không phải truy vấn lại)
    private List<TransactionEntity> gridIncome = new ArrayList<>();
    private List<TransactionEntity> gridExpense = new ArrayList<>();
    private Map<Long, CategoryEntity> categoryMap = new HashMap<>();
    private int loadVersion = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        tvMonthIncome = view.findViewById(R.id.tvMonthIncome);
        tvMonthExpense = view.findViewById(R.id.tvMonthExpense);
        tvMonthTotal = view.findViewById(R.id.tvMonthTotal);
        tvSelectedDay = view.findViewById(R.id.tvSelectedDay);
        tvDayEmpty = view.findViewById(R.id.tvDayEmpty);
        rcvDayTransactions = view.findViewById(R.id.rcvDayTransactions);

        RecyclerView rcvCalendar = view.findViewById(R.id.rcvCalendar);
        rcvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        dayAdapter = new CalendarDayAdapter(requireContext(), cell -> {
            selectedDayStart = cell.dayStartMillis;
            dayAdapter.setSelected(selectedDayStart);
            // Bấm vào ngày của tháng khác -> chuyển sang tháng đó
            if (!cell.inMonth) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(cell.dayStartMillis);
                monthStart = firstOfMonth(c);
                loadData();
            } else {
                renderSelectedDay();
            }
        });
        rcvCalendar.setAdapter(dayAdapter);

        rcvDayTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionAdapter = new TransactionAdapter(requireContext(), this::showTransactionOptions);
        rcvDayTransactions.setAdapter(transactionAdapter);

        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> changeMonth(-1));
        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> changeMonth(1));
        view.findViewById(R.id.btnFixedExpenses).setOnClickListener(v ->
                startActivity(new Intent(requireContext(),
                        vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main.RecurringExpenseActivity.class)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void changeMonth(int delta) {
        monthStart.add(Calendar.MONTH, delta);
        // Chọn sẵn hôm nay nếu là tháng hiện tại, ngược lại chọn ngày 1
        Calendar today = Calendar.getInstance();
        if (monthStart.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && monthStart.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            selectedDayStart = startOfDay(System.currentTimeMillis());
        } else {
            selectedDayStart = monthStart.getTimeInMillis();
        }
        loadData();
    }

    private void loadData() {
        if (getContext() == null) return;
        final Context appContext = requireContext().getApplicationContext();
        final int version = ++loadVersion;
        final Calendar monthSnapshot = (Calendar) monthStart.clone();
        final long gridStart = gridStartOf(monthSnapshot).getTimeInMillis();
        final Calendar gridEndCal = gridStartOf(monthSnapshot);
        gridEndCal.add(Calendar.DAY_OF_MONTH, GRID_CELLS);
        final long gridEnd = gridEndCal.getTimeInMillis();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(appContext);
            SharedPreferences prefs = appContext.getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            UserEntity user = db.userDao().getUserByEmail(prefs.getString("USER_EMAIL", ""));

            List<TransactionEntity> income = new ArrayList<>();
            List<TransactionEntity> expense = new ArrayList<>();
            Map<Long, CategoryEntity> cats = new HashMap<>();

            if (user != null) {
                // Tự ghi các khoản chi phí cố định đã đến hạn trước khi vẽ lịch
                RecurringExpenseProcessor.generateDue(db, user.userId);
                income = db.transactionDao().getIncomeByDateRange(user.userId, gridStart, gridEnd);
                expense = db.transactionDao().getExpenseByDateRange(user.userId, gridStart, gridEnd);
                for (CategoryEntity c : db.categoryDao().getAllCategories()) cats.put(c.categoryId, c);
            }

            final List<TransactionEntity> fIncome = income, fExpense = expense;
            final Map<Long, CategoryEntity> fCats = cats;

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (!isAdded() || version != loadVersion) return;
                gridIncome = fIncome;
                gridExpense = fExpense;
                categoryMap = fCats;
                render(monthSnapshot);
            });
        }).start();
    }

    private void render(Calendar month) {
        final int curYear = month.get(Calendar.YEAR);
        final int curMonth = month.get(Calendar.MONTH);

        // Đánh dấu những ngày có thu / chi
        Set<Long> incomeDays = new HashSet<>();
        Set<Long> expenseDays = new HashSet<>();
        long monthIncome = 0, monthExpense = 0;
        Calendar tmp = Calendar.getInstance();

        for (TransactionEntity t : gridIncome) {
            incomeDays.add(startOfDay(t.transactionDate));
            tmp.setTimeInMillis(t.transactionDate);
            if (tmp.get(Calendar.YEAR) == curYear && tmp.get(Calendar.MONTH) == curMonth && t.amount != null) {
                monthIncome += t.amount.longValue();
            }
        }
        for (TransactionEntity t : gridExpense) {
            expenseDays.add(startOfDay(t.transactionDate));
            tmp.setTimeInMillis(t.transactionDate);
            if (tmp.get(Calendar.YEAR) == curYear && tmp.get(Calendar.MONTH) == curMonth && t.amount != null) {
                monthExpense += t.amount.longValue();
            }
        }

        // Dựng 42 ô của lưới
        List<CalendarDayAdapter.DayCell> cells = new ArrayList<>();
        Calendar c = gridStartOf(month);
        long todayStart = startOfDay(System.currentTimeMillis());
        for (int i = 0; i < GRID_CELLS; i++) {
            CalendarDayAdapter.DayCell cell = new CalendarDayAdapter.DayCell();
            cell.dayStartMillis = c.getTimeInMillis();
            cell.day = c.get(Calendar.DAY_OF_MONTH);
            cell.inMonth = c.get(Calendar.MONTH) == curMonth && c.get(Calendar.YEAR) == curYear;
            cell.isToday = cell.dayStartMillis == todayStart;
            cell.hasIncome = incomeDays.contains(cell.dayStartMillis);
            cell.hasExpense = expenseDays.contains(cell.dayStartMillis);
            cells.add(cell);
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Nếu ngày đang chọn không thuộc tháng này thì chọn ngày 1
        Calendar sel = Calendar.getInstance();
        sel.setTimeInMillis(selectedDayStart);
        if (sel.get(Calendar.YEAR) != curYear || sel.get(Calendar.MONTH) != curMonth) {
            selectedDayStart = month.getTimeInMillis();
        }
        dayAdapter.setData(cells, selectedDayStart);

        // Tiêu đề tháng: "11/2026 (01/11 - 30/11)"
        int lastDay = month.getActualMaximum(Calendar.DAY_OF_MONTH);
        tvMonthTitle.setText(String.format(Locale.getDefault(), "%02d/%d (01/%02d - %02d/%02d)",
                curMonth + 1, curYear, curMonth + 1, lastDay, curMonth + 1));

        // Tổng kết tháng
        long total = monthIncome - monthExpense;
        tvMonthIncome.setText(formatMoney(monthIncome));
        tvMonthExpense.setText(formatMoney(monthExpense));
        tvMonthTotal.setText((total >= 0 ? "+" : "-") + formatMoney(Math.abs(total)));
        tvMonthTotal.setTextColor(ContextCompat.getColor(requireContext(),
                total >= 0 ? R.color.primary : R.color.expense_red));

        renderSelectedDay();
    }

    private void renderSelectedDay() {
        long dayStart = selectedDayStart;
        long dayEnd = dayStart + 24L * 60 * 60 * 1000;

        List<TransactionEntity> list = new ArrayList<>();
        for (TransactionEntity t : gridIncome) {
            if (t.transactionDate >= dayStart && t.transactionDate < dayEnd) list.add(t);
        }
        for (TransactionEntity t : gridExpense) {
            if (t.transactionDate >= dayStart && t.transactionDate < dayEnd) list.add(t);
        }
        Collections.sort(list, (a, b) -> {
            int cmp = Long.compare(b.transactionDate, a.transactionDate);
            return cmp != 0 ? cmp : Long.compare(b.createdAt, a.createdAt);
        });

        String[] weekdays = {"Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"};
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dayStart);
        tvSelectedDay.setText(weekdays[c.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(dayStart)));

        transactionAdapter.setData(list, categoryMap);
        tvDayEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        rcvDayTransactions.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showTransactionOptions(TransactionEntity transaction) {
        if (getContext() == null) return;
        String[] options = {"✏️ Chỉnh sửa giao dịch", "🗑️ Xóa giao dịch"};
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_App_Dialog)
                .setTitle("Tùy chọn giao dịch")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
                        intent.putExtra("EDIT_TRANSACTION_ID", transaction.transactionID);
                        startActivity(intent);
                    } else {
                        confirmDelete(transaction);
                    }
                })
                .show();
    }

    private void confirmDelete(TransactionEntity transaction) {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_App_Dialog)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa", (d, w) -> new Thread(() -> {
                    AppDatabase.getDatabase(requireContext().getApplicationContext())
                            .transactionDao().deleteTransaction(transaction);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show();
                            loadData();
                        });
                    }
                }).start())
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ---------- Helpers ----------

    private static String formatMoney(long value) {
        return new DecimalFormat("#,###").format(value) + "đ";
    }

    private static Calendar firstOfMonth(Calendar any) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(any.get(Calendar.YEAR), any.get(Calendar.MONTH), 1, 0, 0, 0);
        return c;
    }

    /** Ô đầu tiên của lưới: thứ Hai của tuần chứa ngày 1. */
    private static Calendar gridStartOf(Calendar firstOfMonth) {
        Calendar c = (Calendar) firstOfMonth.clone();
        int offset = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // T2 = 0 ... CN = 6
        c.add(Calendar.DAY_OF_MONTH, -offset);
        return c;
    }

    private static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}