package com.example.asmfinal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.asmfinal.R;
import com.example.asmfinal.model.Transaction;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> transactions;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        // Bind your transaction data to the views in the ViewHolder
        // holder.tvDescription.setText(transaction.getDescription());
        // holder.tvAmount.setText(String.valueOf(transaction.getAmount()));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // You'll need to define your views here based on your item_transaction.xml
        // public TextView tvDescription;
        // public TextView tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize your views here
            // tvDescription = itemView.findViewById(R.id.tvDescription);
            // tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}