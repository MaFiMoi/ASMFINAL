package com.example.asmfinal.model;

import java.util.Date;

public class Transaction {
    private String description;
    private double amount;
    private Date date;
    private TransactionType type;
    private int categoryIcon;

    public Transaction(String description, double amount, Date date, TransactionType type, int categoryIcon) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.categoryIcon = categoryIcon;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    // You may need more getters here depending on what your adapter uses.
}