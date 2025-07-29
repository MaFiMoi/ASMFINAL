package com.example.asmfinal.model;

public class User {
    private int id;             // ID duy nhất của user
    private String username;    // Tên đăng nhập
    private String password;    // Mật khẩu
    private String fullName;    // Họ tên đầy đủ

    // Constructor mặc định
    public User() {
    }

    // Constructor với đầy đủ tham số
    public User(int id, String username, String password, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    // Constructor không có id (dùng khi tạo user mới)
    public User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
