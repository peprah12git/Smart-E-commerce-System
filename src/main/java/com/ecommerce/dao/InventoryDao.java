package com.ecommerce.dao;

import com.ecommerce.db.Inventory;

/**
 * InventoryDao Interface
 * Defines database operations for Inventory entity
 */
public interface InventoryDao {
    Inventory getInventoryByProductId(int productId);
    int createInventory(Inventory inventory);
    boolean updateInventory(Inventory inventory);
    boolean deleteInventory(int inventoryId);
    boolean updateQuantity(int productId, int newQuantity);
}
