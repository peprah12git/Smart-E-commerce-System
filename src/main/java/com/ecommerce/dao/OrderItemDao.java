package com.ecommerce.dao;

import java.util.List;

import com.ecommerce.db.OrderItem;

/**
 * OrderItemDao Interface
 * Defines database operations for OrderItem entity
 */
public interface OrderItemDao {
    OrderItem getOrderItemById(int orderItemId);
    List<OrderItem> getOrderItemsByOrder(int orderId);
    int createOrderItem(OrderItem orderItem);
    boolean updateOrderItem(OrderItem orderItem);
    boolean deleteOrderItem(int orderItemId);
}
