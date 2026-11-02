package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter.TransactionAdapter;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.TransactionEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.transaction.AddTransactionActivity;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvTotalBalance;
    private RecyclerView rcvRecentTransactions;
    private TransactionAdapter transactionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();

        // Lấy tên người dùng từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
        tvWelcome.setText("Chào, " + prefs.getString("USER_NAME", "Bạn") + "!");

        return view;
    }

    private void initViews(View view) {
        // Chỉ giữ lại các ID có thật trong fragment_home.xml
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        rcvRecentTransactions = view.findViewById(R.id.rcvRecentTransactions);
    }

    private void setupRecyclerView() {
        if (getContext() != null) {
            rcvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

            transactionAdapter = new TransactionAdapter(requireContext(), transaction -> {
                showTransactionOptions(transaction);
            });
            rcvRecentTransactions.setAdapter(transactionAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void confirmAndDeleteTransaction(TransactionEntity transaction) {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
                        db.transactionDao().deleteTransaction(transaction);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show();
                                loadDashboardData();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showTransactionOptions(TransactionEntity transaction) {
        if (getContext() == null) return;
        String[] options = {"✏️ Chỉnh sửa giao dịch", "🗑️ Xóa giao dịch"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Tùy chọn giao dịch")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
                        intent.putExtra("EDIT_TRANSACTION_ID", transaction.transactionID);
                        startActivity(intent);
                    } else if (which == 1) {
                        confirmAndDeleteTransaction(transaction);
                    }
                })
                .show();
    }

    private void loadDashboardData() {
        if (getContext() == null) return;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
            SharedPreferences prefs = requireContext().getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE);
            String email = prefs.getString("USER_EMAIL", "");

            long currentUserId = -1;
            UserEntity user = db.userDao().getUserByEmail(email);
            if (user != null) {
                currentUserId = user.userId;
            }

            List<TransactionEntity> list;
            if (currentUserId != -1) {
                list = db.transactionDao().getTransactionsByUserId(currentUserId);
            } else {
                list = db.transactionDao().getAllTransactions();
            }

            BigDecimal total = BigDecimal.ZERO;
            if (list != null) {
                for (TransactionEntity tx : list) {
                    if (tx.amount == null) continue;
                    CategoryEntity cat = db.categoryDao().getCategoryById(tx.categoryID);
                    if (cat != null && "Income".equalsIgnoreCase(cat.type)) {
                        total = total.add(tx.amount);
                    } else {
                        total = total.subtract(tx.amount);
                    }
                }
            }

            final BigDecimal finalTotal = total;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    DecimalFormat df = new DecimalFormat("#,###");
                    tvTotalBalance.setText(df.format(finalTotal) + " VNĐ");
                    transactionAdapter.setData(list);
                });
            }
        }).start();
    }
}