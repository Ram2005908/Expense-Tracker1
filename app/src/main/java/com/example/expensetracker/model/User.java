package com.example.expensetracker.model;

public class User {
    private String uid;
    private String email;
    private double monthlyBudget;
    private String displayName;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.monthlyBudget = 0.0;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
} 