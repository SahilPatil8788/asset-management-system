package com.assetmanager.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connector that manages the connection lifecycle and transactions.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        loadProperties();
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("resources/db.properties")) {
            props.load(fis);
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
        } catch (IOException e) {
            // Fallbacks in case config file is not loaded
            this.url = "jdbc:mysql://localhost:3306/asset_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            this.username = "root";
            this.password = "Sahil@8788";
            System.err.println("Warning: Could not load db.properties, using fallback defaults. Error: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Explicitly load the driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, username, password);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found in classpath.", e);
            }
        }
        return connection;
    }

    /**
     * Disables auto-commit to start a transaction context.
     */
    public void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    /**
     * Commits the current transaction and restores auto-commit.
     */
    public void commitTransaction() throws SQLException {
        Connection conn = getConnection();
        if (!conn.getAutoCommit()) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    /**
     * Rolls back changes in the current transaction and restores auto-commit.
     */
    public void rollbackTransaction() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error executing transaction rollback: " + e.getMessage());
        }
    }

    /**
     * Closes the active connection if it exists.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
