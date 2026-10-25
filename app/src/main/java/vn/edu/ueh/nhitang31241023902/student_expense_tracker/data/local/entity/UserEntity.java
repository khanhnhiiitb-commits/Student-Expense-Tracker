package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int userId;
    public String fullName;
    public String email;
    public String passwordHash;
    public long createdAt;
    public UserEntity(String fullName, String email, String password, long createdAt) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }
    public UserEntity(int userId, String fullName, String email, String passwordHash, long createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }
}
