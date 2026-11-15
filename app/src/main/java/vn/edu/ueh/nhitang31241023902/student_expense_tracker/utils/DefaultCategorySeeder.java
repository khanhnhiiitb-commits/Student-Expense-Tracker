package vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;

/** Shared system categories. Run on a background thread. */
public final class DefaultCategorySeeder {
    private DefaultCategorySeeder() {}

    public static void ensureDefaults(AppDatabase db) {
        db.runInTransaction(() -> {
            addMissing(db, "Expense", new String[]{
                    "Ăn uống", "Học tập", "Đi lại", "Nhà ở", "Mua sắm",
                    "Giải trí", "Sức khỏe", "Hóa đơn và dịch vụ", "Chi tiêu khác"
            });
            addMissing(db, "Income", new String[]{
                    "Lương / Làm thêm", "Trợ cấp gia đình", "Học bổng", "Thu nhập khác"
            });
        });
    }

    private static void addMissing(AppDatabase db, String type, String[] names) {
        for (String name : names) {
            if (db.categoryDao().countSystemCategoryByName(name, type) == 0) {
                db.categoryDao().insertCategory(new CategoryEntity(null, name, type));
            }
        }
    }
}
