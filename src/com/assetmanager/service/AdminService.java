package com.assetmanager.service;

import com.assetmanager.dao.AdminDAO;
import com.assetmanager.dao.AdminDAOImpl;
import com.assetmanager.exception.AuthenticationException;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Admin;

/**
 * Service layer class for Admin related business logic.
 */
public class AdminService {
    private final AdminDAO adminDAO;

    public AdminService() {
        this.adminDAO = new AdminDAOImpl();
    }

    /**
     * Authenticates Admin login credentials.
     * @param username Admin username
     * @param password Admin password
     * @return Authenticated Admin object
     * @throws AuthenticationException on invalid credentials
     * @throws DatabaseException on database errors
     */
    public Admin login(String username, String password) throws AuthenticationException, DatabaseException {
        Admin admin = adminDAO.getByUsername(username);
        if (admin == null || !admin.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid Admin username or password.");
        }
        return admin;
    }
}
