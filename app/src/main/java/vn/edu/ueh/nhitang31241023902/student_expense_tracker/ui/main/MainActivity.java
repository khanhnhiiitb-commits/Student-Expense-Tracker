package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RoomTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gọi hàm kiểm thử đọc/ghi Room Database
        testRoomDatabaseOperations();
    }

    private void testRoomDatabaseOperations() {
        // Khởi tạo Database Instance
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

        // Chạy trên luồng nền (Background Thread) để thao tác với Room
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Tạo dữ liệu mẫu User để ghi (Ghi / Insert)
                    UserEntity newUser = new UserEntity(
                            "Nguyen Van A",
                            "sinhvien@ueh.edu.vn",
                            "hashed_password_123",
                            System.currentTimeMillis()
                    );

                    long userId = db.userDao().insertUser(newUser);
                    Log.d(TAG, "Ghi thành công User với ID: " + userId);

                    // 2. Kiểm tra đọc dữ liệu từ Database (Đọc / Query)
                    UserEntity fetchedUser = db.userDao().getUserByEmail("sinhvien@ueh.edu.vn");
                    if (fetchedUser != null) {
                        Log.d(TAG, "Đọc thành công - Tên: " + fetchedUser.fullName + " | Email: " + fetchedUser.email);
                    } else {
                        Log.d(TAG, "Không tìm thấy dữ liệu user vừa ghi.");
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Lỗi kiểm thử Room Database: " + e.getMessage());
                }
            }
        }).start();
    }
}