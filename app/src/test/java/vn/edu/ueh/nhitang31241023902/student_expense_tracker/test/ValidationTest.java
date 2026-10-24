package vn.edu.ueh.nhitang31241023902.student_expense_tracker.test;

import org.junit.Test;
import static org.junit.Assert.*;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.ValidationUtils;

public class ValidationTest {

    @Test
    public void testEmailValidation_Valid() {
        assertTrue(ValidationUtils.isValidEmail("sinhvien@ueh.edu.vn"));
    }

    @Test
    public void testEmailValidation_Invalid() {
        assertFalse(ValidationUtils.isValidEmail("sinhvien.ueh.edu.vn")); // Thiếu dấu @
        assertFalse(ValidationUtils.isValidEmail("")); // Để trống
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(ValidationUtils.isValidPassword("Password123")); // Đủ >= 6 ký tự
        assertFalse(ValidationUtils.isValidPassword("12345")); // Quá ngắn (< 6 ký tự)
    }

    @Test
    public void testFullNameValidation() {
        assertTrue(ValidationUtils.isValidFullName("Nguyen Van A"));
        assertFalse(ValidationUtils.isValidFullName("A")); // Quá ngắn
    }
}
