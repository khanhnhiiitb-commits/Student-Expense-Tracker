package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import java.math.BigDecimal;

/** Kết quả thống kê chi tiêu của một danh mục (6.1.4). */
public class CategoryStat {
    public final long categoryId;
    public final String categoryName;
    public final BigDecimal totalAmount;
    public final double percent; // 0..100, tính trên tổng chi
    public final int color;

    public CategoryStat(long categoryId, String categoryName, BigDecimal totalAmount, double percent, int color) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.percent = percent;
        this.color = color;
    }
}
