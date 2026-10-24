package vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // 1. Kiểm tra họ tên không được để trống và tối thiểu 2 ký tự
    public static boolean isValidFullName(String fullName) {
        return fullName != null && fullName.trim().length() >= 2;
    }

    // 2. Kiểm tra định dạng Email
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // 3. Kiểm tra mật khẩu (Độ dài tối thiểu từ 6 ký tự trở lên)
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // 4. Kiểm tra mật khẩu xác nhận có khớp không
    public static boolean isPasswordMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}