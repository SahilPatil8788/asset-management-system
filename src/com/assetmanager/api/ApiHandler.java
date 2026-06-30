package com.assetmanager.api;

import com.assetmanager.exception.*;
import com.assetmanager.model.*;
import com.assetmanager.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * REST API handler that exposes endpoints to manage authentication, assets, employees, transactions, and reports.
 */
public class ApiHandler implements HttpHandler {
    private final AdminService adminService = new AdminService();
    private final EmployeeService employeeService = new EmployeeService();
    private final AssetService assetService = new AssetService();
    private final TransactionService transactionService = new TransactionService();
    private final ReportService reportService = new ReportService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // Enable CORS for preflight and standard requests
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            if (path.equals("/api/login") && "POST".equalsIgnoreCase(method)) {
                handleLogin(exchange);
            } else if (path.equals("/api/assets")) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleGetAssets(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handleAddAsset(exchange);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    handleUpdateAsset(exchange);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    handleDeleteAsset(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
            } else if (path.equals("/api/employees")) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleGetEmployees(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handleRegisterEmployee(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
            } else if (path.equals("/api/transactions") && "GET".equalsIgnoreCase(method)) {
                handleGetTransactions(exchange);
            } else if (path.equals("/api/transactions/borrow") && "POST".equalsIgnoreCase(method)) {
                handleBorrowAsset(exchange);
            } else if (path.equals("/api/transactions/return") && "POST".equalsIgnoreCase(method)) {
                handleReturnAsset(exchange);
            } else if (path.equals("/api/reports") && "GET".equalsIgnoreCase(method)) {
                handleGetReports(exchange);
            } else {
                sendError(exchange, 404, "Endpoint Not Found");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    // ==========================================
    // ENDPOINT HANDLERS
    // ==========================================

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        String username = getJsonValue(body, "username");
        String email = getJsonValue(body, "email");
        String password = getJsonValue(body, "password");
        String role = getJsonValue(body, "role");

        try {
            if ("admin".equalsIgnoreCase(role)) {
                Admin admin = adminService.login(username, password);
                String json = String.format("{\"success\":true,\"role\":\"admin\",\"user\":{\"username\":\"%s\",\"email\":\"%s\"}}", 
                        escapeJson(admin.getUsername()), escapeJson(admin.getEmail()));
                sendJsonResponse(exchange, 200, json);
            } else {
                Employee emp = employeeService.login(email, password);
                String json = String.format("{\"success\":true,\"role\":\"employee\",\"user\":{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"department\":\"%s\"}}", 
                        emp.getId(), escapeJson(emp.getName()), escapeJson(emp.getEmail()), escapeJson(emp.getDepartment()));
                sendJsonResponse(exchange, 200, json);
            }
        } catch (AuthenticationException e) {
            sendError(exchange, 401, e.getMessage());
        } catch (DatabaseException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        }
    }

    private void handleGetAssets(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String search = queryParams.getOrDefault("search", "");
        String sort = queryParams.getOrDefault("sort", "");

        try {
            List<Asset> assets;
            if (!search.isEmpty()) {
                assets = assetService.searchAssets(search);
            } else if (!sort.isEmpty()) {
                assets = assetService.getSortedAssets(sort);
            } else {
                assets = assetService.getAllAssets();
            }

            String jsonList = assets.stream().map(this::assetToJson).collect(Collectors.joining(","));
            sendJsonResponse(exchange, 200, "[" + jsonList + "]");
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        }
    }

    private void handleAddAsset(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            String name = getJsonValue(body, "name");
            String category = getJsonValue(body, "category");
            String serialNumber = getJsonValue(body, "serialNumber");
            double price = Double.parseDouble(getJsonValue(body, "price"));
            LocalDate purchaseDate = LocalDate.parse(getJsonValue(body, "purchaseDate"));

            Asset asset = new Asset(0, name, category, serialNumber, AssetStatus.AVAILABLE, price, purchaseDate);
            assetService.addAsset(asset);
            sendJsonResponse(exchange, 201, "{\"success\":true,\"message\":\"Asset added successfully\",\"id\":" + asset.getId() + "}");
        } catch (ValidationException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Invalid JSON structure or data formats.");
        }
    }

    private void handleUpdateAsset(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            int id = Integer.parseInt(getJsonValue(body, "id"));
            Asset asset = assetService.getAssetById(id);
            if (asset == null) {
                sendError(exchange, 404, "Asset with ID " + id + " not found.");
                return;
            }

            String name = getJsonValue(body, "name");
            if (!name.isEmpty()) asset.setName(name);

            String category = getJsonValue(body, "category");
            if (!category.isEmpty()) asset.setCategory(category);

            String serialNumber = getJsonValue(body, "serialNumber");
            if (!serialNumber.isEmpty()) asset.setSerialNumber(serialNumber);

            String priceStr = getJsonValue(body, "price");
            if (!priceStr.isEmpty()) asset.setPrice(Double.parseDouble(priceStr));

            String dateStr = getJsonValue(body, "purchaseDate");
            if (!dateStr.isEmpty()) asset.setPurchaseDate(LocalDate.parse(dateStr));

            String statusStr = getJsonValue(body, "status");
            if (!statusStr.isEmpty()) asset.setStatus(AssetStatus.fromString(statusStr));

            assetService.updateAsset(asset);
            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Asset updated successfully\"}");
        } catch (ValidationException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Invalid update request payload: " + e.getMessage());
        }
    }

    private void handleDeleteAsset(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String idStr = queryParams.get("id");
        if (idStr == null || idStr.isEmpty()) {
            sendError(exchange, 400, "Missing required query parameter: id");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Asset asset = assetService.getAssetById(id);
            if (asset == null) {
                sendError(exchange, 404, "Asset not found.");
                return;
            }
            if (asset.getStatus() == AssetStatus.BORROWED) {
                sendError(exchange, 400, "Cannot delete an asset that is currently borrowed.");
                return;
            }

            assetService.deleteAsset(id);
            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Asset deleted successfully\"}");
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid ID format.");
        }
    }

    private void handleGetEmployees(HttpExchange exchange) throws IOException {
        try {
            List<Employee> list = employeeService.getAllEmployees();
            String jsonList = list.stream().map(this::employeeToJson).collect(Collectors.joining(","));
            sendJsonResponse(exchange, 200, "[" + jsonList + "]");
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        }
    }

    private void handleRegisterEmployee(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            String name = getJsonValue(body, "name");
            String email = getJsonValue(body, "email");
            String department = getJsonValue(body, "department");
            String password = getJsonValue(body, "password");

            Employee emp = new Employee(0, name, email, department, password);
            employeeService.register(emp);
            sendJsonResponse(exchange, 201, "{\"success\":true,\"message\":\"Employee registered successfully\",\"id\":" + emp.getId() + "}");
        } catch (ValidationException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Invalid data structure.");
        }
    }

    private void handleGetTransactions(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String employeeIdStr = queryParams.get("employeeId");

        try {
            List<Transaction> transactions;
            if (employeeIdStr != null && !employeeIdStr.isEmpty()) {
                int empId = Integer.parseInt(employeeIdStr);
                transactions = transactionService.getTransactionsByEmployee(empId);
            } else {
                transactions = transactionService.getAllTransactions();
            }

            String jsonList = transactions.stream().map(this::transactionToJson).collect(Collectors.joining(","));
            sendJsonResponse(exchange, 200, "[" + jsonList + "]");
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid employee ID format.");
        }
    }

    private void handleBorrowAsset(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            int assetId = Integer.parseInt(getJsonValue(body, "assetId"));
            int employeeId = Integer.parseInt(getJsonValue(body, "employeeId"));

            transactionService.borrowAsset(assetId, employeeId);
            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Asset borrowed successfully\"}");
        } catch (TransactionException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Invalid request payload.");
        }
    }

    private void handleReturnAsset(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            int assetId = Integer.parseInt(getJsonValue(body, "assetId"));
            String empIdStr = getJsonValue(body, "employeeId");
            Integer employeeId = (empIdStr == null || empIdStr.isEmpty()) ? null : Integer.parseInt(empIdStr);

            transactionService.returnAsset(assetId, employeeId);
            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Asset returned successfully\"}");
        } catch (TransactionException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Invalid request payload.");
        }
    }

    private void handleGetReports(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> report = reportService.generateSummaryReport();
            
            // Format category breakdown manually to JSON
            @SuppressWarnings("unchecked")
            Map<String, Integer> breakdown = (Map<String, Integer>) report.get("categoryDistribution");
            String breakdownJson = breakdown.entrySet().stream()
                    .map(entry -> String.format("\"%s\":%d", escapeJson(entry.getKey()), entry.getValue()))
                    .collect(Collectors.joining(","));

            String json = String.format(
                "{\"totalAssets\":%d,\"availableCount\":%d,\"borrowedCount\":%d,\"maintenanceCount\":%d," +
                "\"totalValuation\":%.2f,\"averageValuation\":%.2f,\"categoryDistribution\":{%s}}",
                report.get("totalAssets"), report.get("availableCount"), report.get("borrowedCount"), report.get("maintenanceCount"),
                report.get("totalValuation"), report.get("averageValuation"), breakdownJson
            );

            sendJsonResponse(exchange, 200, json);
        } catch (DatabaseException e) {
            sendError(exchange, 500, e.getMessage());
        }
    }

    // ==========================================
    // JSON UTILITIES
    // ==========================================

    private String getJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        Matcher m = Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        String numPattern = "\"" + key + "\"\\s*:\\s*([^,\\}\\s]*)";
        m = Pattern.compile(numPattern).matcher(json);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }

    private String assetToJson(Asset a) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"serialNumber\":\"%s\",\"price\":%.2f,\"purchaseDate\":\"%s\",\"status\":\"%s\"}",
            a.getId(), escapeJson(a.getName()), escapeJson(a.getCategory()), escapeJson(a.getSerialNumber()),
            a.getPrice(), a.getPurchaseDate().toString(), a.getStatus().name()
        );
    }

    private String employeeToJson(Employee e) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"department\":\"%s\"}",
            e.getId(), escapeJson(e.getName()), escapeJson(e.getEmail()), escapeJson(e.getDepartment())
        );
    }

    private String transactionToJson(Transaction t) {
        String returnDateStr = t.getReturnDate() != null ? "\"" + t.getReturnDate().toString() + "\"" : "null";
        return String.format(
            "{\"id\":%d,\"assetId\":%d,\"employeeId\":%d,\"borrowDate\":\"%s\",\"returnDate\":%s,\"status\":\"%s\"}",
            t.getId(), t.getAssetId(), t.getEmployeeId(), t.getBorrowDate().toString(),
            returnDateStr, t.getStatus().name()
        );
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                params.put(entry[0], java.net.URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            } else {
                params.put(entry[0], "");
            }
        }
        return params;
    }

    private void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        String json = String.format("{\"success\":false,\"error\":\"%s\"}", escapeJson(message));
        sendJsonResponse(exchange, status, json);
    }
}
