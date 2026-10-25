package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao.UserDao;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao.CategoryDao;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;

@Database(entities = {UserEntity.class, CategoryEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "student_expense_db")
                            .fallbackToDestructiveMigration() // Hỗ trợ cập nhật cấu trúc database
                            .addCallback(sRoomDatabaseCallback) // Gắn Callback khởi tạo dữ liệu
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Callback kích hoạt khi Database được khởi tạo lần đầu tiên
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Chạy luồng ngầm để ghi dữ liệu, không làm đơ giao diện
            new Thread(() -> {
                CategoryDao dao = INSTANCE.categoryDao();

                // Nạp danh mục Thu nhập (Income)
                dao.insertCategory(new CategoryEntity(null, "Lương", "Income"));
                dao.insertCategory(new CategoryEntity(null, "Làm thêm", "Income"));
                dao.insertCategory(new CategoryEntity(null, "Học bổng", "Income"));
                dao.insertCategory(new CategoryEntity(null, "Tiền gia đình hỗ trợ", "Income"));
                dao.insertCategory(new CategoryEntity(null, "Tiền thưởng", "Income"));
                dao.insertCategory(new CategoryEntity(null, "Thu nhập khác", "Income"));

                // Nạp danh mục Chi tiêu (Expense)
                dao.insertCategory(new CategoryEntity(null, "Ăn uống", "Expense"));
                dao.insertCategory(new CategoryEntity(null, "Học tập", "Expense"));
                dao.insertCategory(new CategoryEntity(null, "Đi lại", "Expense"));
                dao.insertCategory(new CategoryEntity(null, "Nhà ở", "Expense"));
                dao.insertCategory(new CategoryEntity(null, "Mua sắm", "Expense"));
                dao.insertCategory(new CategoryEntity(null, "Giải trí", "Expense"));
                dao.insertCategory(new CategoryEntity(null, "Sức khỏe", "Expense"));
                dao.insertCategory(new CategoryEntity(null, "Hóa đơn và dịch vụ", "Expense"));
                dao.insertCategory(new CategoryEntity(null, "Chi tiêu khác", "Expense"));
            }).start();
        }
    };
}