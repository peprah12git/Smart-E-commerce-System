package com.ecommerce.controllers;

import com.ecommerce.models.User;
import com.ecommerce.service.UserService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for Login view
 * Uses UserService for authentication and registration
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private Label errorLabel;
    @FXML private VBox loginBox;
    @FXML private VBox registerBox;

    // Service (not DAO)
    private UserService userService;
    private MainHost mainHost;

    @FXML
    public void initialize() {
        // Use service instead of DAO
        userService = UserService.getInstance();
    }

    public void setHost(MainHost host) {
        this.mainHost = host;
    }
// Handle Sign In button click
    @FXML
    private void handleSignIn() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }

        // Use UserService for authentication
        User user = userService.authenticate(email, password);
        if (user != null) {
            UserSession.setCurrentUser(user);
            // Route based on user role
            if ("admin".equalsIgnoreCase(user.getRole())) {
                mainHost.onAdminAuthenticated();
            } else {
                mainHost.onUserAuthenticated();
            }
        } else {
            showError("Invalid email or password");
        }
    }

    @FXML
    private void handleGuest() {
        UserSession.setCurrentUser(new User(0, "Guest", "guest@example.com", "", "", ""));
        mainHost.onGuest();
    }

    @FXML
    private void handleAdminLogin() {
        mainHost.onSwitchToAdminLogin();
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();

        // Use UserService for registration with validation
        UserService.RegisterResult result = userService.registerUser(
                name, email, password, phone, address
        );

        if (result.isSuccess()) {
            // Automatically sign in the newly registered user
            User newUser = result.getUser();
            UserSession.setCurrentUser(newUser);
            
            // Navigate to client view (registered users are always regular users, not admins)
            mainHost.onUserAuthenticated();
        } else {
            showError(result.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    @FXML
    private void showRegisterForm() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        registerBox.setVisible(true);
        registerBox.setManaged(true);
        errorLabel.setText("");
    }

    @FXML
    private void showLoginForm() {
        registerBox.setVisible(false);
        registerBox.setManaged(false);
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        errorLabel.setText("");
    }

    private void clearRegisterForm() {
        nameField.clear();
        registerEmailField.clear();
        registerPasswordField.clear();
        phoneField.clear();
        addressField.clear();
    }

    public interface MainHost {
        void onAdminAuthenticated();
        void onUserAuthenticated();
        void onGuest();
        void onSwitchToAdminLogin();
    }
}
