package com.assetmanager.main;

import com.assetmanager.database.DatabaseConnection;
import com.assetmanager.exception.*;
import com.assetmanager.model.*;
import com.assetmanager.service.*;
import com.assetmanager.util.ConsoleTable;
import com.assetmanager.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main application class that coordinates the CLI presentation and orchestrates services.
 */
public class AssetManagerApp {
    private static final Scanner scanner = new Scanner(System.in);
    
    // Instantiate all required services
    private static final AdminService adminService = new AdminService();
    private static final EmployeeService employeeService = new EmployeeService();
    private static final AssetService assetService = new AssetService();
    private static final TransactionService transactionService = new TransactionService();
    private static final ReportService reportService = new ReportService();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  WELCOME TO THE ASSET MANAGEMENT SYSTEM (AMS)   ");
        System.out.println("=================================================");
        
        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Admin Login");
            System.out.println("2. Employee Login");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleAdminLogin();
                    break;
                case "2":
                    handleEmployeeLogin();
                    break;
                case "3":
                    running = false;
                    System.out.println("\nThank you for using Asset Management System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1, 2, or 3.");
            }
        }
        
        // Clean up JDBC connection pool / instance on application close
        DatabaseConnection.getInstance().closeConnection();
        scanner.close();
    }

    // ==========================================
    // LOGIN WORKFLOWS
    // ==========================================

    private static void handleAdminLogin() {
        System.out.println("\n--- ADMIN LOGIN ---");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {
            Admin admin = adminService.login(username, password);
            System.out.println("\nSuccess: Welcome, Admin " + admin.getUsername() + "!");
            runAdminMenu(admin);
        } catch (AuthenticationException e) {
            System.out.println("\nError: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("\nDatabase Error: " + e.getMessage());
        }
    }

    private static void handleEmployeeLogin() {
        System.out.println("\n--- EMPLOYEE LOGIN ---");
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {
            Employee emp = employeeService.login(email, password);
            System.out.println("\nSuccess: Welcome back, " + emp.getName() + "!");
            runEmployeeMenu(emp);
        } catch (AuthenticationException e) {
            System.out.println("\nError: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("\nDatabase Error: " + e.getMessage());
        }
    }

    // ==========================================
    // ADMIN DASHBOARD
    // ==========================================

    private static void runAdminMenu(Admin admin) {
        boolean inAdminDashboard = true;
        while (inAdminDashboard) {
            System.out.println("\n=================================");
            System.out.println("      ADMIN DASHBOARD            ");
            System.out.println("=================================");
            System.out.println("1. Add New Asset");
            System.out.println("2. View All Assets");
            System.out.println("3. Update Asset Details");
            System.out.println("4. Delete Asset");
            System.out.println("5. Search Assets");
            System.out.println("6. Sort Assets");
            System.out.println("7. Register Employee Account");
            System.out.println("8. View All Employees");
            System.out.println("9. View Complete Transaction History");
            System.out.println("10. Generate System Statistics Report");
            System.out.println("11. Logout");
            System.out.print("Select an option: ");

            String option = scanner.nextLine().trim();
            try {
                switch (option) {
                    case "1":
                        adminAddAsset();
                        break;
                    case "2":
                        adminViewAllAssets();
                        break;
                    case "3":
                        adminUpdateAsset();
                        break;
                    case "4":
                        adminDeleteAsset();
                        break;
                    case "5":
                        adminSearchAssets();
                        break;
                    case "6":
                        adminSortAssets();
                        break;
                    case "7":
                        adminRegisterEmployee();
                        break;
                    case "8":
                        adminViewAllEmployees();
                        break;
                    case "9":
                        adminViewTransactions();
                        break;
                    case "10":
                        adminGenerateReport();
                        break;
                    case "11":
                        inAdminDashboard = false;
                        System.out.println("Logged out from Admin successfully.");
                        break;
                    default:
                        System.out.println("Invalid option. Please enter 1-11.");
                }
            } catch (Exception e) {
                System.out.println("\nAn unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private static void adminAddAsset() {
        System.out.println("\n--- ADD NEW ASSET ---");
        try {
            System.out.print("Asset Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Category (e.g., Laptop, Monitor, Phone, Tablet): ");
            String category = scanner.nextLine().trim();
            System.out.print("Serial Number: ");
            String serialNumber = scanner.nextLine().trim();
            
            System.out.print("Price (e.g. 1200.50): ");
            double price = InputValidator.parseDouble(scanner.nextLine().trim(), "Price");
            
            System.out.print("Purchase Date (YYYY-MM-DD, e.g. 2026-01-15): ");
            LocalDate purchaseDate = InputValidator.parseDate(scanner.nextLine().trim());

            Asset asset = new Asset(0, name, category, serialNumber, AssetStatus.AVAILABLE, price, purchaseDate);
            assetService.addAsset(asset);
            System.out.println("\nSuccess: Asset added successfully with ID: " + asset.getId());
        } catch (ValidationException | DatabaseException e) {
            System.out.println("\nError adding asset: " + e.getMessage());
        }
    }

    private static void adminViewAllAssets() {
        System.out.println("\n--- ALL REGISTERED ASSETS ---");
        try {
            List<Asset> assets = assetService.getAllAssets();
            displayAssetTable(assets);
        } catch (DatabaseException e) {
            System.out.println("Error fetching assets: " + e.getMessage());
        }
    }

    private static void adminUpdateAsset() {
        System.out.println("\n--- UPDATE ASSET DETAILS ---");
        try {
            System.out.print("Enter the ID of the Asset to update: ");
            int id = InputValidator.parseInt(scanner.nextLine().trim(), "Asset ID");
            
            Asset existingAsset = assetService.getAssetById(id);
            if (existingAsset == null) {
                System.out.println("Error: No asset found with ID " + id);
                return;
            }

            System.out.println("Current details: " + existingAsset.getName() + " (" + existingAsset.getSerialNumber() + ")");
            
            System.out.print("New Name [leave blank to keep '" + existingAsset.getName() + "']: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) existingAsset.setName(name);

            System.out.print("New Category [leave blank to keep '" + existingAsset.getCategory() + "']: ");
            String category = scanner.nextLine().trim();
            if (!category.isEmpty()) existingAsset.setCategory(category);

            System.out.print("New Serial Number [leave blank to keep '" + existingAsset.getSerialNumber() + "']: ");
            String serialNumber = scanner.nextLine().trim();
            if (!serialNumber.isEmpty()) existingAsset.setSerialNumber(serialNumber);

            System.out.print("New Price [leave blank to keep '" + existingAsset.getPrice() + "']: ");
            String priceStr = scanner.nextLine().trim();
            if (!priceStr.isEmpty()) {
                double price = InputValidator.parseDouble(priceStr, "Price");
                existingAsset.setPrice(price);
            }

            System.out.print("New Purchase Date (YYYY-MM-DD) [leave blank to keep '" + existingAsset.getPurchaseDate() + "']: ");
            String dateStr = scanner.nextLine().trim();
            if (!dateStr.isEmpty()) {
                LocalDate purchaseDate = InputValidator.parseDate(dateStr);
                existingAsset.setPurchaseDate(purchaseDate);
            }

            System.out.print("New Status (AVAILABLE, BORROWED, MAINTENANCE) [leave blank to keep '" + existingAsset.getStatus() + "']: ");
            String statusStr = scanner.nextLine().trim();
            if (!statusStr.isEmpty()) {
                existingAsset.setStatus(AssetStatus.fromString(statusStr));
            }

            assetService.updateAsset(existingAsset);
            System.out.println("\nSuccess: Asset details updated successfully!");
        } catch (ValidationException | DatabaseException | IllegalArgumentException e) {
            System.out.println("\nError updating asset: " + e.getMessage());
        }
    }

    private static void adminDeleteAsset() {
        System.out.println("\n--- DELETE ASSET ---");
        try {
            System.out.print("Enter the ID of the Asset to delete: ");
            int id = InputValidator.parseInt(scanner.nextLine().trim(), "Asset ID");

            Asset asset = assetService.getAssetById(id);
            if (asset == null) {
                System.out.println("Error: No asset found with ID " + id);
                return;
            }

            if (asset.getStatus() == AssetStatus.BORROWED) {
                System.out.println("Warning: Cannot delete an asset that is currently borrowed.");
                return;
            }

            System.out.print("Are you sure you want to delete '" + asset.getName() + "'? (Y/N): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.equalsIgnoreCase("Y")) {
                assetService.deleteAsset(id);
                System.out.println("\nSuccess: Asset deleted successfully!");
            } else {
                System.out.println("Delete action cancelled.");
            }
        } catch (ValidationException | DatabaseException e) {
            System.out.println("\nError deleting asset: " + e.getMessage());
        }
    }

    private static void adminSearchAssets() {
        System.out.println("\n--- SEARCH ASSETS ---");
        System.out.print("Enter name, category, or serial number query: ");
        String query = scanner.nextLine().trim();
        try {
            List<Asset> results = assetService.searchAssets(query);
            System.out.println("\nSearch results for '" + query + "':");
            displayAssetTable(results);
        } catch (DatabaseException e) {
            System.out.println("Error searching assets: " + e.getMessage());
        }
    }

    private static void adminSortAssets() {
        System.out.println("\n--- SORT ASSETS ---");
        System.out.println("Sort by:");
        System.out.println("1. Name");
        System.out.println("2. Price");
        System.out.println("3. Purchase Date");
        System.out.print("Select sorting parameter (1-3): ");
        String sortOption = scanner.nextLine().trim();
        
        String sortBy = null;
        if (sortOption.equals("1")) sortBy = "name";
        else if (sortOption.equals("2")) sortBy = "price";
        else if (sortOption.equals("3")) sortBy = "date";
        
        if (sortBy == null) {
            System.out.println("Invalid sorting selection.");
            return;
        }

        try {
            List<Asset> sortedList = assetService.getSortedAssets(sortBy);
            System.out.println("\nSorted assets by " + sortBy.toUpperCase() + ":");
            displayAssetTable(sortedList);
        } catch (DatabaseException e) {
            System.out.println("Error sorting assets: " + e.getMessage());
        }
    }

    private static void adminRegisterEmployee() {
        System.out.println("\n--- REGISTER NEW EMPLOYEE ---");
        try {
            System.out.print("Employee Full Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Email Address: ");
            String email = scanner.nextLine().trim();
            System.out.print("Department Name: ");
            String department = scanner.nextLine().trim();
            System.out.print("Password (at least 4 characters): ");
            String password = scanner.nextLine().trim();

            Employee employee = new Employee(0, name, email, department, password);
            employeeService.register(employee);
            System.out.println("\nSuccess: Employee registered successfully with ID: " + employee.getId());
        } catch (ValidationException | DatabaseException e) {
            System.out.println("\nError registering employee: " + e.getMessage());
        }
    }

    private static void adminViewAllEmployees() {
        System.out.println("\n--- SYSTEM REGISTERED EMPLOYEES ---");
        try {
            List<Employee> list = employeeService.getAllEmployees();
            ConsoleTable table = new ConsoleTable();
            table.setHeaders("ID", "Name", "Email Address", "Department");
            for (Employee emp : list) {
                table.addRow(
                    String.valueOf(emp.getId()),
                    emp.getName(),
                    emp.getEmail(),
                    emp.getDepartment()
                );
            }
            table.print();
        } catch (DatabaseException e) {
            System.out.println("Error fetching employee roster: " + e.getMessage());
        }
    }

    private static void adminViewTransactions() {
        System.out.println("\n--- MASTER TRANSACTION LOG ---");
        try {
            List<Transaction> list = transactionService.getAllTransactions();
            displayTransactionTable(list);
        } catch (DatabaseException e) {
            System.out.println("Error retrieving transaction history: " + e.getMessage());
        }
    }

    private static void adminGenerateReport() {
        System.out.println("\n--- SYSTEM SUMMARY REPORT ---");
        try {
            Map<String, Object> report = reportService.generateSummaryReport();
            
            System.out.println("\n[1] Overall Assets Metrics");
            System.out.println("------------------------------------------");
            System.out.printf("Total Registered Assets: %d\n", report.get("totalAssets"));
            System.out.printf("Available Assets Count:  %d\n", report.get("availableCount"));
            System.out.printf("Borrowed Assets Count:   %d\n", report.get("borrowedCount"));
            System.out.printf("Maintenance Assets Count: %d\n", report.get("maintenanceCount"));
            System.out.printf("Total Asset Valuation:   $%,.2f\n", report.get("totalValuation"));
            System.out.printf("Average Asset Price:     $%,.2f\n", report.get("averageValuation"));
            System.out.println("------------------------------------------");

            System.out.println("\n[2] Category Distribution");
            @SuppressWarnings("unchecked")
            Map<String, Integer> categoryDistribution = (Map<String, Integer>) report.get("categoryDistribution");
            
            ConsoleTable table = new ConsoleTable();
            table.setHeaders("Category Name", "Item Count");
            for (Map.Entry<String, Integer> entry : categoryDistribution.entrySet()) {
                table.addRow(entry.getKey(), String.valueOf(entry.getValue()));
            }
            table.print();

        } catch (DatabaseException e) {
            System.out.println("Error generating system reports: " + e.getMessage());
        }
    }

    // ==========================================
    // EMPLOYEE PORTAL
    // ==========================================

    private static void runEmployeeMenu(Employee emp) {
        boolean inEmployeeDashboard = true;
        while (inEmployeeDashboard) {
            System.out.println("\n=================================");
            System.out.println("      EMPLOYEE PORTAL            ");
            System.out.println("=================================");
            System.out.println("1. View Available Assets");
            System.out.println("2. Borrow an Asset");
            System.out.println("3. Return a Borrowed Asset");
            System.out.println("4. View My Transaction History");
            System.out.println("5. Logout");
            System.out.print("Select an option: ");

            String option = scanner.nextLine().trim();
            try {
                switch (option) {
                    case "1":
                        employeeViewAvailableAssets();
                        break;
                    case "2":
                        employeeBorrowAsset(emp);
                        break;
                    case "3":
                        employeeReturnAsset(emp);
                        break;
                    case "4":
                        employeeViewPersonalTransactions(emp);
                        break;
                    case "5":
                        inEmployeeDashboard = false;
                        System.out.println("Logged out from Employee Portal successfully.");
                        break;
                    default:
                        System.out.println("Invalid option. Please enter 1-5.");
                }
            } catch (Exception e) {
                System.out.println("\nAn unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private static void employeeViewAvailableAssets() {
        System.out.println("\n--- AVAILABLE SYSTEM ASSETS ---");
        try {
            List<Asset> all = assetService.getAllAssets();
            // Filter list in memory to show available assets
            List<Asset> availableOnly = all.stream()
                .filter(a -> a.getStatus() == AssetStatus.AVAILABLE)
                .toList();
            displayAssetTable(availableOnly);
        } catch (DatabaseException e) {
            System.out.println("Error fetching assets: " + e.getMessage());
        }
    }

    private static void employeeBorrowAsset(Employee emp) {
        System.out.println("\n--- BORROW AN ASSET ---");
        try {
            System.out.print("Enter the ID of the Asset to borrow: ");
            int assetId = InputValidator.parseInt(scanner.nextLine().trim(), "Asset ID");

            transactionService.borrowAsset(assetId, emp.getId());
            System.out.println("\nSuccess: Asset borrowed successfully! Please collect the item.");
        } catch (ValidationException | TransactionException | DatabaseException e) {
            System.out.println("\nError borrowing asset: " + e.getMessage());
        }
    }

    private static void employeeReturnAsset(Employee emp) {
        System.out.println("\n--- RETURN AN ASSET ---");
        try {
            System.out.print("Enter the ID of the Asset to return: ");
            int assetId = InputValidator.parseInt(scanner.nextLine().trim(), "Asset ID");

            // Returns the asset and enforces ownership constraint (matching current logged-in employee)
            transactionService.returnAsset(assetId, emp.getId());
            System.out.println("\nSuccess: Asset returned successfully!");
        } catch (ValidationException | TransactionException | DatabaseException e) {
            System.out.println("\nError returning asset: " + e.getMessage());
        }
    }

    private static void employeeViewPersonalTransactions(Employee emp) {
        System.out.println("\n--- MY BORROWING LOGS ---");
        try {
            List<Transaction> myLogs = transactionService.getTransactionsByEmployee(emp.getId());
            displayTransactionTable(myLogs);
        } catch (DatabaseException e) {
            System.out.println("Error retrieving personal log: " + e.getMessage());
        }
    }

    // ==========================================
    // LAYOUT RENDERING HELPERS
    // ==========================================

    private static void displayAssetTable(List<Asset> list) {
        ConsoleTable table = new ConsoleTable();
        table.setHeaders("Asset ID", "Asset Name", "Category", "Serial Number", "Price ($)", "Purchase Date", "Status");
        for (Asset a : list) {
            table.addRow(
                String.valueOf(a.getId()),
                a.getName(),
                a.getCategory(),
                a.getSerialNumber(),
                String.format("%.2f", a.getPrice()),
                a.getPurchaseDate().toString(),
                a.getStatus().name()
            );
        }
        table.print();
    }

    private static void displayTransactionTable(List<Transaction> list) {
        ConsoleTable table = new ConsoleTable();
        table.setHeaders("Transaction ID", "Asset ID", "Employee ID", "Borrow Date", "Return Date", "Transaction Status");
        for (Transaction t : list) {
            String returnDateStr = (t.getReturnDate() != null) ? t.getReturnDate().toString() : "Not Returned Yet";
            table.addRow(
                String.valueOf(t.getId()),
                String.valueOf(t.getAssetId()),
                String.valueOf(t.getEmployeeId()),
                t.getBorrowDate().toString(),
                returnDateStr,
                t.getStatus().name()
            );
        }
        table.print();
    }
}
