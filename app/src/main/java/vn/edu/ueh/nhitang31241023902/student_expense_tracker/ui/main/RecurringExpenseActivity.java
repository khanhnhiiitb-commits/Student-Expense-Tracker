package vn.edu.ueh.nhitang31241023902.student_expense_tracker.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.AppDatabase;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.CategoryEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.RecurringExpenseEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.data.local.entity.UserEntity;
import vn.edu.ueh.nhitang31241023902.student_expense_tracker.utils.RecurringExpenseProcessor;

/** Quản lý chi phí cố định hằng tháng: thêm / sửa / bật-tắt / xóa. */
public class RecurringExpenseActivity extends AppCompatActivity {

    private RecyclerView rcv;
    private TextView tvEmpty;
    private RecurringAdapter adapter;

    private long userId = -1;
    private List<CategoryEntity> expenseCategories = new ArrayList<>();
    private final Map<Long, CategoryEntity> categoryMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

        rcv = findViewById(R.id.rcvRecurring);
        tvEmpty = findViewById(R.id.tvRecurringEmpty);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddRecurring).setOnClickListener(v -> showEditDialog(null));

        adapter = new RecurringAdapter();
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        final Context appContext = getApplicationContext();
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(appContext);
            SharedPreferences prefs = getSharedPreferences("ExpenseAppPrefs", MODE_PRIVATE);
            UserEntity user = db.userDao().getUserByEmail(prefs.getString("USER_EMAIL", ""));
            if (user == null) {
                runOnUiThread(() -> Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show());
                return;
            }
            userId = user.userId;

            List<CategoryEntity> cats = db.categoryDao().getCategoriesByType("Expense", user.userId);
            Map<Long, CategoryEntity> map = new HashMap<>();
            for (CategoryEntity c : db.categoryDao().getAllCategories()) map.put(c.categoryId, c);
            List<RecurringExpenseEntity> items = db.recurringExpenseDao().getByUser(user.userId);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                expenseCategories = cats != null ? cats : new ArrayList<>();
                categoryMap.clear();
                categoryMap.putAll(map);
                adapter.setData(items);
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    /** item == null: thêm mới; ngược lại: chỉnh sửa. */
    private void showEditDialog(RecurringExpenseEntity item) {
        if (userId <= 0) return;
        if (expenseCategories.isEmpty()) {
            Toast.makeText(this, "Chưa có danh mục chi tiêu. Hãy tạo danh mục trước.", Toast.LENGTH_LONG).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_recurring_expense, null);
        EditText edtAmount = view.findViewById(R.id.edtRecAmount);
        EditText edtNote = view.findViewById(R.id.edtRecNote);
        Spinner spCategory = view.findViewById(R.id.spRecCategory);
        Spinner spDay = view.findViewById(R.id.spRecDay);

        List<String> catNames = new ArrayList<>();
        for (CategoryEntity c : expenseCategories) catNames.add(displayName(c));
        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, catNames));

        List<String> days = new ArrayList<>();
        for (int d = 1; d <= 31; d++) days.add("Ngày " + d);
        spDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days));

        if (item != null) {
            edtAmount.setText(item.amount != null ? item.amount.toPlainString() : "");
            edtNote.setText(item.note != null ? item.note : "");
            spDay.setSelection(Math.max(0, Math.min(30, item.dayOfMonth - 1)));
            for (int i = 0; i < expenseCategories.size(); i++) {
                if (expenseCategories.get(i).categoryId == item.categoryID) {
                    spCategory.setSelection(i);
                    break;
                }
            }
        } else {
            spDay.setSelection(java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) - 1);
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_App_Dialog)
                .setTitle(item == null ? "Thêm chi phí cố định" : "Sửa chi phí cố định")
                .setView(view)
                .setPositiveButton("Lưu", null) // gán listener bên dưới để không tự đóng khi nhập sai
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String raw = edtAmount.getText().toString().trim();
            BigDecimal amount;
            try {
                amount = new BigDecimal(raw);
            } catch (NumberFormatException e) {
                edtAmount.setError("Vui lòng nhập số tiền");
                return;
            }
            if (amount.signum() <= 0) {
                edtAmount.setError("Số tiền phải lớn hơn 0");
                return;
            }
            String note = edtNote.getText().toString().trim();
            CategoryEntity cat = expenseCategories.get(spCategory.getSelectedItemPosition());
            int day = spDay.getSelectedItemPosition() + 1;

            save(item, amount, note, cat.categoryId, day);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void save(RecurringExpenseEntity existing, BigDecimal amount, String note, long categoryId, int day) {
        final Context appContext = getApplicationContext();
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(appContext);
            if (existing == null) {
                RecurringExpenseEntity e = new RecurringExpenseEntity(
                        userId, categoryId, amount, note, day,
                        System.currentTimeMillis(), System.currentTimeMillis());
                db.recurringExpenseDao().insert(e);
            } else {
                existing.amount = amount;
                existing.note = note;
                existing.categoryID = categoryId;
                existing.dayOfMonth = day;
                db.recurringExpenseDao().update(existing);
            }
            // Nếu ngày đã đặt đã tới trong tháng này thì ghi luôn
            RecurringExpenseProcessor.generateDue(db, userId);
            runOnUiThread(this::loadData);
        }).start();
    }

    private void confirmDelete(RecurringExpenseEntity item) {
        new AlertDialog.Builder(this, R.style.ThemeOverlay_App_Dialog)
                .setTitle("Xóa chi phí cố định")
                .setMessage("Các giao dịch đã được ghi trước đó vẫn được giữ lại. Bạn muốn xóa khoản này?")
                .setPositiveButton("Xóa", (d, w) -> new Thread(() -> {
                    AppDatabase.getDatabase(getApplicationContext()).recurringExpenseDao().delete(item);
                    runOnUiThread(this::loadData);
                }).start())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setActive(RecurringExpenseEntity item, boolean active) {
        item.isActive = active;
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            if (active) {
                // Bật lại: bỏ qua các kỳ đã trôi qua trong lúc tạm dừng, chỉ tính từ hôm nay
                item.startDate = System.currentTimeMillis();
            }
            db.recurringExpenseDao().update(item);
            if (active) RecurringExpenseProcessor.generateDue(db, userId);
        }).start();
    }

    /** Danh mục người dùng tự tạo lưu dạng "icon #màu Tên" -> chỉ hiện "icon Tên". */
    private static String displayName(CategoryEntity c) {
        String full = c.categoryName != null ? c.categoryName : "";
        if (c.userId != null && c.userId > 0) {
            String[] parts = full.split(" ", 3);
            if (parts.length == 3 && parts[1].startsWith("#")) return parts[0] + " " + parts[2];
        }
        return full;
    }

    private class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.VH> {
        private final List<RecurringExpenseEntity> data = new ArrayList<>();

        void setData(List<RecurringExpenseEntity> items) {
            data.clear();
            data.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recurring_expense, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            RecurringExpenseEntity item = data.get(position);
            CategoryEntity cat = categoryMap.get(item.categoryID);
            String catName = cat != null ? displayName(cat) : "Danh mục đã xóa";

            h.tvTitle.setText(item.note != null && !item.note.isEmpty() ? item.note : catName);
            h.tvSub.setText("Ngày " + item.dayOfMonth + " hằng tháng · " + catName);
            h.tvAmount.setText(new DecimalFormat("#,###").format(item.amount) + "đ");

            h.sw.setOnCheckedChangeListener(null);
            h.sw.setChecked(item.isActive);
            h.sw.setOnCheckedChangeListener((b, checked) -> setActive(item, checked));

            h.itemView.setAlpha(item.isActive ? 1f : 0.5f);
            h.itemView.setOnClickListener(v -> showEditDialog(item));
            h.btnDelete.setOnClickListener(v -> confirmDelete(item));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView tvTitle, tvSub, tvAmount, btnDelete;
            final SwitchCompat sw;

            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvRecTitle);
                tvSub = v.findViewById(R.id.tvRecSub);
                tvAmount = v.findViewById(R.id.tvRecAmount);
                btnDelete = v.findViewById(R.id.btnRecDelete);
                sw = v.findViewById(R.id.swRecActive);
            }
        }
    }
}