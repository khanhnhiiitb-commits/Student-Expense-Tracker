package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long categoryId;
    public Long userId; // NULL nếu là danh mục mặc định của hệ thống
    public String categoryName;
    public String type; // "Income" hoặc "Expense"

    public CategoryEntity(Long userId, String categoryName, String type) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.type = type;
    }
}
