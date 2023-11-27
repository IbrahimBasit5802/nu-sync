package com.example.nusync.controllers;

import com.example.nusync.GUIHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class AdminMainViewController {
    private GUIHandler guiHandler;

    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }

    @FXML
    private void handleViewFeedbacks() {
        // Logic to handle viewing user feedbacks
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "This will show user feedbacks.");
        alert.showAndWait();
    }

    public void handleLogout() {
        guiHandler.switchToStartView();
    }
}
