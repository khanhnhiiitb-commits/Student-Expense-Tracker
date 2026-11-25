package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE role = 'Student' ORDER BY createdAt DESC")
    List<UserEntity> getAllStudents();

    @Query("UPDATE users SET isActive = :active WHERE userId = :userId AND role = 'Student'")
    int updateActive(int userId, boolean active);

    @Query("UPDATE users SET fullName = :fullName WHERE LOWER(email) = LOWER(:email)")
    int updateFullName(String email, String fullName);

    @Query("SELECT COUNT(*) FROM users WHERE role = 'Student'")
    int countStudents();

    // Đặt lại mật khẩu (Quên mật khẩu)
    @Query("UPDATE users SET passwordHash = :passwordHash WHERE LOWER(email) = LOWER(:email)")
    int updatePassword(String email, String passwordHash);
}