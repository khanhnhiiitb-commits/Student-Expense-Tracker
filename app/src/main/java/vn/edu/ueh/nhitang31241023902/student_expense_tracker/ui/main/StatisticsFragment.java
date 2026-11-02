package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StatisticsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(getContext());
        tv.setText("📊 Màn hình Thống kê đang phát triển");
        tv.setTextSize(18f);
        tv.setPadding(50, 50, 50, 50);
        return tv;
        // Sau này bạn thay bằng: return inflater.inflate(R.layout.fragment_statistics, container, false);
    }
}