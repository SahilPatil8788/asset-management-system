package com.assetmanager.service;

import com.assetmanager.dao.AssetDAO;
import com.assetmanager.dao.AssetDAOImpl;
import com.assetmanager.exception.DatabaseException;
import com.assetmanager.model.Asset;
import com.assetmanager.model.AssetStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer class that aggregates database values for status reports.
 */
public class ReportService {
    private final AssetDAO assetDAO;

    public ReportService() {
        this.assetDAO = new AssetDAOImpl();
    }

    /**
     * Aggregates asset data into a report map of key statistics.
     */
    public Map<String, Object> generateSummaryReport() throws DatabaseException {
        List<Asset> assets = assetDAO.getAll();
        
        int totalAssets = assets.size();
        int availableCount = 0;
        int borrowedCount = 0;
        int maintenanceCount = 0;
        double totalValuation = 0.0;
        
        Map<String, Integer> categoryDistribution = new HashMap<>();

        for (Asset asset : assets) {
            totalValuation += asset.getPrice();
            
            // Count categories
            String category = asset.getCategory();
            categoryDistribution.put(category, categoryDistribution.getOrDefault(category, 0) + 1);
            
            // Count statuses
            if (asset.getStatus() == AssetStatus.AVAILABLE) {
                availableCount++;
            } else if (asset.getStatus() == AssetStatus.BORROWED) {
                borrowedCount++;
            } else if (asset.getStatus() == AssetStatus.MAINTENANCE) {
                maintenanceCount++;
            }
        }

        double averageValuation = totalAssets > 0 ? (totalValuation / totalAssets) : 0.0;

        Map<String, Object> report = new HashMap<>();
        report.put("totalAssets", totalAssets);
        report.put("availableCount", availableCount);
        report.put("borrowedCount", borrowedCount);
        report.put("maintenanceCount", maintenanceCount);
        report.put("totalValuation", totalValuation);
        report.put("averageValuation", averageValuation);
        report.put("categoryDistribution", categoryDistribution);

        return report;
    }
}
