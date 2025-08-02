package com.example.asmfinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.R;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnItemClickListener listener;
    private final Map<String, Integer> categoryIcons = new HashMap<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    // Interface để xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
        // Khởi tạo icons, khớp với drawable và category key
        categoryIcons.put("food", R.drawable.ic_food);
        categoryIcons.put("transport", R.drawable.ic_transport);
        categoryIcons.put("water", R.drawable.ic_water);
        categoryIcons.put("phone", R.drawable.ic_phone);
        categoryIcons.put("electricity", R.drawable.ic_electricity);
        categoryIcons.put("maintenance", R.drawable.ic_maintenance);
    }

    // Phương thức để cập nhật dữ liệu một cách hiệu quả
    public void updateData(List<Expense> newExpenses) {
        this.expenseList.clear();
        this.expenseList.addAll(newExpenses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCategoryName;
        final TextView tvAmount;
        final ImageView ivCategoryIcon;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View từ layout item_expense.xml
            tvCategoryName = itemView.findViewById(R.id.expenseTitle);
            tvAmount = itemView.findViewById(R.id.expenseAmount);
            ivCategoryIcon = itemView.findViewById(R.id.expenseIcon);

            // Gán OnClickListener cho toàn bộ item view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(expenseList.get(position));
                }
            });
        }

        public void bind(Expense expense) {
            if (expense == null) return;

            // Lấy tên danh mục hoặc mô tả
            // Lựa chọn giữa tên danh mục hoặc mô tả tùy thuộc vào bạn muốn hiển thị gì
            if (tvCategoryName != null) {
                // Hiển thị tên danh mục
                tvCategoryName.setText(expense.getCategoryName());
            }

            // Định dạng và hiển thị số tiền
            if (tvAmount != null) {
                // Sử dụng Math.abs() để đảm bảo số tiền hiển thị luôn dương trước khi thêm dấu "-"
                tvAmount.setText(formatter.format(Math.abs(expense.getAmount())) + " VND");
            }

            // Lấy category key từ Expense để hiển thị icon
            if (ivCategoryIcon != null) {
                String categoryKey = expense.getCategory();
                Integer iconResId = categoryIcons.get(categoryKey);
                if (iconResId != null) {
                    ivCategoryIcon.setImageResource(iconResId);
                } else {
                    ivCategoryIcon.setImageResource(R.drawable.ic_default_category); // Icon mặc định
                }
            }
        }
    }
}