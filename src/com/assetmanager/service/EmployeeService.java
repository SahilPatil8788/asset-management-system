package com.assetmanager.service;

import com.assetmanager.dao.EmployeeDAO;
import com.assetmanager.dao.EmployeeDAOImpl;
import com.assetmanager.exception.AuthenticationException;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.exception.ValidationException;
import com.assetmanager.model.Employee;
import com.assetmanager.util.InputValidator;

import java.util.List;

/**
 * Service layer class for Employee related business logic.
 */
public class EmployeeService {
    private final EmployeeDAO employeeDAO;

    public EmployeeService() {
        this.employeeDAO = new EmployeeDAOImpl();
    }

    /**
     * Authenticates Employee login credentials.
     */
    public Employee login(String email, String password) throws AuthenticationException, DatabaseException {
        Employee emp = employeeDAO.getByEmail(email);
        if (emp == null || !emp.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid Employee email or password.");
        }
        return emp;
    }

    /**
     * Registers a new employee with basic validations.
     */
    public void register(Employee employee) throws DatabaseException, ValidationException {
        validateEmployee(employee);
        
        // Check if email already exists
        Employee existing = employeeDAO.getByEmail(employee.getEmail());
        if (existing != null) {
            throw new ValidationException("Employee with email '" + employee.getEmail() + "' already exists.");
        }
        
        employeeDAO.add(employee);
    }

    public List<Employee> getAllEmployees() throws DatabaseException {
        return employeeDAO.getAll();
    }

    public Employee getEmployeeById(int id) throws DatabaseException {
        return employeeDAO.getById(id);
    }

    public void updateEmployee(Employee employee) throws DatabaseException, ValidationException {
        validateEmployee(employee);
        
        // Ensure email isn't stolen by another user
        Employee existing = employeeDAO.getByEmail(employee.getEmail());
        if (existing != null && existing.getId() != employee.getId()) {
            throw new ValidationException("Email '" + employee.getEmail() + "' is already in use by another employee.");
        }
        
        employeeDAO.update(employee);
    }

    public void deleteEmployee(int id) throws DatabaseException {
        employeeDAO.delete(id);
    }

    private void validateEmployee(Employee employee) throws ValidationException {
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            throw new ValidationException("Employee name cannot be empty.");
        }
        if (!InputValidator.isValidEmail(employee.getEmail())) {
            throw new ValidationException("Invalid email address format.");
        }
        if (employee.getDepartment() == null || employee.getDepartment().trim().isEmpty()) {
            throw new ValidationException("Department name cannot be empty.");
        }
        if (employee.getPassword() == null || employee.getPassword().length() < 4) {
            throw new ValidationException("Password must be at least 4 characters long.");
        }
    }
}
