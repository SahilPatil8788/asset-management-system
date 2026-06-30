package com.assetmanager.dao;

import com.assetmanager.database.DatabaseConnection;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Asset;
import com.assetmanager.model.AssetStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of the AssetDAO interface.
 */
public class AssetDAOImpl implements AssetDAO {

    @Override
    public void add(Asset asset) throws DatabaseException {
        String sql = "INSERT INTO Asset (name, category, serial_number, status, price, purchase_date) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, asset.getName());
                pstmt.setString(2, asset.getCategory());
                pstmt.setString(3, asset.getSerialNumber());
                pstmt.setString(4, asset.getStatus().name());
                pstmt.setDouble(5, asset.getPrice());
                pstmt.setDate(6, Date.valueOf(asset.getPurchaseDate()));
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        asset.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add asset: " + asset.getName(), e);
        }
    }

    @Override
    public Asset getById(int id) throws DatabaseException {
        String sql = "SELECT id, name, category, serial_number, status, price, purchase_date FROM Asset WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAsset(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch asset by id: " + id, e);
        }
        return null;
    }

    @Override
    public Asset getBySerialNumber(String serialNumber) throws DatabaseException {
        String sql = "SELECT id, name, category, serial_number, status, price, purchase_date FROM Asset WHERE serial_number = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, serialNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAsset(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch asset by serial number: " + serialNumber, e);
        }
        return null;
    }

    @Override
    public List<Asset> getAll() throws DatabaseException {
        List<Asset> list = new ArrayList<>();
        String sql = "SELECT id, name, category, serial_number, status, price, purchase_date FROM Asset";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAsset(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all assets", e);
        }
        return list;
    }

    @Override
    public void update(Asset asset) throws DatabaseException {
        String sql = "UPDATE Asset SET name = ?, category = ?, serial_number = ?, status = ?, price = ?, purchase_date = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, asset.getName());
                pstmt.setString(2, asset.getCategory());
                pstmt.setString(3, asset.getSerialNumber());
                pstmt.setString(4, asset.getStatus().name());
                pstmt.setDouble(5, asset.getPrice());
                pstmt.setDate(6, Date.valueOf(asset.getPurchaseDate()));
                pstmt.setInt(7, asset.getId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update asset: " + asset.getName(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM Asset WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete asset with id: " + id, e);
        }
    }

    @Override
    public List<Asset> searchByNameOrCategory(String query) throws DatabaseException {
        List<Asset> list = new ArrayList<>();
        String sql = "SELECT id, name, category, serial_number, status, price, purchase_date FROM Asset " +
                     "WHERE name LIKE ? OR category LIKE ? OR serial_number LIKE ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String searchPattern = "%" + query + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToAsset(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search assets with query: " + query, e);
        }
        return list;
    }

    @Override
    public void updateStatus(int assetId, AssetStatus status) throws DatabaseException {
        String sql = "UPDATE Asset SET status = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status.name());
                pstmt.setInt(2, assetId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update status for asset id: " + assetId, e);
        }
    }

    private Asset mapResultSetToAsset(ResultSet rs) throws SQLException {
        return new Asset(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("serial_number"),
            AssetStatus.fromString(rs.getString("status")),
            rs.getDouble("price"),
            rs.getDate("purchase_date").toLocalDate()
        );
    }
}
