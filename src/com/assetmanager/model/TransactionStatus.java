package com.assetmanager.model;

/**
 * Enum representing the current status of a Transaction.
 */
public enum TransactionStatus {
    BORROWED,
    RETURNED;

    public static TransactionStatus fromString(String text) {
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No transaction status matched for value: " + text);
    }
}
