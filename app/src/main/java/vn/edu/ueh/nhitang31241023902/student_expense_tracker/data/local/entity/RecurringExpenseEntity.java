package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;

/**
 * Chi phí cố định hằng tháng (tiền nhà, wifi, học phí...).
 * Đến ngày {@link #dayOfMonth} mỗi tháng, app tự tạo một giao dịch Chi tiêu tương ứng.
 */
@Entity(
        tableName = "recurring_expenses",
        indices = {@Index("userID")}
)
public class RecurringExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    public long recurringID;
    public long userID;
    public long categoryID;
    public BigDecimal amount;
    public String note;
    /** Ngày trong tháng (1-31). Tháng ngắn hơn sẽ lấy ngày cuối tháng. */
    public int dayOfMonth;
    /** Mốc bắt đầu áp dụng (millis). Các kỳ trước mốc này không được tạo. */
    public long startDate;
    /** Tháng gần nhất đã xử lý, dạng yyyyMM (0 = chưa xử lý kỳ nào). Dùng để không tạo trùng. */
    public int lastGeneratedMonth;
    public boolean isActive = true;
    public long createdAt;

    public RecurringExpenseEntity(long userID, long categoryID, BigDecimal amount, String note,
                                  int dayOfMonth, long startDate, long createdAt) {
        this.userID = userID;
        this.categoryID = categoryID;
        this.amount = amount;
        this.note = note;
        this.dayOfMonth = dayOfMonth;
        this.startDate = startDate;
        this.createdAt = createdAt;
    }
}