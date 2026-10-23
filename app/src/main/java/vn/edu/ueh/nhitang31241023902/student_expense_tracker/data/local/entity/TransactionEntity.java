package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.math.BigDecimal;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    public long transactionID;
    public long userID;
    public long categoryID;
    public BigDecimal amount;
    public String note;
    public long transactionDate;
    public long createdAt;

    public TransactionEntity(long userID, long categoryID, BigDecimal amount, String note, long transactionDate, long createdAt) {
        this.userID = userID;
        this.categoryID = categoryID;
        this.amount = amount;
        this.note = note;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
    }
}
