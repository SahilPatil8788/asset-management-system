package com.assetmanager.dao;

import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Employee;
import java.util.List;

/**
 * Data Access Object Interface for Employee entities.
 */
public interface EmployeeDAO {
    void add(Employee employee) throws DatabaseException;
    Employee getById(int id) throws DatabaseException;
    Employee getByEmail(String email) throws DatabaseException;
    List<Employee> getAll() throws DatabaseException;
    void update(Employee employee) throws DatabaseException;
    void delete(int id) throws DatabaseException;
}
