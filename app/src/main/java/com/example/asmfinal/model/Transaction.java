package com.example.asmfinal.model;

import java.util.Date;

public class Transaction {
    private int id;
    private String description;
    private double amount;
    private Date date;
    private String categoryName;
    private int categoryIconResId;

    public Transaction(int id, String description, double amount, Date date, String categoryName, int categoryIconResId) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.categoryIconResId = categoryIconResId;
    }

    // Getters for all fields
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getCategoryIconResId() {
        return categoryIconResId;
    }

    // Utility methods
    public boolean isIncome() {
        return amount > 0;
    }

    public boolean isExpense() {
        return amount < 0;
    }
}