package com.assetmanager.model;

/**
 * Admin model class extending the User class.
 * Demonstrates inheritance.
 */
public class Admin extends User {
    private String username;

    public Admin() {
        super();
    }

    public Admin(int id, String username, String email, String password) {
        super(id, email, password);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
