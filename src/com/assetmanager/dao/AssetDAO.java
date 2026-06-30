package com.assetmanager.dao;

import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Asset;
import com.assetmanager.model.AssetStatus;
import java.util.List;

/**
 * Data Access Object Interface for Asset entities.
 */
public interface AssetDAO {
    void add(Asset asset) throws DatabaseException;
    Asset getById(int id) throws DatabaseException;
    Asset getBySerialNumber(String serialNumber) throws DatabaseException;
    List<Asset> getAll() throws DatabaseException;
    void update(Asset asset) throws DatabaseException;
    void delete(int id) throws DatabaseException;
    List<Asset> searchByNameOrCategory(String query) throws DatabaseException;
    void updateStatus(int assetId, AssetStatus status) throws DatabaseException;
}
