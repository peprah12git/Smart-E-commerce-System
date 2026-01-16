package com.ecommerce.dao;

import java.util.List;

import com.ecommerce.db.Order;

/**
 * OrderDao Interface
 * Defines database operations for Order entity
 */
public interface OrderDao {
    Order getOrderById(int orderId);
    List<Order> getAllOrders();
    List<Order> getOrdersByUser(int userId);
    int createOrder(Order order);
    boolean updateOrder(Order order);
    boolean deleteOrder(int orderId);
}
