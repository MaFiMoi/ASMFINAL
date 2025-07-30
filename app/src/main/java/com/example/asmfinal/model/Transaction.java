package com.example.asmfinal.model;

import java.util.Date;

public class Transaction {
    private String description;
    private double amount;
    private Date date;
    private TransactionType type;
    private int iconResId;

    public Transaction(String description, double amount, Date date, TransactionType type, int iconResId) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.iconResId = iconResId;
    }

    // Getters and setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public boolean isIncome() {
        return amount > 0;
    }

    public boolean isExpense() {
        return amount < 0;
    }
}
