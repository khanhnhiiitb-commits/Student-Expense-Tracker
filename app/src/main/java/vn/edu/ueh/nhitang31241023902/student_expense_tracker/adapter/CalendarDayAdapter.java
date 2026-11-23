package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;

/** Lưới 7 cột (T2 → CN) hiển thị các ngày trong tháng, kèm chấm thu/chi. */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public static class DayCell {
        public long dayStartMillis;
        public int day;
        public boolean inMonth;
        public boolean isToday;
        public boolean hasIncome;
        public boolean hasExpense;
    }

    public interface OnDayClickListener {
        void onDayClick(DayCell cell);
    }

    private final Context context;
    private final OnDayClickListener listener;
    private final List<DayCell> cells = new ArrayList<>();
    private long selectedDayStart = -1;

    public CalendarDayAdapter(Context context, OnDayClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<DayCell> newCells, long selectedDayStart) {
        cells.clear();
        cells.addAll(newCells);
        this.selectedDayStart = selectedDayStart;
        notifyDataSetChanged();
    }

    public void setSelected(long dayStartMillis) {
        this.selectedDayStart = dayStartMillis;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder h, int position) {
        DayCell cell = cells.get(position);
        h.tvDay.setText(String.valueOf(cell.day));

        int col = position % 7; // 0 = T2 ... 5 = T7, 6 = CN
        int color;
        if (!cell.inMonth) {
            color = R.color.text_hint;
        } else if (col == 6) {
            color = R.color.expense_red;
        } else if (col == 5) {
            color = R.color.primary;
        } else {
            color = R.color.text_main;
        }
        h.tvDay.setTextColor(ContextCompat.getColor(context, color));
        h.tvDay.setTypeface(null, cell.isToday ? Typeface.BOLD : Typeface.NORMAL);

        boolean selected = cell.dayStartMillis == selectedDayStart;
        if (selected) {
            h.root.setBackgroundResource(R.drawable.bg_cal_selected);
        } else if (cell.isToday) {
            h.root.setBackgroundResource(R.drawable.bg_cal_today);
        } else {
            h.root.setBackground(null);
        }

        h.dotIncome.setVisibility(cell.hasIncome ? View.VISIBLE : View.GONE);
        h.dotExpense.setVisibility(cell.hasExpense ? View.VISIBLE : View.GONE);

        h.root.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(cell);
        });
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        final View root;
        final TextView tvDay;
        final View dotIncome, dotExpense;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.dayRoot);
            tvDay = itemView.findViewById(R.id.tvDay);
            dotIncome = itemView.findViewById(R.id.dotIncome);
            dotExpense = itemView.findViewById(R.id.dotExpense);
        }
    }
}