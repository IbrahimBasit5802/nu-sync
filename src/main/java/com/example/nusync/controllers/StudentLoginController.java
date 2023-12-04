package com.example.nusync.controllers;

import com.example.nusync.exceptions.AuthenticationException;
import com.example.nusync.GUIHandler;
import com.example.nusync.exceptions.UserNotFoundException;
import com.example.nusync.data.Student;
import com.example.nusync.database.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private GUIHandler guiHandler;
    private DatabaseUtil dbUtil;
    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }

    @FXML
    private void handleCreateAccount() {
        // Switch to the sign-up view
        guiHandler.switchToSignUp();
    }

    @FXML
    private void initialize() {
        // Initialize the combo boxes with available options
        dbUtil = new DatabaseUtil();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Authenticate the student
        try {
            Student authenticatedStudent = dbUtil.authenticateStudent(email, password);
            guiHandler.switchToMainApp(authenticatedStudent);
            // Continue with authenticated student
        } catch (UserNotFoundException e) {
            // Handle user not found situation
            errorLabel.setText(e.getMessage());
        } catch (AuthenticationException e) {
            // Handle wrong password situation
            errorLabel.setText(e.getMessage());
        }

    }

    @FXML
    private void handleForgotPassword() {

        guiHandler.switchToForgotPassword();
        // Implement the functionality for password recovery here
        // This can be done in a later development phase
    }
}
