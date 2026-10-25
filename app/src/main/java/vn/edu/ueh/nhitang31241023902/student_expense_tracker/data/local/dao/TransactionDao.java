package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;

@Dao
public interface TransactionDao {

    @Insert
    long insertTransaction(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE userID = :userId ORDER BY transactionDate DESC")
    List<TransactionEntity> getTransactionsByUserId(long userId);

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    List<TransactionEntity> getAllTransactions();
}