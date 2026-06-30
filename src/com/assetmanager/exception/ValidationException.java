package com.assetmanager.exception;

/**
 * Custom exception representing failures during user input or business entity validation.
 */
public class ValidationException extends AssetManagementException {
    public ValidationException(String message) {
        super(message);
    }
}
