package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class BudgetEntity {
    @PrimaryKey(autoGenerate = true)
    public long budgetId;

    public long userId;
    public long categoryID;
    public long amount;
    public int month;
    public int year;

    public BudgetEntity() {}

    public BudgetEntity(long userId, long categoryID, long amount, int month, int year) {
        this.userId = userId;
        this.categoryID = categoryID;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }
}