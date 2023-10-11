package com.example.nusync;

import com.example.nusync.controllers.FreeRoomsController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class GUIHandler {

    private BorderPane rootLayout;

    public Scene initGUI(Stage stage) {
        rootLayout = new BorderPane();
        setupToolBar();

        // Loading Default View
        switchToTimetable();

        return new Scene(rootLayout, 800, 800);
    }

    private void setupToolBar() {
        ToolBar toolBar = new ToolBar();

        Button viewTimetableBtn = new Button("View Timetable");
        Button viewFreeRoomsBtn = new Button("View Free Rooms");


        viewTimetableBtn.setMaxWidth(Double.MAX_VALUE);
        viewFreeRoomsBtn.setMaxWidth(Double.MAX_VALUE);


        viewTimetableBtn.setOnAction(event -> switchToTimetable());
        viewFreeRoomsBtn.setOnAction(event -> switchToFreeRooms());

        toolBar.getItems().addAll(viewTimetableBtn, viewFreeRoomsBtn);

        // Style the toolbar (optional but recommended)
        toolBar.setStyle("-fx-background-color: #2f4f4f;"); // Dark slate gray color
        viewTimetableBtn.setStyle("-fx-text-fill: white; -fx-background-color: #696969;"); // Dim gray
        viewFreeRoomsBtn.setStyle("-fx-text-fill: white; -fx-background-color: #696969;"); // Dim gray

        rootLayout.setTop(toolBar);
    }


    private void switchToTimetable() {
        try {
            Node timetableView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("timetable-view.fxml")));
            rootLayout.setCenter(timetableView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchToFreeRooms() {
        // The logic for switching to the Free Rooms view.
        try {
            FXMLLoader freeRoomsLoader = new FXMLLoader(getClass().getResource("free-rooms-view.fxml"));
            Node freeRoomsView = freeRoomsLoader.load();
            rootLayout.setCenter(freeRoomsView);

            FreeRoomsController freeRoomsController = freeRoomsLoader.getController();

            // Ensure the progress indicator is visible
            freeRoomsController.showProgressIndicator();

            // Add a slight delay before loading data
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(e -> freeRoomsController.loadData());
            pause.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
