
package vn.edu.ueh.nhitang31241023902.student_expense_tracker;

import android.app.Application;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.NotificationHelper;

public class ExpenseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper.createNotificationChannel(this);
    }
}
