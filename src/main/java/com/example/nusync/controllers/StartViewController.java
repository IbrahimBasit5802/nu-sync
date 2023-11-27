package com.example.nusync.controllers;

import com.example.nusync.GUIHandler;
import javafx.fxml.FXML;

public class StartViewController {
    private GUIHandler guiHandler;

    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }

    @FXML
    private void handleStudentLogin() {
        guiHandler.switchToLogin();
    }

    @FXML
    private void handleAdminLogin() {
        guiHandler.switchToAdminLogin();
    }
}