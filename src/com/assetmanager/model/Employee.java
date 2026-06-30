package com.assetmanager.model;

/**
 * Employee model class extending the User class.
 * Demonstrates inheritance.
 */
public class Employee extends User {
    private String name;
    private String department;

    public Employee() {
        super();
    }

    public Employee(int id, String name, String email, String department, String password) {
        super(id, email, password);
        this.name = name;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
