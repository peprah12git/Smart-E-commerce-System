package com.ecommerce.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.ecommerce.models.CartItem;
import com.ecommerce.models.Order;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for Checkout view
 * Uses OrderService for order processing
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class CheckoutController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField cityField;
    @FXML private TextField postalCodeField;
    @FXML private TextArea orderNotesField;
    @FXML private TableView<CartItem> summaryTable;
    @FXML private TableColumn<CartItem, String> productColumn;
    @FXML private TableColumn<CartItem, Integer> qtyColumn;
    @FXML private TableColumn<CartItem, BigDecimal> priceColumn;
    @FXML private Label checkoutSubtotal;
    @FXML private Label checkoutTax;
    @FXML private Label checkoutTotal;

    private ClientViewController clientViewController;
    
    // Service (not DAOs)
    private OrderService orderService;

    @FXML
    public void initialize() {
        // Use service instead of DAOs
        orderService = OrderService.getInstance();
        setupTableColumns();
        displayOrderSummary();
        prefillUserInfo();
    }

    public void setClientViewController(ClientViewController controller) {
        this.clientViewController = controller;
    }

    private void setupTableColumns() {
        productColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productName"));
        qtyColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("subtotal"));
    }

    private void prefillUserInfo() {
        // Pre-fill user information if logged in
        if (UserSession.getCurrentUser() != null) {
            nameField.setText(UserSession.getCurrentUser().getName());
            emailField.setText(UserSession.getCurrentUser().getEmail());
            if (UserSession.getCurrentUser().getPhone() != null) {
                phoneField.setText(UserSession.getCurrentUser().getPhone());
            }
            if (UserSession.getCurrentUser().getAddress() != null) {
                addressField.setText(UserSession.getCurrentUser().getAddress());
            }
        }
    }

    private void displayOrderSummary() {
        summaryTable.setItems(CartService.getInstance().getCartItems());
        updateSummary();
    }

    private void updateSummary() {
        // Use OrderService for calculations
        BigDecimal subtotal = orderService.calculateSubtotal(
                new ArrayList<>(CartService.getInstance().getCartItems())
        );
        BigDecimal tax = orderService.calculateTax(subtotal);
        BigDecimal total = subtotal.add(tax);

        checkoutSubtotal.setText(String.format("$%.2f", subtotal));
        checkoutTax.setText(String.format("$%.2f", tax));
        checkoutTotal.setText(String.format("$%.2f", total));
    }

    @FXML
    private void placeOrder() {
        // Validate form
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty() ||
            phoneField.getText().isEmpty() || addressField.getText().isEmpty() ||
            cityField.getText().isEmpty() || postalCodeField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields");
            return;
        }

        // Validate email format
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Validation Error", "Please enter a valid email address");
            return;
        }

        if (CartService.getInstance().isEmpty()) {
            showAlert("Empty Cart", "Your cart is empty");
            return;
        }

        try {
            // Get current user ID
            int userId = UserSession.getCurrentUser() != null ? 
                        UserSession.getCurrentUser().getUserId() : 0;
            
            // Build shipping address
            String shippingAddress = String.format("%s, %s %s",
                    addressField.getText().trim(),
                    cityField.getText().trim(),
                    postalCodeField.getText().trim());
            
            // Use OrderService to create order
            OrderService.OrderResult result = orderService.createOrder(
                    userId,
                    new ArrayList<>(CartService.getInstance().getCartItems())
            );

            if (result.isSuccess()) {
                CartService.getInstance().clearCart();
                clientViewController.updateCartButton();

                // Show success with order details
                showOrderConfirmation(result.getOrder(), shippingAddress);
                clientViewController.backToProducts();
            } else {
                showAlert("Error", result.getMessage());
            }
        } catch (Exception e) {
            showAlert("Error", "Error placing order: " + e.getMessage());
            System.err.println(e);
        }
    }

    private void showOrderConfirmation(Order order, String shippingAddress) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Confirmed!");
        alert.setHeaderText("Thank you for your order!");
        alert.setContentText(String.format(
                "Order ID: %d\n" +
                "Total: $%.2f\n\n" +
                "Shipping to:\n%s\n%s\n\n" +
                "You will receive a confirmation email shortly.",
                order.getOrderId(),
                order.getTotalAmount(),
                nameField.getText(),
                shippingAddress
        ));
        alert.showAndWait();
    }

    @FXML
    private void backToCart() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    com.ecommerce.Main.class.getResource("/com/ecommerce/cart-view.fxml"));
            javafx.scene.Parent root = loader.load();
            CartViewController controller = loader.getController();
            controller.setClientViewController(clientViewController);

            javafx.scene.layout.StackPane mainContent = 
                    (javafx.scene.layout.StackPane) nameField.getScene().lookup("#mainContent");
            if (mainContent != null) {
                mainContent.getChildren().setAll(root);
            }
        } catch (Exception e) {
            System.err.println("Error loading cart: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
