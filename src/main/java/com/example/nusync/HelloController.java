package com.example.nusync;

import com.example.nusync.data.Lecture;
import com.example.nusync.database.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;

import java.util.List;

public class HelloController {

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
    private ProgressIndicator loadingSpinner;

    @FXML
    private Label loadingMessage;


    private final DatabaseUtil databaseUtil = new DatabaseUtil();

    @FXML
    public void initialize() {
        // Sample data for the selectors. Replace with real data as needed.
        sectionSelector.getItems().addAll("A", "B", "C");
        daySelector.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        batchSelector.getItems().addAll("2022", "2023", "2024");
        departmentSelector.getItems().addAll("CS", "EE", "ME");
        sectionSelector.getSelectionModel().selectFirst();
        daySelector.getSelectionModel().selectFirst();
        batchSelector.getSelectionModel().selectFirst();
        departmentSelector.getSelectionModel().selectFirst();

    }

    @FXML
    public void onViewTimetableClick() {
        // Extract selected values
        String section = sectionSelector.getValue();
        String day = daySelector.getValue();
        String batch = batchSelector.getValue();
        String department = departmentSelector.getValue();

        loadingSpinner.setVisible(true);
        loadingMessage.setText("Fetching classes...");

        // Use these values to query the database for lectures
        List<Lecture> lectures = databaseUtil.queryLectures(section, day, batch, department);

        // Display the lectures in the ListView
        lecturesListView.getItems().clear();
        for (Lecture lecture : lectures) {
            lecturesListView.getItems().add(lecture.getCourseName() + " - " + lecture.getTimeslot() + " - " + lecture.getRoom());
        }

        loadingSpinner.setVisible(false);
        loadingMessage.setText("Fetching classes...");
    }
}
