package com.example.nusync.controllers;

import com.example.nusync.DatabaseHandler;
import com.example.nusync.data.Lecture;
import com.example.nusync.database.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TimeTableController {

    @FXML
    private ComboBox<String> sectionSelector;

    @FXML
    private ComboBox<String> daySelector;

    @FXML
    private ComboBox<String> batchSelector;

    @FXML
    private ComboBox<String> departmentSelector;

    @FXML
    private ListView<String> lecturesListView;

    @FXML
    private ProgressIndicator databaseLoadingProgress;


    @FXML
    private Label loadingMessage;


    private final DatabaseUtil databaseUtil = new DatabaseUtil();

    @FXML
    public void initialize() {
        // Sample data for the selectors. Replace with real data as needed.
        sectionSelector.getItems().addAll("A", "B", "C", "D", "E", "F", "G", "H", "J", "K");
        daySelector.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        batchSelector.getItems().addAll("23", "22", "21", "20");
        departmentSelector.getItems().addAll("CS", "AI", "DS", "CY", "SE");
        sectionSelector.getSelectionModel().selectFirst();
        daySelector.getSelectionModel().selectFirst();
        batchSelector.getSelectionModel().selectFirst();
        departmentSelector.getSelectionModel().selectFirst();
        databaseLoadingProgress.setVisible(true);
        loadingMessage.setText("Initializing database...");

        DatabaseHandler dbHandler = new DatabaseHandler();
        dbHandler.initializeDatabaseAsync().thenRun(() -> {
            Platform.runLater(() -> {
                databaseLoadingProgress.setVisible(false);
                loadingMessage.setText("Database initialized.");
            });
        });

    }

    @FXML
    public void onViewTimetableClick() {
        // Extract selected values
        String section = sectionSelector.getValue();
        String day = daySelector.getValue();
        String batch = batchSelector.getValue();
        String department = departmentSelector.getValue();
        DatabaseUtil.initialize();

        // Show loading indicator and hide loading message initially
        databaseLoadingProgress.setVisible(true);
        loadingMessage.setVisible(false);

        // Perform database query in a separate thread
        CompletableFuture.supplyAsync(() -> {
            try {
                List<Lecture> lectures = databaseUtil.queryLectures(section, day, batch, department);
                return lectures;
            } catch (Exception e) {
                e.printStackTrace(); // Log any exceptions
                return null;
            }
        }).thenAcceptAsync(lectures -> {
            // Update UI on the JavaFX application thread
            Platform.runLater(() -> {
                // Check if lectures is not null
                if (lectures != null) {
                    System.out.println("Lectures list size: " + lectures.size());

                    // Display the lectures in the ListView
                    lecturesListView.getItems().clear();
                    for (Lecture lecture : lectures) {
                        lecturesListView.getItems().add(lecture.getCourseName() + " - " + lecture.getTimeslot() + " - " + lecture.getRoom());
                    }

                    // Update loading message
                    loadingMessage.setText("Classes fetched.");
                } else {
                    // Update loading message
                    loadingMessage.setText("Error fetching classes.");
                }

                // Hide loading indicator and show loading message
                databaseLoadingProgress.setVisible(false);
                loadingMessage.setVisible(true);
            });
        });
    }




}
