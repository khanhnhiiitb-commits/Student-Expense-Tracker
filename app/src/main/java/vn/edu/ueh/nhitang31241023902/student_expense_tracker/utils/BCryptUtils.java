package vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils;


import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtils {

    // Hàm băm mật khẩu (Mật khẩu thuần -> Chuỗi mã hóa BCrypt)
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Hàm kiểm tra mật khẩu khi đăng nhập (Mật khẩu nhập vào vs Mật khẩu đã băm trong CSDL)
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
