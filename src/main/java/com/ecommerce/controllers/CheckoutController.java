package com.ecommerce.controllers;

import com.ecommerce.models.CartItem;
import com.ecommerce.models.Order;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.ArrayList;

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
    @FXML private TableView<CartItem> summaryTable;
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
        displayOrderSummary();
    }

    public void setClientViewController(ClientViewController controller) {
        this.clientViewController = controller;
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
            phoneField.getText().isEmpty() || addressField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields");
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
            
            // Use OrderService to create order
            OrderService.OrderResult result = orderService.createOrder(
                    userId,
                    new ArrayList<>(CartService.getInstance().getCartItems())
            );

            if (result.isSuccess()) {
                CartService.getInstance().clearCart();
                clientViewController.updateCartButton();

                showAlert("Success", result.getMessage() + 
                         "\nOrder ID: " + result.getOrder().getOrderId());
                clientViewController.backToProducts();
            } else {
                showAlert("Error", result.getMessage());
            }
        } catch (Exception e) {
            showAlert("Error", "Error placing order: " + e.getMessage());
            System.err.println(e);
        }
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
