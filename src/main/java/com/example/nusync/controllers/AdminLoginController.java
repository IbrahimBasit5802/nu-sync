package com.example.nusync.controllers;

import com.example.nusync.exceptions.AuthenticationException;
import com.example.nusync.GUIHandler;
import com.example.nusync.exceptions.UserNotFoundException;
import com.example.nusync.data.Admin;
import com.example.nusync.database.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AdminLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private GUIHandler guiHandler;
    private DatabaseUtil dbUtil;

    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }

    @FXML
    private void initialize() {
        dbUtil = new DatabaseUtil();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Authenticate the admin
        try {
            // Assuming you have an authenticateAdmin method in your DatabaseUtil class
            Admin authenticatedAdmin = dbUtil.authenticateAdmin(email, password);
            guiHandler.switchToAdminMainApp(authenticatedAdmin);
        } catch (UserNotFoundException | AuthenticationException e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
