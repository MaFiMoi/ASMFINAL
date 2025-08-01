package com.example.asmfinal.adapter;

import android.content.Context; // Import Context
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

    private final Context context; // Thêm trường Context
    private List<Transaction> transactionList;
    private DecimalFormat currencyFormat;

    // Sửa constructor để nhận cả Context và List
    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.currencyFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng context để lấy LayoutInflater
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvCategoryName.setText(transaction.getCategoryName());
        holder.tvDescription.setText(transaction.getDescription());

        double amount = transaction.getAmount();
        String formattedAmount = currencyFormat.format(Math.abs(amount)) + " VND";
        holder.tvAmount.setText(formattedAmount);

        // Đặt màu theo thu (xanh) hoặc chi (đỏ)
        if (amount < 0) {
            // Sử dụng Context để lấy màu
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red_negative));
        } else {
            // Sử dụng Context để lấy màu
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green_positive));
        }

        // Icon
        holder.ivCategoryIcon.setImageResource(transaction.getCategoryIconResId());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

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