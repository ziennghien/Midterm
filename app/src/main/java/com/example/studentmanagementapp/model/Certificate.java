package com.example.studentmanagementapp.model;

import java.io.Serializable;

public class Certificate implements Serializable {
    private String id;
    private String name;
    private String studentId;
    private String issueDate;

    // Constructor mặc định (Firebase)
    public Certificate() {}

    // Constructor đầy đủ
    public Certificate(String id, String name, String studentId, String issueDate) {
        this.id        = id;
        this.name      = name;
        this.studentId = studentId;
        this.issueDate = issueDate;
    }

    // Getters / Setters
    public String getId()          { return id; }
    public void setId(String id)   { this.id = id; }

    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }

    public String getStudentId()               { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getIssueDate()               { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
}
