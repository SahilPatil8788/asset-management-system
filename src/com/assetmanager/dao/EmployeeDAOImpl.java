package com.assetmanager.dao;

import com.assetmanager.database.DatabaseConnection;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of the EmployeeDAO interface.
 */
public class EmployeeDAOImpl implements EmployeeDAO {

    @Override
    public void add(Employee employee) throws DatabaseException {
        String sql = "INSERT INTO Employee (name, email, department, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getEmail());
            pstmt.setString(3, employee.getDepartment());
            pstmt.setString(4, employee.getPassword());
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    employee.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add employee: " + employee.getName(), e);
        }
    }

    @Override
    public Employee getById(int id) throws DatabaseException {
        String sql = "SELECT id, name, email, department, password FROM Employee WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch employee by id: " + id, e);
        }
        return null;
    }

    @Override
    public Employee getByEmail(String email) throws DatabaseException {
        String sql = "SELECT id, name, email, department, password FROM Employee WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch employee by email: " + email, e);
        }
        return null;
    }

    @Override
    public List<Employee> getAll() throws DatabaseException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT id, name, email, department, password FROM Employee";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Employee(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("department"),
                    rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all employees", e);
        }
        return list;
    }

    @Override
    public void update(Employee employee) throws DatabaseException {
        String sql = "UPDATE Employee SET name = ?, email = ?, department = ?, password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getEmail());
            pstmt.setString(3, employee.getDepartment());
            pstmt.setString(4, employee.getPassword());
            pstmt.setInt(5, employee.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update employee: " + employee.getName(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM Employee WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete employee with id: " + id, e);
        }
    }
}
