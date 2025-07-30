package com.example.asmfinal.model;

/**
 * Represents a user entity with basic authentication and personal information.
 */
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
    /**
     * Returns the unique ID of the user.
     * @return the user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique ID of the user.
     * @param id the user ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the username of the user.
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password of the user.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user.
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the full name of the user.
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of the user.
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}