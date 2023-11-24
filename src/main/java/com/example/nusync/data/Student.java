package com.example.nusync.data;

public class Student {
    private String fullName;
    private String email;
    private String section;
    private String batch;
    private String department;

    // Constructor
    public Student(String fullName, String email, String section, String batch, String department) {
        this.fullName = fullName;
        this.email = email;
        this.section = section;
        this.batch = batch;
        this.department = department;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // You might also include other methods relevant to the student here
}

