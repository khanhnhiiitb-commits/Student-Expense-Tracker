package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local;


import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao.CategoryDao;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao.TransactionDao;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao.UserDao;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

@Database(entities = {UserEntity.class, CategoryEntity.class, TransactionEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract TransactionDao transactionDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "student_expense_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
