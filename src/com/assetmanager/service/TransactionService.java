package com.assetmanager.service;

import com.assetmanager.database.DatabaseConnection;
import com.assetmanager.dao.*;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.exception.TransactionException;
import com.assetmanager.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer class managing database transactions for borrowing and returning assets.
 */
public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final AssetDAO assetDAO;
    private final EmployeeDAO employeeDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAOImpl();
        this.assetDAO = new AssetDAOImpl();
        this.employeeDAO = new EmployeeDAOImpl();
    }

    /**
     * Borrows an asset. Uses database transaction to guarantee consistency.
     */
    public void borrowAsset(int assetId, int employeeId) throws DatabaseException, TransactionException {
        // Validate existence and availability
        Employee employee = employeeDAO.getById(employeeId);
        if (employee == null) {
            throw new TransactionException("Employee with ID " + employeeId + " does not exist.");
        }

        Asset asset = assetDAO.getById(assetId);
        if (asset == null) {
            throw new TransactionException("Asset with ID " + assetId + " does not exist.");
        }

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new TransactionException("Asset '" + asset.getName() + "' is not available. Current status: " + asset.getStatus());
        }

        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        try {
            // Begin Transaction
            dbConn.beginTransaction();

            // 1. Update asset status
            assetDAO.updateStatus(assetId, AssetStatus.BORROWED);

            // 2. Add transaction log
            Transaction transaction = new Transaction(
                0,
                assetId,
                employeeId,
                LocalDate.now(),
                null,
                TransactionStatus.BORROWED
            );
            transactionDAO.add(transaction);

            // Commit Transaction
            dbConn.commitTransaction();

        } catch (SQLException | DatabaseException e) {
            // Rollback on failure
            dbConn.rollbackTransaction();
            throw new DatabaseException("Transaction failed: Borrow operation rolled back. Reason: " + e.getMessage(), e);
        }
    }

    /**
     * Returns an asset. Uses database transaction.
     * @param employeeId Optional. If provided, ensures the asset was borrowed by this employee.
     */
    public void returnAsset(int assetId, Integer employeeId) throws DatabaseException, TransactionException {
        Asset asset = assetDAO.getById(assetId);
        if (asset == null) {
            throw new TransactionException("Asset with ID " + assetId + " does not exist.");
        }

        if (asset.getStatus() != AssetStatus.BORROWED) {
            throw new TransactionException("Asset '" + asset.getName() + "' is not currently borrowed.");
        }

        // Get the active borrow log
        Transaction transaction = transactionDAO.getActiveByAssetId(assetId);
        if (transaction == null) {
            throw new TransactionException("No active borrow transaction found for asset ID " + assetId);
        }

        // Check ownership if employee ID is given (prevent employee A from returning employee B's asset)
        if (employeeId != null && transaction.getEmployeeId() != employeeId) {
            throw new TransactionException("Access denied: You did not borrow this asset.");
        }

        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        try {
            // Begin Transaction
            dbConn.beginTransaction();

            // 1. Update asset status back to AVAILABLE
            assetDAO.updateStatus(assetId, AssetStatus.AVAILABLE);

            // 2. Update transaction record
            transaction.setReturnDate(LocalDate.now());
            transaction.setStatus(TransactionStatus.RETURNED);
            transactionDAO.update(transaction);

            // Commit Transaction
            dbConn.commitTransaction();

        } catch (SQLException | DatabaseException e) {
            // Rollback on failure
            dbConn.rollbackTransaction();
            throw new DatabaseException("Transaction failed: Return operation rolled back. Reason: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getAllTransactions() throws DatabaseException {
        return transactionDAO.getAll();
    }

    public List<Transaction> getTransactionsByEmployee(int employeeId) throws DatabaseException {
        return transactionDAO.getByEmployeeId(employeeId);
    }
}
