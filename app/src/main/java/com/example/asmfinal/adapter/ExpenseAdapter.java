package com.example.asmfinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Added for icon if you decide to use it
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.R;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // For category icons (optional)

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnItemClickListener listener;

    // Optional: Map for category icons if you want to display them
    private Map<String, Integer> categoryIcons = new HashMap<>();

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
        // Initialize icons (match with your drawable names and category keys)
        categoryIcons.put("food", R.drawable.ic_food);
        categoryIcons.put("transport", R.drawable.ic_transport);
        categoryIcons.put("water", R.drawable.ic_water);
        categoryIcons.put("phone", R.drawable.ic_phone);
        categoryIcons.put("electricity", R.drawable.ic_electricity);
        categoryIcons.put("maintenance", R.drawable.ic_maintenance);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This inflates item_expense.xml, which now needs to be your RecyclerView item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // For the main screen's summarized categories, 'title' is the category name,
        // 'amount' is the total for that category, and 'date' is the categoryKey.
        holder.title.setText(expense.getTitle()); // This is now the category name (e.g., "Ăn uống")
        holder.amount.setText("-" + formatCurrency(expense.getAmount()) + " VND");
        // holder.date.setText(expense.getDate()); // We don't need a date for summarized categories

        // Set the icon based on the category key (which is stored in expense.getDate() for this purpose)
        String categoryKey = expense.getDate(); // Re-using date field for categoryKey
        if (categoryIcons.containsKey(categoryKey)) {
            holder.icon.setImageResource(categoryIcons.get(categoryKey));
            // You might also need to set the background if your icons are layered
            // holder.icon.setBackgroundResource(R.drawable.ic_circle_background); // Example
        } else {
            holder.icon.setImageResource(R.drawable.ic_default_category); // Default icon
        }


        // Set click listener for the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(expense);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView title, amount;
        ImageView icon; // Added ImageView for the icon
        // TextView date; // Removed as it's not needed for the summarized view

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.expenseTitle); // This will be tvCategoryName in item_expense.xml
            amount = itemView.findViewById(R.id.expenseAmount); // This will be tvAmount in item_expense.xml
            icon = itemView.findViewById(R.id.expenseIcon); // You need to add this ID to your item_expense.xml
            // date = itemView.findViewById(R.id.expenseDate); // Removed
        }
    }

    private String formatCurrency(int amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }
}