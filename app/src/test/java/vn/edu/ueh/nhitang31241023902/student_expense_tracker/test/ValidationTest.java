package vn.edu.ueh.nhitang31241023902.student_expense_tracker.test;

import org.junit.Test;
import static org.junit.Assert.*;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.ValidationUtils;

public class ValidationTest {

    // UT_ACC_01: Email hợp lệ
    @Test
    public void testEmailValidation_Valid() {
        assertTrue(ValidationUtils.isValidEmail("sinhvien@ueh.edu.vn"));
        assertTrue(ValidationUtils.isValidEmail("student123@gmail.com"));
    }

    // UT_ACC_02: Email không hợp lệ
    @Test
    public void testEmailValidation_Invalid() {
        assertFalse(ValidationUtils.isValidEmail("sinhvien.ueh.edu.vn"));
        assertFalse(ValidationUtils.isValidEmail("sinhvien@"));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    // UT_ACC_03: Mật khẩu hợp lệ
    @Test
    public void testPasswordValidation_Valid() {
        assertTrue(ValidationUtils.isValidPassword("Password123"));
        assertTrue(ValidationUtils.isValidPassword("123456"));
    }

    // UT_ACC_04: Mật khẩu không hợp lệ
    @Test
    public void testPasswordValidation_Invalid() {
        assertFalse(ValidationUtils.isValidPassword("12345"));
        assertFalse(ValidationUtils.isValidPassword(""));
        assertFalse(ValidationUtils.isValidPassword(null));
    }

    // UT_ACC_05: Họ tên hợp lệ
    @Test
    public void testFullNameValidation_Valid() {
        assertTrue(ValidationUtils.isValidFullName("Nguyen Van A"));
        assertTrue(ValidationUtils.isValidFullName("An"));
    }

    // UT_ACC_06: Họ tên không hợp lệ
    @Test
    public void testFullNameValidation_Invalid() {
        assertFalse(ValidationUtils.isValidFullName("A"));
        assertFalse(ValidationUtils.isValidFullName(""));
        assertFalse(ValidationUtils.isValidFullName("   "));
        assertFalse(ValidationUtils.isValidFullName(null));
    }

    // UT_ACC_07: Mật khẩu xác nhận trùng khớp
    @Test
    public void testPasswordMatch_Valid() {
        assertTrue(ValidationUtils.isPasswordMatch("123456", "123456"));
        assertTrue(ValidationUtils.isPasswordMatch("Password123", "Password123"));
    }

    // UT_ACC_08: Mật khẩu xác nhận không trùng khớp
    @Test
    public void testPasswordMatch_Invalid() {
        assertFalse(ValidationUtils.isPasswordMatch("123456", "654321"));
        assertFalse(ValidationUtils.isPasswordMatch("Password123", "password123"));
        assertFalse(ValidationUtils.isPasswordMatch(null, "123456"));
    }
}
