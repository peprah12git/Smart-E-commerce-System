package com.ecommerce.service;

import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.OrderItemDAO;
import com.ecommerce.models.CartItem;
import com.ecommerce.models.Order;
import com.ecommerce.models.OrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Order operations
 * Implements business logic for order processing and management
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class OrderService {
    
    private static OrderService instance;
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private InventoryService inventoryService;
    
    // Tax rate (10%)
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    
    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.inventoryService = new InventoryService();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }
    
    // ============ ORDER CREATION ============
    
    /**
     * Create order from cart items
     * @param userId User placing the order
     * @param cartItems Items from shopping cart
     * @return OrderResult with success status and order details
     */
    public OrderResult createOrder(int userId, List<CartItem> cartItems) {
        // Validation
        if (cartItems == null || cartItems.isEmpty()) {
            return new OrderResult(false, "Cart is empty", null);
        }
        
        // Calculate totals
        BigDecimal subtotal = calculateSubtotal(cartItems);
        BigDecimal tax = calculateTax(subtotal);
        BigDecimal total = subtotal.add(tax);
        
        // Verify stock availability
        for (CartItem item : cartItems) {
            if (!inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())) {
                return new OrderResult(false, 
                        "Insufficient stock for: " + item.getProductName(), null);
            }
        }
        
        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("Pending");
        order.setTotalAmount(total);
        
        if (!orderDAO.addOrder(order)) {
            return new OrderResult(false, "Failed to create order", null);
        }
        
        // Create order items and reduce inventory
        for (CartItem item : cartItems) {
            boolean itemAdded = orderItemDAO.addOrderItem(
                    order.getOrderId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getPrice()
            );
            
            if (!itemAdded) {
                // Rollback would be needed in production
                return new OrderResult(false, "Failed to add order items", null);
            }
            
            // Reduce inventory
            inventoryService.reduceStock(item.getProductId(), item.getQuantity());
        }
        
        System.out.println("[OrderService] Order created: #" + order.getOrderId() + 
                         " Total: $" + total);
        
        return new OrderResult(true, "Order placed successfully", order);
    }
    
    /**
     * Result class for order operations
     */
    public static class OrderResult {
        private final boolean success;
        private final String message;
        private final Order order;
        
        public OrderResult(boolean success, String message, Order order) {
            this.success = success;
            this.message = message;
            this.order = order;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Order getOrder() { return order; }
    }
    
    // ============ CALCULATIONS ============
    
    /**
     * Calculate subtotal from cart items
     */
    public BigDecimal calculateSubtotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate tax amount
     */
    public BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate total (subtotal + tax)
     */
    public BigDecimal calculateTotal(BigDecimal subtotal) {
        return subtotal.add(calculateTax(subtotal));
    }
    
    /**
     * Get tax rate
     */
    public BigDecimal getTaxRate() {
        return TAX_RATE;
    }
    
    // ============ ORDER RETRIEVAL ============
    
    /**
     * Get all orders
     */
    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }
    
    /**
     * Get order by ID
     */
    public Order getOrderById(int orderId) {
        return orderDAO.getOrderById(orderId);
    }
    
    /**
     * Get orders for specific user
     */
    public List<Order> getOrdersByUserId(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }
    
    /**
     * Get order items for an order
     */
    public List<OrderItem> getOrderItems(int orderId) {
        return orderItemDAO.getOrderItemsByOrderId(orderId);
    }
    
    /**
     * Get order with items (full details)
     */
    public Order getOrderWithItems(int orderId) {
        Order order = orderDAO.getOrderById(orderId);
        if (order != null) {
            List<OrderItem> items = orderItemDAO.getOrderItemsByOrderId(orderId);
            order.setOrderItems(items);
        }
        return order;
    }
    
    // ============ ORDER STATUS MANAGEMENT ============
    
    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, String newStatus) {
        if (!isValidStatus(newStatus)) {
            System.err.println("[OrderService] Invalid status: " + newStatus);
            return false;
        }
        
        boolean updated = orderDAO.updateOrderStatus(orderId, newStatus);
        if (updated) {
            System.out.println("[OrderService] Order #" + orderId + " status updated to: " + newStatus);
        }
        return updated;
    }
    
    /**
     * Validate order status
     */
    public boolean isValidStatus(String status) {
        return status != null && (
                status.equals("Pending") ||
                status.equals("Processing") ||
                status.equals("Shipped") ||
                status.equals("Delivered") ||
                status.equals("Cancelled")
        );
    }
    
    /**
     * Get available status options
     */
    public List<String> getStatusOptions() {
        return List.of("Pending", "Processing", "Shipped", "Delivered", "Cancelled");
    }
    
    /**
     * Cancel order (if allowed)
     */
    public boolean cancelOrder(int orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            return false;
        }
        
        // Only allow cancellation of pending/processing orders
        if (!order.getStatus().equals("Pending") && !order.getStatus().equals("Processing")) {
            System.err.println("[OrderService] Cannot cancel order in status: " + order.getStatus());
            return false;
        }
        
        // Restore inventory
        List<OrderItem> items = getOrderItems(orderId);
        for (OrderItem item : items) {
            inventoryService.addStock(item.getProductId(), item.getQuantity());
        }
        
        return updateOrderStatus(orderId, "Cancelled");
    }
    
    // ============ FILTERING & SORTING ============
    
    /**
     * Get orders by status
     */
    public List<Order> getOrdersByStatus(String status) {
        return getAllOrders().stream()
                .filter(o -> status.equals(o.getStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get pending orders
     */
    public List<Order> getPendingOrders() {
        return getOrdersByStatus("Pending");
    }
    
    /**
     * Sort orders by date
     */
    public List<Order> sortByDate(boolean newest) {
        List<Order> orders = new ArrayList<>(getAllOrders());
        if (newest) {
            orders.sort(Comparator.comparing(Order::getOrderDate).reversed());
        } else {
            orders.sort(Comparator.comparing(Order::getOrderDate));
        }
        return orders;
    }
    
    /**
     * Sort orders by total amount
     */
    public List<Order> sortByTotal(boolean descending) {
        List<Order> orders = new ArrayList<>(getAllOrders());
        if (descending) {
            orders.sort(Comparator.comparing(Order::getTotalAmount).reversed());
        } else {
            orders.sort(Comparator.comparing(Order::getTotalAmount));
        }
        return orders;
    }
    
    // ============ STATISTICS ============
    
    /**
     * Get total revenue
     */
    public BigDecimal getTotalRevenue() {
        return getAllOrders().stream()
                .filter(o -> !o.getStatus().equals("Cancelled"))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get order count
     */
    public int getOrderCount() {
        return getAllOrders().size();
    }
    
    /**
     * Get user's order count
     */
    public int getUserOrderCount(int userId) {
        return getOrdersByUserId(userId).size();
    }
}
