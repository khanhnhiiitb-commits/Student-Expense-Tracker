package vn.edu.ueh.nhitang31241023902.student_expense_tracker.test;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class TransactionTest {

    // Mô phỏng quy tắc kiểm tra số tiền trong AddTransactionActivity
    private boolean isValidAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return false;
        }

        try {
            BigDecimal value = new BigDecimal(amount);

            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            return amount.length() <= 12;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidCategory(long categoryId) {
        return categoryId > 0;
    }

    private boolean isValidTransactionType(String categoryType, String transactionType) {
        return categoryType != null && categoryType.equals(transactionType);
    }

    private boolean isValidNote(String note) {
        return note != null && note.trim().length() <= 200;
    }

    private boolean isValidDate(long dateMillis) {
        return dateMillis > 0;
    }

    // UT_TRANS_01: Số tiền hợp lệ
    @Test
    public void testAmount_Valid() {
        assertTrue(isValidAmount("50000"));
        assertTrue(isValidAmount("1"));
    }

    // UT_TRANS_02: Số tiền bằng 0
    @Test
    public void testAmount_Zero() {
        assertFalse(isValidAmount("0"));
    }

    // UT_TRANS_03: Số tiền âm
    @Test
    public void testAmount_Negative() {
        assertFalse(isValidAmount("-10000"));
    }

    // UT_TRANS_04: Số tiền rỗng
    @Test
    public void testAmount_Empty() {
        assertFalse(isValidAmount(""));
    }

    // UT_TRANS_05: Số tiền sai định dạng
    @Test
    public void testAmount_InvalidFormat() {
        assertFalse(isValidAmount("abc"));
    }

    // UT_TRANS_06: Số tiền vượt quá 12 ký tự
    @Test
    public void testAmount_OverLimit() {
        assertFalse(isValidAmount("1234567890123"));
    }

    // UT_TRANS_07: Danh mục hợp lệ
    @Test
    public void testCategory_Valid() {
        assertTrue(isValidCategory(1));
    }

    // UT_TRANS_08: Chưa chọn danh mục
    @Test
    public void testCategory_Invalid() {
        assertFalse(isValidCategory(-1));
    }

    // UT_TRANS_09: Loại giao dịch khớp danh mục
    @Test
    public void testTransactionType_Valid() {
        assertTrue(isValidTransactionType("Expense", "Expense"));
        assertTrue(isValidTransactionType("Income", "Income"));
    }

    // UT_TRANS_10: Loại giao dịch không khớp
    @Test
    public void testTransactionType_Invalid() {
        assertFalse(isValidTransactionType("Income", "Expense"));
    }

    // UT_TRANS_11: Ghi chú hợp lệ
    @Test
    public void testNote_Valid() {
        assertTrue(isValidNote("Mua sách"));
        assertTrue(isValidNote(""));
    }

    // UT_TRANS_12: Ghi chú vượt quá 200 ký tự
    @Test
    public void testNote_OverLimit() {
        String note = new String(new char[201]).replace('\0', 'A');
        assertFalse(isValidNote(note));
    }

    // UT_TRANS_13: Ngày giao dịch hợp lệ
    @Test
    public void testDate_Valid() {
        assertTrue(isValidDate(System.currentTimeMillis()));
    }

    // UT_TRANS_14: Ngày giao dịch không hợp lệ
    @Test
    public void testDate_Invalid() {
        assertFalse(isValidDate(0));
    }
}
