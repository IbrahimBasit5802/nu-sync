package com.example.nusync.controllers;

import com.example.nusync.data.Lecture;
import com.example.nusync.database.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
public class FreeRoomsController {
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private ListView<String> freeRoomsListView;

    private final DatabaseUtil databaseUtil = new DatabaseUtil();

    @FXML
    public void initialize() {
        DatabaseUtil.initialize();
        progressIndicator.setVisible(true);


    }

    public void loadData() {
        // Place the database loading code here
        CompletableFuture<List<Lecture>> future = CompletableFuture.supplyAsync(() -> {
            try {
                List<Lecture> lectures = databaseUtil.loadLectures();
                System.out.println("Fetched " + lectures.size() + " lectures."); // Debugging
                return lectures;
            } catch (Exception e) {
                e.printStackTrace(); // Log any exceptions
                return null;
            }
        });

        future.thenAcceptAsync(lectures -> {
            if (lectures == null) {
                System.out.println("Lectures list is null."); // Debugging
                return;
            }
            Platform.runLater(() -> {
                freeRoomsListView.getItems().clear();
                for (Lecture lecture : lectures) {
                    freeRoomsListView.getItems().add(lecture.getCourseName() + " - " + lecture.getTimeslot() + " - " + lecture.getRoom());
                }
                progressIndicator.setVisible(false);
            });
        });

        future.exceptionally(ex -> {
            System.out.println("An exception occurred: " + ex.getMessage()); // Debugging
            progressIndicator.setVisible(false);
            return null;
        });
    }

    public void showProgressIndicator() {
        progressIndicator.setVisible(true);
    }



}

