package com.example.nusync.controllers;

import com.example.nusync.GUIHandler;
import com.example.nusync.data.Student;
import com.example.nusync.database.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class StudentSignUpController {
    public PasswordField passwordField;
    public Label errorLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    // ... other fields
    @FXML private ComboBox<String> batchComboBox;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> sectionComboBox;
    private DatabaseUtil dbUtil;


    private GUIHandler guiHandler;

    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }

    @FXML
    private void initialize() {
        // Initialize the combo boxes with available options
        initializeBatchComboBox();
        initializeDepartmentComboBox();
        initializeSectionComboBox();
        sectionComboBox.getSelectionModel().selectFirst();
        batchComboBox.getSelectionModel().selectFirst();
        departmentComboBox.getSelectionModel().selectFirst();
        dbUtil = new DatabaseUtil();
    }

    private void initializeBatchComboBox() {
        // Populate the batch combo box with available options (e.g., 20 to 23)
        for (int i = 20; i <= 23; i++) {
            batchComboBox.getItems().add(Integer.toString(i));
        }
    }

    private void initializeDepartmentComboBox() {
        // Populate the department combo box with available options
        departmentComboBox.getItems().addAll("CS", "DS", "AI", "SE", "CY");
    }

    private void initializeSectionComboBox() {
        // Populate the section combo box with single characters (A to Z)
        for (char section = 'A'; section <= 'Z'; section++) {
            sectionComboBox.getItems().add(String.valueOf(section));
        }
    }

    @FXML
    private void handleSignUp() {
        // Retrieve the selected values from combo boxes
        String batch = batchComboBox.getSelectionModel().getSelectedItem();
        String department = departmentComboBox.getSelectionModel().getSelectedItem();
        String section = sectionComboBox.getSelectionModel().getSelectedItem();

        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();


        // Perform input validation here

        boolean isCreated = dbUtil.createStudent(fullName, email, password, section, batch, department);

        if (!isCreated) {
            errorLabel.setText("Email already exists, please login.");

        }

        else {
            // If sign-up is successful:
            Student createdStudent = new Student(fullName, email, password, section, batch, department);
            guiHandler.switchToMainApp(createdStudent);
        }



    }

    public void handleGoToLogin() {

        guiHandler.switchToLogin();
    }
}
