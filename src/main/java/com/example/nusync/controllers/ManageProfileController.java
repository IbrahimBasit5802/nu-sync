package com.example.nusync.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.nusync.data.Student;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.nusync.database.DatabaseUtil;

public class ManageProfileController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> batchComboBox;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> sectionComboBox;

    private Student currentStudent;


    public ManageProfileController(Student student) {
        this.currentStudent = student;
        currentStudent.printStudentDetails();
    }
    @FXML
    public void initialize() {
populateFields();    }

    private void populateFields() {
        // Set the text fields with the current student's information
        fullNameField.setText(currentStudent.getFullName());
        emailField.setText(currentStudent.getEmail());

        // Assuming you have methods like getBatchOptions(), getDepartmentOptions(), and getSectionOptions()
        // that return a list of string options for the combo boxes.
        batchComboBox.getItems().setAll(getBatchOptions());
        departmentComboBox.getItems().setAll(getDepartmentOptions());
        sectionComboBox.getItems().setAll(getSectionOptions());

        // Set the combo boxes with the current student's batch, department, and section
        batchComboBox.setValue(currentStudent.getBatch());
        departmentComboBox.setValue(currentStudent.getDepartment());
        sectionComboBox.setValue(currentStudent.getSection());

        // It's important not to set the password field for security reasons.
        // If needed, you can set a placeholder text to indicate the option to change the password.
        passwordField.setPromptText("Enter new password to change");
    }

    // Example methods to get options for combo boxes
// These methods should return the data from the database or a predefined list
    private List<String> getBatchOptions() {
        // This should return the list of batches from the database or a static list
        return List.of("20", "21", "22", "23"); // Example batch values
    }

    private List<String> getDepartmentOptions() {


        // This should return the list of departments from the database or a static list
        return List.of("CS", "DS", "AI", "SE", "CY"); // Example department values
    }

    private List<String> getSectionOptions() {
        List<String> sectionList = new ArrayList<>();
        for (char section = 'A'; section <= 'Z'; section++) {
            sectionList.add(String.valueOf(section));
        }
        // This should return the list of sections from the database or a static list
        return sectionList; // Example section values
    }

    @FXML
    private void handleSaveChanges() {
        // Retrieve the entered data from the form fields
        String fullName = fullNameField.getText();
        // Assume the email field is not editable, so we use the current student's email
        String email = currentStudent.getEmail();
        String newMail = emailField.getText();
        String password = passwordField.getText();
        String section = sectionComboBox.getValue();
        String batch = batchComboBox.getValue();
        String department = departmentComboBox.getValue();

        // Check if the password field is empty or not
        String encryptedPassword = password.isEmpty() ? currentStudent.getPassword()
                : BCrypt.withDefaults().hashToString(12, password.toCharArray());

        // Update the student's information in the database
        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement updateStmt = conn.prepareStatement(
                     "UPDATE students SET full_name = ?, section = ?, batch = ?, department = ?, email = ?"
                             + (password.isEmpty() ? "" : ", password = ?")
                             + " WHERE email = ?")) {

            updateStmt.setString(1, fullName);
            updateStmt.setString(2, section);
            updateStmt.setString(3, batch);
            updateStmt.setString(4, department);
            updateStmt.setString(5, newMail);
            if (!password.isEmpty()) {
                updateStmt.setString(6, encryptedPassword);
                updateStmt.setString(7, email);
            } else {
                updateStmt.setString(6, email);
            }

            int affectedRows = updateStmt.executeUpdate();

            if (affectedRows > 0) {
                // Update was successful
                // Optionally, you can update the current_student object with the new details
                currentStudent.setFullName(fullName);
                currentStudent.setEmail(newMail);
                currentStudent.setSection(section);
                currentStudent.setBatch(batch);
                currentStudent.setDepartment(department);
                // Don't store the plaintext password in the current_student object

                // Show a confirmation message to the user
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully.");
                alert.showAndWait();
            } else {
                // Handle the case where the student record was not updated
                Alert alert = new Alert(Alert.AlertType.ERROR, "Profile update failed.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            // Handle any SQL exceptions
            Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred while updating the profile: " + e.getMessage());
            alert.showAndWait();
        }
    }

}
