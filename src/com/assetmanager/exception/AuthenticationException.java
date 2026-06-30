package com.assetmanager.exception;

/**
 * Custom exception representing failures during user authentication (login, privilege validation).
 */
public class AuthenticationException extends AssetManagementException {
    public AuthenticationException(String message) {
        super(message);
    }
}
