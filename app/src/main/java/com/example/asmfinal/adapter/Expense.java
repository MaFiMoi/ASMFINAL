package com.example.asmfinal.adapter;

public class Expense {
    private int id;
    private String title;
    private int amount;
    private String date;
    private String category;

    // Main constructor including ID
    public Expense(int id, String title, int amount, String date, String category) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    // Constructor for creating a NEW expense (before it has an ID from the DB)
    public Expense(String title, int amount, String date, String category) {
        this(0, title, amount, date, category); // Assign a default/placeholder ID (like 0 or -1) for new expenses
    }

    // Existing constructor (might be used less now that category is central)
    public Expense(String title, int amount, String date) {
        this(0, title, amount, date, null); // Assign a default ID and null category
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    // Setters (optional, but useful if you need to modify Expense objects)
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}