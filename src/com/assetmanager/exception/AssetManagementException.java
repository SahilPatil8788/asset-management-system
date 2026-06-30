package com.assetmanager.exception;

/**
 * Base exception class for the Asset Management System.
 */
public class AssetManagementException extends Exception {
    public AssetManagementException(String message) {
        super(message);
    }

    public AssetManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
