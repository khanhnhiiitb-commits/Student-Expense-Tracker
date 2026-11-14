
package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;

@Dao
public interface TransactionDao {

    @Insert
    long insertTransaction(TransactionEntity transaction);

    @Delete
    int deleteTransaction(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE userID = :userId ORDER BY transactionDate DESC, createdAt DESC")
    List<TransactionEntity> getTransactionsByUserId(long userId);

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, createdAt DESC")
    List<TransactionEntity> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE transactionID = :id LIMIT 1")
    TransactionEntity getTransactionById(long id);

    // Lấy thu nhập theo khoảng thời gian
    @Query("SELECT t.* FROM transactions AS t " +
            "INNER JOIN categories AS c ON c.categoryId = t.categoryID " +
            "WHERE t.userID = :userId AND c.type = 'Income' " +
            "AND t.transactionDate >= :startMillis " +
            "AND t.transactionDate < :endExclusiveMillis " +
            "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<TransactionEntity> getIncomeByDateRange(
            long userId,
            long startMillis,
            long endExclusiveMillis
    );

    // Lấy chi tiêu theo khoảng thời gian
    @Query("SELECT t.* FROM transactions AS t " +
            "INNER JOIN categories AS c ON c.categoryId = t.categoryID " +
            "WHERE t.userID = :userId AND c.type = 'Expense' " +
            "AND t.transactionDate >= :startMillis " +
            "AND t.transactionDate < :endExclusiveMillis " +
            "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<TransactionEntity> getExpenseByDateRange(
            long userId,
            long startMillis,
            long endExclusiveMillis
    );

    // Lấy tất cả giao dịch trong khoảng thời gian
    @Query("SELECT * FROM transactions " +
            "WHERE userID = :userId " +
            "AND transactionDate >= :startMillis " +
            "AND transactionDate < :endExclusiveMillis " +
            "ORDER BY transactionDate DESC, createdAt DESC")
    List<TransactionEntity> getTransactionsByDateRange(
            long userId,
            long startMillis,
            long endExclusiveMillis
    );

    @Update
    int updateTransaction(TransactionEntity transaction);
}
