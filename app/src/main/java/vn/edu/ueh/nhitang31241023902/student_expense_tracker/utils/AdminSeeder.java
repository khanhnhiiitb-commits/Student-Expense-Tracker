package vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

/** Tạo sẵn 1 tài khoản quản trị viên mặc định nếu chưa có. Gọi trên background thread. */
public class AdminSeeder {

    public static final String ADMIN_EMAIL = "admin@ueh.edu.vn";
    public static final String ADMIN_PASSWORD = "Admin@123";

    public static void ensureDefaultAdmin(AppDatabase db) {
        if (db.userDao().getUserByEmail(ADMIN_EMAIL) != null) return;

        UserEntity admin = new UserEntity(
                "Quản trị viên",
                ADMIN_EMAIL,
                BCryptUtils.hashPassword(ADMIN_PASSWORD),
                System.currentTimeMillis()
        );
        admin.role = UserEntity.ROLE_ADMIN;
        db.userDao().insertUser(admin);
    }
}