package com.assetmanager.dao;

import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Admin;

/**
 * Data Access Object Interface for Admin entities.
 */
public interface AdminDAO {
    /**
     * Retrieve an Admin by their username.
     * @param username The username to look up
     * @return Admin object, or null if not found
     * @throws DatabaseException on database error
     */
    Admin getByUsername(String username) throws DatabaseException;
}
