package com.example.nusync.controllers;

import com.example.nusync.data.FreeRoom;
import com.example.nusync.data.Lecture;
import com.example.nusync.database.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    @FXML
    private void handleShowCurrentFreeRooms() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        CompletableFuture<List<FreeRoom>> future = CompletableFuture.supplyAsync(() -> {
            try {
                List<FreeRoom> rooms = databaseUtil.loadFreeRooms();
                // Filter for rooms that are free today and at the current time
                return rooms.stream()
                        .filter(room -> room.getDay().equalsIgnoreCase(today.getDayOfWeek().toString()))
                        .filter(room -> {
                            LocalTime startTime = LocalTime.parse(room.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                            LocalTime endTime = LocalTime.parse(room.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
                            return (now.isAfter(startTime) && now.isBefore(endTime)) || now.equals(startTime);
                        })
                        .collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });

        future.thenAcceptAsync(rooms -> {
            if (rooms == null) {
                System.out.println("Free Room list is null.");
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
            System.out.println("An exception occurred: " + ex.getMessage());
            progressIndicator.setVisible(false);
            return null;
        });
    }


    public void showProgressIndicator() {
        progressIndicator.setVisible(true);
    }



}

