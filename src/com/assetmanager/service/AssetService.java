package com.assetmanager.service;

import com.assetmanager.dao.AssetDAO;
import com.assetmanager.dao.AssetDAOImpl;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.exception.ValidationException;
import com.assetmanager.model.Asset;
import com.assetmanager.model.AssetStatus;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Service layer class for Asset related business logic.
 */
public class AssetService {
    private final AssetDAO assetDAO;

    public AssetService() {
        this.assetDAO = new AssetDAOImpl();
    }

    public void addAsset(Asset asset) throws DatabaseException, ValidationException {
        validateAsset(asset);
        
        // Check for unique serial number
        Asset existing = assetDAO.getBySerialNumber(asset.getSerialNumber());
        if (existing != null) {
            throw new ValidationException("Asset with serial number '" + asset.getSerialNumber() + "' already exists.");
        }
        
        assetDAO.add(asset);
    }

    public Asset getAssetById(int id) throws DatabaseException {
        return assetDAO.getById(id);
    }

    public List<Asset> getAllAssets() throws DatabaseException {
        return assetDAO.getAll();
    }

    public void updateAsset(Asset asset) throws DatabaseException, ValidationException {
        validateAsset(asset);
        
        // Check serial number uniqueness
        Asset existing = assetDAO.getBySerialNumber(asset.getSerialNumber());
        if (existing != null && existing.getId() != asset.getId()) {
            throw new ValidationException("Serial number '" + asset.getSerialNumber() + "' is already assigned to another asset.");
        }
        
        assetDAO.update(asset);
    }

    public void deleteAsset(int id) throws DatabaseException {
        assetDAO.delete(id);
    }

    public List<Asset> searchAssets(String query) throws DatabaseException {
        if (query == null || query.trim().isEmpty()) {
            return assetDAO.getAll();
        }
        return assetDAO.searchByNameOrCategory(query.trim());
    }

    /**
     * Sorts assets in-memory using Java Collections and Comparators.
     * @param sortBy "name", "price", or "date"
     * @return Sorted list of assets
     */
    public List<Asset> getSortedAssets(String sortBy) throws DatabaseException {
        List<Asset> list = assetDAO.getAll();
        if (sortBy == null) {
            return list;
        }

        switch (sortBy.toLowerCase().trim()) {
            case "name":
                list.sort(Comparator.comparing(Asset::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "price":
                list.sort(Comparator.comparingDouble(Asset::getPrice));
                break;
            case "date":
                list.sort(Comparator.comparing(Asset::getPurchaseDate));
                break;
            default:
                // No sort, return as is
                break;
        }
        return list;
    }

    private void validateAsset(Asset asset) throws ValidationException {
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new ValidationException("Asset name cannot be empty.");
        }
        if (asset.getCategory() == null || asset.getCategory().trim().isEmpty()) {
            throw new ValidationException("Category cannot be empty.");
        }
        if (asset.getSerialNumber() == null || asset.getSerialNumber().trim().isEmpty()) {
            throw new ValidationException("Serial number cannot be empty.");
        }
        if (asset.getPrice() < 0) {
            throw new ValidationException("Price cannot be negative.");
        }
        if (asset.getPurchaseDate() == null || asset.getPurchaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Purchase date cannot be in the future.");
        }
        if (asset.getStatus() == null) {
            asset.setStatus(AssetStatus.AVAILABLE);
        }
    }
}
