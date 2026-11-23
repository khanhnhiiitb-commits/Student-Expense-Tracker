package vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils;

import java.util.Calendar;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.RecurringExpenseEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;

/**
 * Tự tạo giao dịch Chi tiêu cho các khoản chi phí cố định đã đến hạn.
 * Gọi trên background thread. An toàn khi gọi nhiều lần (không tạo trùng).
 */
public final class RecurringExpenseProcessor {

    private RecurringExpenseProcessor() {}

    /** @return số giao dịch vừa được tạo */
    public static synchronized int generateDue(AppDatabase db, long userId) {
        final long now = System.currentTimeMillis();
        final int[] created = {0};

        List<RecurringExpenseEntity> items = db.recurringExpenseDao().getActiveByUser(userId);
        for (RecurringExpenseEntity item : items) {
            if (item.amount == null || item.amount.signum() <= 0) continue;
            CategoryEntity cat = db.categoryDao().getCategoryById(item.categoryID);
            if (cat == null || !"Expense".equals(cat.type)) continue; // danh mục đã bị xóa/đổi loại

            db.runInTransaction(() -> created[0] += processItem(db, item, now));
        }
        return created[0];
    }

    private static int processItem(AppDatabase db, RecurringExpenseEntity item, long now) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(item.startDate);
        long startOfStartDay = startOfDay(start);

        Calendar cur = Calendar.getInstance();
        cur.setTimeInMillis(now);
        int curIndex = cur.get(Calendar.YEAR) * 12 + cur.get(Calendar.MONTH);

        int y = start.get(Calendar.YEAR);
        int m = start.get(Calendar.MONTH);
        int count = 0;
        boolean changed = false;

        // Giới hạn 240 tháng để tránh vòng lặp quá dài nếu dữ liệu bất thường
        for (int guard = 0; guard < 240 && y * 12 + m <= curIndex; guard++) {
            int ym = y * 100 + (m + 1);
            if (ym > item.lastGeneratedMonth) {
                Calendar due = Calendar.getInstance();
                due.clear();
                due.set(y, m, 1, 12, 0, 0);
                int dim = due.getActualMaximum(Calendar.DAY_OF_MONTH);
                due.set(Calendar.DAY_OF_MONTH, Math.min(item.dayOfMonth, dim));

                if (due.getTimeInMillis() > now) break; // chưa đến hạn

                if (due.getTimeInMillis() >= startOfStartDay) {
                    String note = (item.note == null || item.note.trim().isEmpty())
                            ? "Chi phí cố định" : item.note;
                    db.transactionDao().insertTransaction(new TransactionEntity(
                            item.userID, item.categoryID, item.amount, note,
                            due.getTimeInMillis(), System.currentTimeMillis()));
                    count++;
                }
                item.lastGeneratedMonth = ym;
                changed = true;
            }
            m++;
            if (m > 11) { m = 0; y++; }
        }

        if (changed) db.recurringExpenseDao().update(item);
        return count;
    }

    private static long startOfDay(Calendar c) {
        Calendar d = (Calendar) c.clone();
        d.set(Calendar.HOUR_OF_DAY, 0);
        d.set(Calendar.MINUTE, 0);
        d.set(Calendar.SECOND, 0);
        d.set(Calendar.MILLISECOND, 0);
        return d.getTimeInMillis();
    }
}