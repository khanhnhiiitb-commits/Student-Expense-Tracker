
package vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "budget_alert_channel";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cảnh báo ngân sách",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(
                    "Thông báo khi chi tiêu đạt hoặc vượt ngưỡng ngân sách"
            );

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(
                            Context.NOTIFICATION_SERVICE
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showBudgetNotification(
            Context context,
            String message
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Cảnh báo ngân sách")
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context)
                    .notify(
                            (int) (System.currentTimeMillis()
                                    & 0x7FFFFFFF),
                            builder.build()
                    );
        } catch (SecurityException e) {
            android.util.Log.e(
                    "NotificationHelper",
                    "Không có quyền gửi thông báo",
                    e
            );
        }
    }
}
