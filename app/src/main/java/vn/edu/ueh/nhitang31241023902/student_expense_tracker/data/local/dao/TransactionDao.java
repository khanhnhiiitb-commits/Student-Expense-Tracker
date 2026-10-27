package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;

@Dao
public interface TransactionDao {
    @Insert
    long insertTransaction(TransactionEntity transaction);
    @androidx.room.Delete
    int deleteTransaction(TransactionEntity transaction);
    @Query("SELECT * FROM transactions WHERE userID = :userId ORDER BY transactionDate DESC, createdAt DESC")
    List<TransactionEntity> getTransactionsByUserId(long userId);

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, createdAt DESC")
    List<TransactionEntity> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE transactionId = :id LIMIT 1")
    TransactionEntity getTransactionById(long id);

    @Update
    int updateTransaction(TransactionEntity transaction);
}
