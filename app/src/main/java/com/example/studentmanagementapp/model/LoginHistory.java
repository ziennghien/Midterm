package com.example.studentmanagementapp.model;

public class LoginHistory {

    private String userId;
    private long timestamp;

    // Không lưu trực tiếp trong Firebase, chỉ để hiển thị
    private String name;
    private String userName;
    private String role;
    private String avatarUrl;

    public LoginHistory() {
        // Required for Firebase
    }

    public LoginHistory(String userId, long timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters & Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Các thông tin phụ trợ dùng khi binding UI
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
