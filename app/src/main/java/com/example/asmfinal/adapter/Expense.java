package com.example.asmfinal.adapter;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Expense implements Serializable {

    private int id;
    private String category;
    private String categoryName;
    private int amount;
    private String description;
    private String date;
    private String time;

    // Constructor 1: Sử dụng cho các trường hợp lấy dữ liệu đầy đủ từ Database
    // đã bao gồm ID, category (key), amount, description, date, và time.
    public Expense(int id, String category, String categoryName, int amount, String description, String date, String time) {
        this.id = id;
        this.category = category;
        this.categoryName = categoryName;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.time = time;
    }

    // Constructor 2: Dùng cho việc hiển thị chi tiêu tổng hợp trên màn hình Home
    public Expense(String categoryName, int amount, String category) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.category = category;
    }

    // Constructor 3: Dùng cho việc tạo chi tiêu mới, chưa có ID
    // ID sẽ được gán bởi Database sau khi insert
    public Expense(String category, int amount, String description, String date, String time) {
        this.id = 0; // ID sẽ được DB gán tự động
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.time = time;
    }

    // --- Getters (giữ nguyên) ---
    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    // --- Setters (giữ nguyên) ---
    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Phương thức tiện ích để lấy đối tượng Date từ chuỗi ngày và giờ.
     * Hữu ích khi cần sắp xếp hoặc so sánh thời gian.
     */
    public Date getDateTimeObject() {
        if (this.date == null || this.time == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            return format.parse(this.date + " " + this.time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}