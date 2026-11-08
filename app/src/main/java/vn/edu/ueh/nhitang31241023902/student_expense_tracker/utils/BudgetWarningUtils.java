
package vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils;

public class BudgetWarningUtils {

    public enum WarningLevel {
        NOT_SET,
        NORMAL,
        WARNING,
        DANGER,
        EXHAUSTED,
        EXCEEDED
    }

    public static WarningLevel checkBudget(
            long budget, long spent) {

        if (budget <= 0) {
            return WarningLevel.NOT_SET;
        }

        // So sánh trực tiếp để tránh sai số phần trăm
        if (spent > budget) {
            return WarningLevel.EXCEEDED;
        }

        if (spent == budget) {
            return WarningLevel.EXHAUSTED;
        }

        double percentage = (double) spent / budget * 100;

        if (percentage >= 90) {
            return WarningLevel.DANGER;
        }

        if (percentage >= 70) {
            return WarningLevel.WARNING;
        }

        return WarningLevel.NORMAL;
    }

    public static String getMessage(WarningLevel level) {
        switch (level) {
            case NOT_SET:
                return "Bạn chưa thiết lập ngân sách.";

            case WARNING:
                return "Bạn đã sử dụng ít nhất 70% ngân sách.";

            case DANGER:
                return "Bạn sắp sử dụng hết ngân sách!";

            case EXHAUSTED:
                return "Bạn đã sử dụng hết ngân sách!";

            case EXCEEDED:
                return "Cảnh báo! Bạn đã vượt ngân sách!";

            default:
                return "Chi tiêu đang trong mức an toàn.";
        }
    }

    public static int getColor(WarningLevel level) {
        switch (level) {
            case WARNING:
                return 0xFFFFA000; // Vàng cam

            case DANGER:
                return 0xFFFF6D00; // Cam đậm

            case EXHAUSTED:
            case EXCEEDED:
                return 0xFFD32F2F; // Đỏ

            case NOT_SET:
                return 0xFF757575; // Xám

            default:
                return 0xFF388E3C; // Xanh
        }
    }
}
