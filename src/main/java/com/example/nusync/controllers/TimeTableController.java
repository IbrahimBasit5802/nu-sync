package com.example.nusync.controllers;

import com.example.nusync.DatabaseHandler;
import com.example.nusync.data.Lecture;
import com.example.nusync.data.Student;
import com.example.nusync.database.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;

import java.util.ArrayList;
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

    private final Student currentStudent;


    private final DatabaseUtil databaseUtil = new DatabaseUtil();

    public TimeTableController(Student currentStudent) {
        currentStudent.printStudentDetails();
        this.currentStudent = currentStudent;

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
    public void initialize() {
        // Sample data for the selectors. Replace with real data as needed.
        sectionSelector.getItems().setAll(getSectionOptions());
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


        String section = currentStudent.getSection();
        String batch = currentStudent.getBatch();
        String department = currentStudent.getDepartment();
        DatabaseUtil.initialize();

        // Show loading indicator and hide loading message initially
        databaseLoadingProgress.setVisible(true);
        loadingMessage.setVisible(false);

        // Perform database query in a separate thread
        CompletableFuture.supplyAsync(() -> {
            try {
                List<Lecture> lectures = databaseUtil.queryLectures(section, batch, department);
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
                        lecturesListView.getItems().add(lecture.getCourseName() + " - " + lecture.getTimeslot() + " - " + lecture.getRoom() + " - " + lecture.getDay());
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

    @FXML
    public void onViewTimetableClick() {
        // Extract selected values
        String section = sectionSelector.getValue();
        String batch = batchSelector.getValue();
        String department = departmentSelector.getValue();
        DatabaseUtil.initialize();


        // Show loading indicator and hide loading message initially
        databaseLoadingProgress.setVisible(true);
        loadingMessage.setVisible(false);

        // Perform database query in a separate thread
        CompletableFuture.supplyAsync(() -> {
            try {
                List<Lecture> lectures = databaseUtil.queryLectures(section, batch, department);
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
                        lecturesListView.getItems().add(lecture.getCourseName() + " - " + lecture.getTimeslot() + " - " + lecture.getRoom() + "-" + lecture.getDay());
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
