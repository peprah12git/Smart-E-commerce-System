package com.ecommerce.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.OrderItemDAO;
import com.ecommerce.models.Order;
import com.ecommerce.models.OrderItem;

public class OrderService {
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private List<Order> ordersCache;
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 180000; // 3 minutes

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.ordersCache = new ArrayList<>();
        this.lastCacheUpdate = 0;
    }

    public boolean createOrder(Order order) {
        boolean success = orderDAO.addOrder(order);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public boolean addOrderItem(OrderItem item) {
        boolean success = orderItemDAO.addOrderItem(item);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public List<Order> getAllOrders() {
        long now = System.currentTimeMillis();

        if (!ordersCache.isEmpty() && (now - lastCacheUpdate) < CACHE_VALIDITY) {
            System.out.println("✓ Orders from cache");
            return new ArrayList<>(ordersCache);
        }

        System.out.println("✗ Fetching orders from database");
        ordersCache = orderDAO.getAllOrders();
        lastCacheUpdate = now;
        return new ArrayList<>(ordersCache);
    }

    public Order getOrderById(int id) {
        return orderDAO.getOrderById(id);
    }

    public List<Order> getOrdersByUserId(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }

    public List<OrderItem> getOrderItems(int orderId) {
        return orderItemDAO.getOrderItemsByOrderId(orderId);
    }

    public boolean updateOrderStatus(int orderId, String status) {
        boolean success = orderDAO.updateOrderStatus(orderId, status);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public boolean deleteOrder(int id) {
        boolean success = orderDAO.deleteOrder(id);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    // Business logic: Filter orders by status
    public List<Order> getOrdersByStatus(String status) {
        return getAllOrders().stream()
                .filter(o -> o.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    // Business logic: Get recent orders
    public List<Order> getRecentOrders(int limit) {
        List<Order> orders = getAllOrders();
        return orders.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Business logic: Calculate total revenue
    public double calculateTotalRevenue() {
        return getAllOrders().stream()
                .filter(o -> !o.getStatus().equals("cancelled"))
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .sum();
    }

    // Sorting
    public List<Order> sortByDate(boolean ascending) {
        List<Order> orders = getAllOrders();
        if (ascending) {
            orders.sort(Comparator.comparing(Order::getOrderDate));
        } else {
            orders.sort(Comparator.comparing(Order::getOrderDate).reversed());
        }
        return orders;
    }

    public List<Order> sortByAmount(boolean ascending) {
        List<Order> orders = getAllOrders();
        if (ascending) {
            orders.sort(Comparator.comparing(Order::getTotalAmount));
        } else {
            orders.sort(Comparator.comparing(Order::getTotalAmount).reversed());
        }
        return orders;
    }

    private void invalidateCache() {
        ordersCache.clear();
        lastCacheUpdate = 0;
    }

    /**
     * Create an order with items in a single database transaction.
     * Ensures consistency: either all inserts succeed or none do.
     */
    public boolean createOrderWithItems(Order order, List<OrderItem> items) {
        java.sql.Connection conn = com.ecommerce.config.DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            // Create order
            boolean orderCreated = orderDAO.addOrder(order);
            if (!orderCreated) {
                conn.rollback();
                return false;
            }

            // Add items
            for (OrderItem item : items) {
                item.setOrderId(order.getOrderId());
                boolean itemCreated = orderItemDAO.addOrderItem(item);
                if (!itemCreated) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            invalidateCache();
            return true;
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ignored) {}
            System.err.println("✗ Transaction failed: " + e.getMessage());
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }
}
