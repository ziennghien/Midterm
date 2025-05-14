package com.example.studentmanagementapp.model;
public class Student {
    private String id;
    private String name;
    private String major;
    private String className;

    // Constructor mặc định (bắt buộc cho Firebase)
    public Student() {
    }

    // Constructor đầy đủ
    public Student(String id, String name, String major, String className) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.className = className;
    }

    // Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
