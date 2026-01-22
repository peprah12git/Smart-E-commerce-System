package com.ecommerce.controllers;

import com.ecommerce.models.User;
import com.ecommerce.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
            mainHost.onAuthenticated();
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
            showError("Registration successful! You can now sign in.");
            clearRegisterForm();
        } else {
            showError(result.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void clearRegisterForm() {
        nameField.clear();
        registerEmailField.clear();
        registerPasswordField.clear();
        phoneField.clear();
        addressField.clear();
    }

    public interface MainHost {
        void onAuthenticated();
        void onGuest();
        void onSwitchToAdminLogin();
    }
}
