package com.assetmanager.model;

import java.time.LocalDate;

/**
 * Model class representing a borrowing/returning transaction.
 */
public class Transaction {
    private int id;
    private int assetId;
    private int employeeId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private TransactionStatus status;

    public Transaction() {}

    public Transaction(int id, int assetId, int employeeId, LocalDate borrowDate, LocalDate returnDate, TransactionStatus status) {
        this.id = id;
        this.assetId = assetId;
        this.employeeId = employeeId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
