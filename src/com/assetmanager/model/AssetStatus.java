package com.assetmanager.model;

/**
 * Enum representing the current status of an Asset.
 */
public enum AssetStatus {
    AVAILABLE,
    BORROWED,
    MAINTENANCE;

    public static AssetStatus fromString(String text) {
        for (AssetStatus status : AssetStatus.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No asset status matched for value: " + text);
    }
}
