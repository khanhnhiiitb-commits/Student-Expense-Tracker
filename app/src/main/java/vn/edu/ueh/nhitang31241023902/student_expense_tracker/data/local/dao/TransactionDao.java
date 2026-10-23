package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransaction(TransactionEntity transaction);

    @Update
    void updateTransaction(TransactionEntity transaction);

    @Delete
    void deleteTransaction(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE userID = :userId ORDER BY transactionDate DESC")
    List<TransactionEntity> getTransactionsByUser(long userId);
}
