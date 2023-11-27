package com.example.nusync.data;

public class Feedback {

    private String name;
    private String email;
    private String feedback;

    // Constructor
    public Feedback(String name, String email, String feedback) {
        this.name = name;
        this.email = email;
        this.feedback = feedback;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getFeedback() {
        return feedback;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
