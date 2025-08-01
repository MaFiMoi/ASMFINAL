package com.example.asmfinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.R;
import com.example.asmfinal.model.Transaction;
import com.example.asmfinal.adapter.TransactionAdapter;

import java.text.DecimalFormat;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private DecimalFormat currencyFormat;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
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
        Transaction transaction = transactions.get(position);

        holder.tvDescription.setText(transaction.getDescription());
        holder.ivIcon.setImageResource(transaction.getIconResId());

        // Format amount
        String formattedAmount = (transaction.getAmount() >= 0 ? "" : "-") +
                currencyFormat.format(Math.abs(transaction.getAmount())) + " VND";
        holder.tvAmount.setText(formattedAmount);

        // Set amount color
        int colorRes = transaction.getAmount() >= 0 ?
                R.color.green_positive : R.color.red_negative;
        holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(colorRes));

        // Set icon background color based on type
        int backgroundRes = getIconBackground(transaction.getType());
        holder.ivIcon.setBackgroundResource(backgroundRes);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private int getIconBackground(TransactionType type) {
        switch (type) {
            case INCOME:
                return R.drawable.circle_yellow;
            case TRANSPORT:
                return R.drawable.circle_orange;
            case UTILITIES:
                return R.drawable.circle_green;
            case FOOD:
                return R.drawable.circle_red;
            case SHOPPING:
                return R.drawable.circle_purple;
            case ENTERTAINMENT:
                return R.drawable.circle_pink;
            case HEALTHCARE:
                return R.drawable.circle_blue;
            default:
                return R.drawable.circle_gray;
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvDescription;
        TextView tvAmount;
        ImageView ivArrow;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivTransactionIcon);
            tvDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}
