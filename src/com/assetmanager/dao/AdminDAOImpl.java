package com.assetmanager.dao;

import com.assetmanager.database.DatabaseConnection;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC implementation of the AdminDAO interface.
 */
public class AdminDAOImpl implements AdminDAO {
    
    @Override
    public Admin getByUsername(String username) throws DatabaseException {
        String sql = "SELECT id, username, email, password FROM Admin WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch admin by username: " + username, e);
        }
        return null;
    }
}
