package com.assetmanager.dao;

import com.assetmanager.database.DatabaseConnection;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Transaction;
import com.assetmanager.model.TransactionStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of the TransactionDAO interface.
 */
public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public void add(Transaction transaction) throws DatabaseException {
        String sql = "INSERT INTO Transaction (asset_id, employee_id, borrow_date, return_date, status) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, transaction.getAssetId());
                pstmt.setInt(2, transaction.getEmployeeId());
                pstmt.setDate(3, Date.valueOf(transaction.getBorrowDate()));
                if (transaction.getReturnDate() != null) {
                    pstmt.setDate(4, Date.valueOf(transaction.getReturnDate()));
                } else {
                    pstmt.setNull(4, Types.DATE);
                }
                pstmt.setString(5, transaction.getStatus().name());
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        transaction.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert transaction log.", e);
        }
    }

    @Override
    public Transaction getById(int id) throws DatabaseException {
        String sql = "SELECT id, asset_id, employee_id, borrow_date, return_date, status FROM Transaction WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTransaction(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch transaction by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<Transaction> getAll() throws DatabaseException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, asset_id, employee_id, borrow_date, return_date, status FROM Transaction ORDER BY borrow_date DESC";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all transactions.", e);
        }
        return list;
    }

    @Override
    public List<Transaction> getByEmployeeId(int employeeId) throws DatabaseException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, asset_id, employee_id, borrow_date, return_date, status FROM Transaction WHERE employee_id = ? ORDER BY borrow_date DESC";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, employeeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToTransaction(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch transactions for employee ID: " + employeeId, e);
        }
        return list;
    }

    @Override
    public List<Transaction> getByAssetId(int assetId) throws DatabaseException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, asset_id, employee_id, borrow_date, return_date, status FROM Transaction WHERE asset_id = ? ORDER BY borrow_date DESC";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, assetId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToTransaction(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch transactions for asset ID: " + assetId, e);
        }
        return list;
    }

    @Override
    public Transaction getActiveByAssetId(int assetId) throws DatabaseException {
        String sql = "SELECT id, asset_id, employee_id, borrow_date, return_date, status FROM Transaction WHERE asset_id = ? AND status = 'Borrowed'";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, assetId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTransaction(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch active transaction for asset ID: " + assetId, e);
        }
        return null;
    }

    @Override
    public void update(Transaction transaction) throws DatabaseException {
        String sql = "UPDATE Transaction SET asset_id = ?, employee_id = ?, borrow_date = ?, return_date = ?, status = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, transaction.getAssetId());
                pstmt.setInt(2, transaction.getEmployeeId());
                pstmt.setDate(3, Date.valueOf(transaction.getBorrowDate()));
                if (transaction.getReturnDate() != null) {
                    pstmt.setDate(4, Date.valueOf(transaction.getReturnDate()));
                } else {
                    pstmt.setNull(4, Types.DATE);
                }
                pstmt.setString(5, transaction.getStatus().name());
                pstmt.setInt(6, transaction.getId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update transaction ID: " + transaction.getId(), e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Date returnDateSql = rs.getDate("return_date");
        LocalDate returnDate = returnDateSql != null ? returnDateSql.toLocalDate() : null;
        
        return new Transaction(
            rs.getInt("id"),
            rs.getInt("asset_id"),
            rs.getInt("employee_id"),
            rs.getDate("borrow_date").toLocalDate(),
            returnDate,
            TransactionStatus.fromString(rs.getString("status"))
        );
    }
}
