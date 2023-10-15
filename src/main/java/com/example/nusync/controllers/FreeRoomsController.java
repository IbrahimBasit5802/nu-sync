package com.example.nusync.controllers;

import com.example.nusync.data.FreeRoom;
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
        CompletableFuture<List<FreeRoom>> future = CompletableFuture.supplyAsync(() -> {
            try {
                List<FreeRoom> rooms = databaseUtil.loadFreeRooms();
                System.out.println("Fetched " + rooms.size() + " free rooms."); // Debugging
                return rooms;
            } catch (Exception e) {
                e.printStackTrace(); // Log any exceptions
                return null;
            }
        });

        future.thenAcceptAsync(rooms -> {
            if (rooms == null) {
                System.out.println("Free Room list is null."); // Debugging
                return;
            }
            Platform.runLater(() -> {
                freeRoomsListView.getItems().clear();
                for (FreeRoom room : rooms) {
                    freeRoomsListView.getItems().add(room.getRoomName() + " - " + room.getStartTime() + " - " + room.getEndTime() + " - " + room.getDay());
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

