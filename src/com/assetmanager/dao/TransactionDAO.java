package com.assetmanager.dao;

import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Transaction;
import java.util.List;

/**
 * Data Access Object Interface for Transaction entities.
 */
public interface TransactionDAO {
    void add(Transaction transaction) throws DatabaseException;
    Transaction getById(int id) throws DatabaseException;
    List<Transaction> getAll() throws DatabaseException;
    List<Transaction> getByEmployeeId(int employeeId) throws DatabaseException;
    List<Transaction> getByAssetId(int assetId) throws DatabaseException;
    Transaction getActiveByAssetId(int assetId) throws DatabaseException;
    void update(Transaction transaction) throws DatabaseException;
}
