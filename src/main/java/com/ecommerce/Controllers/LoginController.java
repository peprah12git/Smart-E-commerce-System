package com.ecommerce.Controllers;

import com.ecommerce.service.UserService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    private MainHost host;
    private final UserService userService = new UserService();

    public interface MainHost {
        void onAuthenticated();
        void onGuest();
    }

    public void setHost(MainHost host) {
        this.host = host;
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail != null ? txtEmail.getText().trim() : "";
        String password = txtPassword != null ? txtPassword.getText() : "";

        // Validation
        if (email.isEmpty()) {
            showStatus("Email cannot be empty", true);
            return;
        }

        if (!email.contains("@")) {
            showStatus("Please enter a valid email address", true);
            return;
        }

        if (password.isEmpty()) {
            showStatus("Password cannot be empty", true);
            return;
        }

        // Authenticate
        System.out.println("[AUTH] Attempting login for: " + email);
        boolean authenticated = userService.authenticateUser(email, password);
        
        if (authenticated) {
            System.out.println("[AUTH] ✓ Login successful for: " + email);
            showStatus("✓ Signed in successfully", false);
            
            if (txtPassword != null) {
                txtPassword.clear();
            }
            if (txtEmail != null) {
                txtEmail.clear();
            }
            
            // Switch to main view after brief delay
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    if (host != null) {
                        Platform.runLater(() -> host.onAuthenticated());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            System.out.println("[AUTH] ✗ Login failed for: " + email);
            showStatus("Invalid email or password", true);
        }
    }

    @FXML
    private void handleGuest() {
        showStatus("Continuing as guest", false);
        if (host != null) {
            Platform.runLater(() -> host.onGuest());
        }
    }

    private void showStatus(String message, boolean error) {
        if (lblStatus == null) return;
        lblStatus.setText(message);
        lblStatus.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
}
