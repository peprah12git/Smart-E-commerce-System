package com.ecommerce.db;

import java.sql.Timestamp;

/**
 * Inventory Entity Class
 * Represents an inventory record in the Inventory table
 */
public class Inventory {
    private int inventoryId;
    private int productId;
    private int quantityAvailable;
    private Timestamp lastUpdated;

    // Constructors
    public Inventory() {
    }

    public Inventory(int productId, int quantityAvailable) {
        this.productId = productId;
        this.quantityAvailable = quantityAvailable;
    }

    public Inventory(int inventoryId, int productId, int quantityAvailable, Timestamp lastUpdated) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.quantityAvailable = quantityAvailable;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId=" + inventoryId +
                ", productId=" + productId +
                ", quantityAvailable=" + quantityAvailable +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
