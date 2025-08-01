package com.example.asmfinal.model;

import java.util.Date;

public class Transaction {
    private int id;
    private String description;
    private double amount;
    private Date date;
    private String categoryName;
    private int categoryIconResId;
    private TransactionType type;

    // Constructor 1: Sử dụng để tạo một Transaction mới (chưa có ID)
    public Transaction(String description, double amount, Date date, String categoryName, int categoryIconResId) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.categoryIconResId = categoryIconResId;
        // Tự động xác định loại giao dịch dựa trên số tiền
        this.type = (amount >= 0) ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    // Constructor 2: Sử dụng để lấy một Transaction từ database (đã có ID)
    public Transaction(int id, String description, double amount, Date date, String categoryName, int categoryIconResId) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.categoryIconResId = categoryIconResId;
        // Tự động xác định loại giao dịch dựa trên số tiền
        this.type = (amount >= 0) ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    // Constructor rỗng (optional)
    public Transaction() {
    }

    // Getters
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

    public TransactionType getType() {
        return type;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        // Cập nhật lại loại giao dịch khi số tiền thay đổi
        this.type = (amount >= 0) ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setCategoryIconResId(int categoryIconResId) {
        this.categoryIconResId = categoryIconResId;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    // Utilities
    public boolean isIncome() {
        return this.amount >= 0; // Hoặc return type == TransactionType.INCOME;
    }

    public boolean isExpense() {
        return this.amount < 0; // Hoặc return type == TransactionType.EXPENSE;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", categoryName='" + categoryName + '\'' +
                ", categoryIconResId=" + categoryIconResId +
                ", type=" + type +
                '}';
    }
}