package com.assetmanager.exception;

/**
 * Custom exception representing errors in database interaction or connectivity.
 */
public class DatabaseException extends AssetManagementException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
