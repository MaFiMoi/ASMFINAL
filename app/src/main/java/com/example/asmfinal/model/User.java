package com.example.asmfinal.model;

/**
 * Represents a user entity with basic authentication and personal information.
 */
public class User {
    private int id;
    private String email;
    private String password;
    private String fullName;
    private String dateOfBirth;
    private String gender;

    // Constructor mặc định
    public User() {
    }

    // Constructor với đầy đủ tham số (dùng khi đọc từ database)
    public User(int id, String email, String password, String fullName, String dateOfBirth, String gender) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Constructor không có id (dùng khi tạo user mới để lưu vào database)
    public User(String email, String password, String fullName, String dateOfBirth, String gender) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}