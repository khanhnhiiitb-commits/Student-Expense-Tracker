
package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;

public class ReportFragment extends Fragment {

    public ReportFragment() {
        super(R.layout.fragment_report);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        TextView tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        TextView tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        TextView tvNetResult = view.findViewById(R.id.tvNetResult);

        // Giá trị mặc định trước khi tải dữ liệu
        tvTotalIncome.setText("0 đ");
        tvTotalExpense.setText("0 đ");
        tvNetResult.setText("Thu - Chi: 0 đ");

        btnBack.setOnClickListener(v -> {
            // Chuyển về tab Trang chủ
            com.google.android.material.bottomnavigation.BottomNavigationView
                    bottomNav = requireActivity()
                    .findViewById(R.id.bottom_navigation);

            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });
    }
}
