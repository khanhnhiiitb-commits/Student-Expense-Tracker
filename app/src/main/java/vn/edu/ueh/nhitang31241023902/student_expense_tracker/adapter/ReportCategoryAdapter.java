package vn.edu.ueh.nhitang31241023902.student_expense_tracker.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.ueh.nhitang31241023902.student_expense_tracker.R;

public class ReportCategoryAdapter extends RecyclerView.Adapter<ReportCategoryAdapter.ViewHolder> {

    private List<CategoryStat> list = new ArrayList<>();
    private final DecimalFormat moneyFormat =
            new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(new Locale("vi", "VN")));

    public void setData(List<CategoryStat> newList) {
        list = newList == null ? new ArrayList<>() : newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CategoryStat s = list.get(position);
        h.tvCategoryName.setText(s.categoryName);
        h.tvCategoryPercent.setText(String.format(new Locale("vi", "VN"), "%.1f%%", s.percent));
        h.tvCategoryAmount.setText(moneyFormat.format(s.totalAmount) + " đ");
        h.viewDot.setBackgroundTintList(ColorStateList.valueOf(s.color));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewDot;
        TextView tvCategoryName, tvCategoryPercent, tvCategoryAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot = itemView.findViewById(R.id.viewDot);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryPercent = itemView.findViewById(R.id.tvCategoryPercent);
            tvCategoryAmount = itemView.findViewById(R.id.tvCategoryAmount);
        }
    }
}
