package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.BudgetEntity;

@Dao
public interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBudget(BudgetEntity budget);

    @Update
    void updateBudget(BudgetEntity budget);

    @Delete
    void deleteBudget(BudgetEntity budget);

    // Lấy tất cả ngân sách của một người dùng trong một tháng cụ thể
    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    List<BudgetEntity> getAllBudgetsForMonth(long userId, int month, int year);

    // Lấy ngân sách của một danh mục cụ thể trong một tháng (Dùng cho kiểm tra trùng lặp khi Setup)
    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryID = :categoryId AND month = :month AND year = :year LIMIT 1")
    BudgetEntity getBudgetForCategoryAndMonth(long userId, long categoryId, int month, int year);
}