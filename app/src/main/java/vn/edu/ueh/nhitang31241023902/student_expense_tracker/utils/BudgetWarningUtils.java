package vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils;

import static vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.BudgetWarningUtils.WarningLevel.WARNING;

public final class BudgetWarningUtils {
    private BudgetWarningUtils() {}

    public enum WarningLevel { NOT_SET, NORMAL, WARNING, DANGER, EXHAUSTED, EXCEEDED }

    public static WarningLevel checkBudget(long budget, long spent) {
        if (budget <= 0) return WarningLevel.NOT_SET;
        if (spent > budget) return WarningLevel.EXCEEDED;
        if (spent == budget) return WarningLevel.EXHAUSTED;
        double ratio = (double) spent / (double) budget;
        if (ratio >= 0.90) return WarningLevel.DANGER;
        if (ratio >= 0.70) return WARNING;
        return WarningLevel.NORMAL;
    }

    public static String getMessage(WarningLevel level) {
        switch (level) {
            case NOT_SET: return "Bạn chưa thiết lập ngân sách.";
            case WARNING: return "Bạn đã sử dụng ít nhất 70% ngân sách.";
            case DANGER: return "Bạn đã sử dụng ít nhất 90% ngân sách!";
            case EXHAUSTED: return "Bạn đã sử dụng hết ngân sách!";
            case EXCEEDED: return "Bạn đã vượt ngân sách!";
            default: return "Chi tiêu đang trong mức an toàn.";
        }
    }

    public static int getColor(WarningLevel level) {
        switch (level) {
            case WARNING: return 0xFFF59E0B;
            case DANGER: return 0xFFF97316;
            case EXHAUSTED:
            case EXCEEDED: return 0xFFF43F5E;
            case NOT_SET: return 0xFF94A3B8;
            default: return 0xFF10B981;
        }
    }

    public static boolean shouldAlert(WarningLevel before, WarningLevel after) {
        return after.ordinal() >= WARNING.ordinal() && after.ordinal() > before.ordinal();
    }
}