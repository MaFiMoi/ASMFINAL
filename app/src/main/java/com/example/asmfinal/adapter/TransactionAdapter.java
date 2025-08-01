package com.example.asmfinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.R;
import com.example.asmfinal.model.Transaction;

import java.text.DecimalFormat;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private DecimalFormat currencyFormat;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
        this.currencyFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Bind data from the Transaction object to the views
        holder.tvCategoryName.setText(transaction.getCategoryName());
        holder.tvDescription.setText(transaction.getDescription());

        double amount = transaction.getAmount();
        String formattedAmount = currencyFormat.format(Math.abs(amount)) + " VND";
        holder.tvAmount.setText(formattedAmount);

        // Set amount color based on whether it's income or expense
        if (amount < 0) {
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red_negative));
        } else {
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_positive));
        }

        // Set category icon
        holder.ivCategoryIcon.setImageResource(transaction.getCategoryIconResId());

        // Note: The icon background logic you had previously might need to be implemented
        // in your DatabaseHelper or a separate helper class if your database stores
        // this information. For now, we'll stick to a simple icon setting.
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    /**
     * Updates the data in the adapter and notifies the RecyclerView.
     */
    public void updateData(List<Transaction> newData) {
        this.transactionList.clear();
        this.transactionList.addAll(newData);
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvDescription;
        TextView tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}