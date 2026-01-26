package com.ecommerce.controllers;

import java.math.BigDecimal;

import com.ecommerce.models.CartItem;
import com.ecommerce.service.CartService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class CartViewController {
    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> nameColumn;
    @FXML
    private TableColumn<CartItem, BigDecimal> priceColumn;
    @FXML
    private TableColumn<CartItem, Integer> quantityColumn;
    @FXML
    private TableColumn<CartItem, BigDecimal> subtotalColumn;
    @FXML
    private TableColumn<CartItem, Void> actionColumn;

    @FXML
    private Label subtotalLabel;
    @FXML
    private Label shippingLabel;
    @FXML
    private Label taxLabel;
    @FXML
    private Label totalLabel;

    private ClientViewController clientViewController;

    @FXML
    public void initialize() {
        setupTable();
        loadCart();
    }

    public void setClientViewController(ClientViewController controller) {
        this.clientViewController = controller;
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Add delete button column
        actionColumn.setCellFactory(param -> new TableCell<CartItem, Void>() {
            private final Button deleteBtn = new Button("Remove");

            {
                deleteBtn.setStyle("-fx-font-size: 11; -fx-padding: 5; -fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; -fx-cursor: hand; -fx-border-radius: 3; " +
                        "-fx-background-radius: 3;");
                deleteBtn.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    CartService.getInstance().removeItem(item.getProductId());
                    updateCartDisplay();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        cartTable.setItems(CartService.getInstance().getCartItems());
    }

    private void updateCartDisplay() {
        cartTable.refresh();
        loadCart();
        if (clientViewController != null) {
            clientViewController.updateCartButton();
        }
    }

    private void loadCart() {
        if (CartService.getInstance().isEmpty()) {
            shippingLabel.setText("FREE");
            subtotalLabel.setText("$0.00");
            taxLabel.setText("$0.00");
            totalLabel.setText("$0.00");
        } else {
            updateSummary();
        }
    }

    private void updateSummary() {
        BigDecimal subtotal = CartService.getInstance().getTotalPrice();
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)); // 10% tax
        BigDecimal total = subtotal.add(tax);

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        shippingLabel.setText("FREE");
        taxLabel.setText(String.format("$%.2f", tax));
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void continueShopping() {
        clientViewController.backToProducts();
    }

    @FXML
    private void handleIncreaseQuantity() {
        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select an item to increase quantity.");
            return;
        }
        int newQuantity = selectedItem.getQuantity() + 1;
        CartService.getInstance().updateQuantity(selectedItem.getProductId(), newQuantity);
        updateCartDisplay();
    }

    @FXML
    private void handleDecreaseQuantity() {
        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select an item to decrease quantity.");
            return;
        }
        int newQuantity = selectedItem.getQuantity() - 1;
        CartService.getInstance().updateQuantity(selectedItem.getProductId(), newQuantity);
        updateCartDisplay();
    }

    @FXML
    private void checkout() {
        if (CartService.getInstance().isEmpty()) {
            showAlert("Empty Cart", "Your cart is empty. Add items before checkout.");
            return;
        }
        // Navigate to checkout
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    com.ecommerce.Main.class.getResource("/com/ecommerce/checkout.fxml"));
            javafx.scene.Parent root = loader.load();
            CheckoutController controller = loader.getController();
            controller.setClientViewController(clientViewController);

            javafx.scene.layout.StackPane mainContent = (javafx.scene.layout.StackPane) cartTable.getScene()
                    .lookup("#mainContent");
            if (mainContent != null) {
                mainContent.getChildren().setAll(root);
            }
        } catch (Exception e) {
            System.err.println("Error loading checkout: " + e.getMessage());
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
