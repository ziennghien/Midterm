package com.example.studentmanagementapp.models;

public class User {

    private String name;
    private int age;
    private String phone;
    private String email;
    private String password;
    private String status;  // "Normal" hoặc "Locked"
    private String role;    // "admin", "manager", "employee"
    private String avt;

    public User() {
        // Bắt buộc phải có constructor trống cho Firestore
    }

    public User(String name, int age, String phone, String email, String password, String status, String role, String avt) {
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.status = status;
        this.role = role;
        this.avt = avt;

    }

    // Getters and Setters

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }

    public void setAge(int age) { this.age = age; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public String getAvt() { return avt; }

    public void setAvt(String avt) { this.avt = avt; }
}
