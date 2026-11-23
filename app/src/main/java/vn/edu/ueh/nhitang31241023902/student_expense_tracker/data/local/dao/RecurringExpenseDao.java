package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.RecurringExpenseEntity;

@Dao
public interface RecurringExpenseDao {

    @Insert
    long insert(RecurringExpenseEntity item);

    @Update
    int update(RecurringExpenseEntity item);

    @Delete
    int delete(RecurringExpenseEntity item);

    @Query("SELECT * FROM recurring_expenses WHERE userID = :userId ORDER BY dayOfMonth ASC, recurringID ASC")
    List<RecurringExpenseEntity> getByUser(long userId);

    @Query("SELECT * FROM recurring_expenses WHERE userID = :userId AND isActive = 1")
    List<RecurringExpenseEntity> getActiveByUser(long userId);
}