package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    public int userId;

    public String fullName;
    public String email;
    public String passwordHash;
    public long createdAt;

    // Phân quyền: "Student" (mặc định) hoặc "Admin"
    public String role = ROLE_STUDENT;

    // false = tài khoản bị quản trị viên khóa (UC14)
    public boolean isActive = true;

    public static final String ROLE_STUDENT = "Student";
    public static final String ROLE_ADMIN = "Admin";

    // Constructor dùng để Room đọc dữ liệu từ SQLite
    public UserEntity(
            int userId,
            String fullName,
            String email,
            String passwordHash,
            long createdAt
    ) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Constructor dùng khi đăng ký tài khoản mới
    @Ignore
    public UserEntity(
            String fullName,
            String email,
            String passwordHash,
            long createdAt
    ) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }
}