package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);

        // Lấy thông tin user từ SharedPreferences hiển thị lên màn hình
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String name = prefs.getString("USER_NAME", "Người dùng");
            String email = prefs.getString("USER_EMAIL", "Chưa có email");

            tvProfileName.setText(name);
            tvProfileEmail.setText(email);
        }

        // Xử lý sự kiện bấm Đăng xuất
        btnLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
                // 1. Xóa toàn bộ dữ liệu phiên đăng nhập hiện tại
                SharedPreferences prefs = getActivity().getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();

                // 2. Chuyển về màn hình Login và xóa cờ lịch sử (không cho bấm Back quay lại App)
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }
}