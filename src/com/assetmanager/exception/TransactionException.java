package com.assetmanager.exception;

/**
 * Custom exception representing errors in borrowing/returning transaction workflows.
 */
public class TransactionException extends AssetManagementException {
    public TransactionException(String message) {
        super(message);
    }
}
