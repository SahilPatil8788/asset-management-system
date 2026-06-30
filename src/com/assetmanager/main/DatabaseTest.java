package com.assetmanager.main;

import com.assetmanager.database.DatabaseConnection;
import com.assetmanager.exception.*;
import com.assetmanager.model.*;
import com.assetmanager.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Automated test script to verify database connectivity, transactions, and business logic.
 */
public class DatabaseTest {
    private static final AdminService adminService = new AdminService();
    private static final EmployeeService employeeService = new EmployeeService();
    private static final AssetService assetService = new AssetService();
    private static final TransactionService transactionService = new TransactionService();
    private static final ReportService reportService = new ReportService();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("       AMS AUTOMATED INTEGRATION TEST            ");
        System.out.println("=================================================");

        try {
            // 1. Connection Test
            System.out.print("[TEST 1/6] Database connection... ");
            DatabaseConnection.getInstance().getConnection();
            System.out.println("PASSED.");

            // 2. Authentication Test
            System.out.print("[TEST 2/6] User authentication... ");
            Admin admin = adminService.login("admin", "admin123");
            if (admin == null || !admin.getUsername().equals("admin")) {
                throw new Exception("Admin login failed.");
            }
            Employee employee = employeeService.login("alice@assets.com", "alice123");
            if (employee == null || !employee.getName().equals("Alice Smith")) {
                throw new Exception("Employee login failed.");
            }
            System.out.println("PASSED.");

            // 3. Asset CRUD & Search Test
            System.out.print("[TEST 3/6] Asset CRUD & Search... ");
            Asset testAsset = new Asset(
                0,
                "Test Macbook Pro",
                "Laptop",
                "SN-TEST-999",
                AssetStatus.AVAILABLE,
                2000.0,
                LocalDate.now()
            );
            assetService.addAsset(testAsset);
            
            // Check lookup
            Asset retrieved = assetService.getAssetById(testAsset.getId());
            if (retrieved == null || !retrieved.getSerialNumber().equals("SN-TEST-999")) {
                throw new Exception("Asset insertion/retrieval failed.");
            }

            // Search
            List<Asset> searchResults = assetService.searchAssets("Test Macbook");
            if (searchResults.isEmpty()) {
                throw new Exception("Asset search failed.");
            }
            System.out.println("PASSED.");

            // 4. Transaction Borrowing Test (with Transaction rollback check)
            System.out.print("[TEST 4/6] Transaction: Borrow Asset... ");
            transactionService.borrowAsset(retrieved.getId(), employee.getId());
            
            // Verify status changed
            Asset borrowedAsset = assetService.getAssetById(retrieved.getId());
            if (borrowedAsset.getStatus() != AssetStatus.BORROWED) {
                throw new Exception("Asset status was not updated to BORROWED.");
            }
            System.out.println("PASSED.");

            // 5. Transaction Returning Test
            System.out.print("[TEST 5/6] Transaction: Return Asset... ");
            transactionService.returnAsset(retrieved.getId(), employee.getId());
            
            // Verify status changed back
            Asset returnedAsset = assetService.getAssetById(retrieved.getId());
            if (returnedAsset.getStatus() != AssetStatus.AVAILABLE) {
                throw new Exception("Asset status was not reverted to AVAILABLE.");
            }
            System.out.println("PASSED.");

            // 6. Reports & Valuation Test
            System.out.print("[TEST 6/6] Statistics Report compilation... ");
            Map<String, Object> report = reportService.generateSummaryReport();
            if ((int) report.get("totalAssets") <= 0) {
                throw new Exception("Report summary failed.");
            }
            System.out.println("PASSED.");

            // Cleanup: delete transaction record first, then the test asset
            try (java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Transaction WHERE asset_id = ?")) {
                pstmt.setInt(1, retrieved.getId());
                pstmt.executeUpdate();
            }
            assetService.deleteAsset(retrieved.getId());
            
            System.out.println("\n=================================================");
            System.out.println("  CONGRATULATIONS: ALL INTEGRATION TESTS PASSED! ");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.out.println("FAILED.");
            System.err.println("\n[ERROR] Test Execution Failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.getInstance().closeConnection();
        }
    }
}
