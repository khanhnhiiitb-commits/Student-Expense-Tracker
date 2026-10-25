package vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local;

import androidx.room.TypeConverter;
import java.math.BigDecimal;

public class Converters {

    @TypeConverter
    public static BigDecimal fromString(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    @TypeConverter
    public static String bigDecimalToString(BigDecimal bigDecimal) {
        return bigDecimal == null ? null : bigDecimal.toString();
    }
}