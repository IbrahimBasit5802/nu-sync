package com.example.nusync.controllers;

import com.example.nusync.GUIHandler;
import com.example.nusync.data.Feedback;
import com.example.nusync.data.Student;
import com.example.nusync.database.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class SubmitFeedbackController {

    @FXML
    private TextArea feedbackTextArea;

    @FXML
    private Label errorLabel;

    private final DatabaseUtil databaseUtil = new DatabaseUtil();
    private GUIHandler guiHandler;

    private Student curr_student;


    public SubmitFeedbackController(Student currentStudent) {
        this.curr_student = currentStudent;

    }

    @FXML
    private void handleSubmitButton() {
        String feedbackText = feedbackTextArea.getText().trim();

        if (feedbackText.isEmpty()) {
            errorLabel.setText("Feedback cannot be empty.");
            return;
        }

        try {
            // Assuming you have a method in DatabaseUtil to insert feedback
            Feedback feedback = new Feedback(curr_student.getFullName(), curr_student.getEmail(), feedbackText);
            databaseUtil.insertFeedback(feedback);
            errorLabel.setText("Feedback submitted successfully.");
            feedbackTextArea.clear();
        } catch (Exception e) {
            errorLabel.setText("Error submitting feedback: " + e.getMessage());
        }
    }

    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }
}
