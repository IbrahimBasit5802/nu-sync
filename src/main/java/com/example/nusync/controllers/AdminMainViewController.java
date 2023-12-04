package com.example.nusync.controllers;

import com.example.nusync.GUIHandler;
import com.example.nusync.data.Feedback;
import com.example.nusync.data.TeacherAllocation;
import com.example.nusync.database.DatabaseUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.util.List;

public class AdminMainViewController {

    @FXML private ListView<String> feedbackListView;
    private DatabaseUtil dbUtil = new DatabaseUtil();


    private GUIHandler guiHandler;

    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }

    @FXML
    private void handleViewFeedbacks() {
        // Logic to handle viewing user feedbacks
        feedbackListView.getItems().clear();
        List<Feedback> fd = dbUtil.getAllFeedbacks();
        for (Feedback a : fd) {
            feedbackListView.getItems().add(a.getName() + " - " + a.getEmail() + " - " + a.getFeedback());
        }
    }

    public void handleLogout() {
        guiHandler.switchToStartView();
    }
}
