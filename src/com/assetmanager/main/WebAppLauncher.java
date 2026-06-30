package com.assetmanager.main;

import com.assetmanager.api.HttpServerConfig;
import com.assetmanager.database.DatabaseConnection;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Launch entry point for the Web-based Asset Management System.
 */
public class WebAppLauncher {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   BOOTING ASSET MANAGEMENT SYSTEM WEB SUITE     ");
        System.out.println("=================================================");

        // 1. Verify database connection
        try {
            System.out.println("Connecting to MySQL Database...");
            DatabaseConnection.getInstance().getConnection();
            System.out.println("[SUCCESS] Database connectivity verified.");
        } catch (SQLException e) {
            System.err.println("[FATAL] Could not establish connection to the database.");
            System.err.println("Please check that your MySQL service is running and 'resources/db.properties' is configured correctly.");
            System.err.println("Error Details: " + e.getMessage());
            System.exit(1);
        }

        // 2. Start built-in HttpServer
        HttpServerConfig server = new HttpServerConfig(8080);
        try {
            server.start();
            System.out.println("\n-------------------------------------------------");
            System.out.println("  AMS Web UI is running. Access it here:        ");
            System.out.println("  --> http://localhost:8080                      ");
            System.out.println("-------------------------------------------------");
            System.out.println("Please keep this command window open to run the server.");
            System.out.println("Press Ctrl+C inside the console to stop the server.");
        } catch (IOException e) {
            System.err.println("[FATAL] Failed to start local web server on port 8080.");
            System.err.println("Error Details: " + e.getMessage());
            System.exit(1);
        }

        // 3. Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nGracefully shutting down server...");
            server.stop();
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("Cleanup complete. Server stopped successfully.");
        }));
    }
}
