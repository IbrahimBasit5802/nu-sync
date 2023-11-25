package com.example.nusync.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.nusync.GUIHandler;
import com.example.nusync.database.DatabaseUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ForgotPasswordController {
    @FXML
    private TextField emailField;

    @FXML
    private Text promptText;

    private GUIHandler guiHandler;


    public void initialize() {
        emailField.textProperty().addListener((obs, oldText, newText) -> {
            promptText.setVisible(newText.isEmpty());
        });

        emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && emailField.getText().isEmpty()) {
                promptText.setVisible(true);
            } else {
                promptText.setVisible(false);
            }
        });

        // Initialize prompt visibility based on initial TextField content
        promptText.setVisible(emailField.getText().isEmpty());
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please enter your email.");
            return;
        }

        // Assuming the existence of a method that checks if the email exists in the database
        if (!emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Email not found.");
            return;
        }

        // Prompt for the new password, for simplicity using a TextInputDialog here
        String newPassword = promptNewPassword();
        if (newPassword == null || newPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Password reset cancelled.");
            return;
        }

        // Encrypt the new password
        String encryptedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

        // Update the password in the database
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement stmt = conn.prepareStatement("UPDATE students SET password = ? WHERE email = ?")) {
            stmt.setString(1, encryptedPassword);
            stmt.setString(2, email);
            int result = stmt.executeUpdate();

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Password has been reset successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Password reset failed.");
            }
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "An error occurred: " + ex.getMessage());
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM students WHERE email = ?";
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String promptNewPassword() {
        // Simple dialog to get the new password. In real-world scenarios, you should use a more secure method.
        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Password Reset");
        passwordDialog.setHeaderText("Enter a new password");
        passwordDialog.setContentText("New password:");

        // Traditional dialog pane approach
        Optional<String> result = passwordDialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }
        return null; // If the user cancels the dialog, return null
    }

    private void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type, content);
        alert.showAndWait();
    }

    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }

    public void handleBackToLogin() {
        guiHandler.switchToLogin();
    }
}
