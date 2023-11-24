package com.example.nusync;

import com.example.nusync.controllers.FreeRoomsController;
import com.example.nusync.controllers.StudentLoginController;
import com.example.nusync.controllers.StudentSignUpController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class GUIHandler {

    private BorderPane rootLayout;
    private Stage primaryStage;

    public Scene initGUI(Stage stage) {
        this.primaryStage = stage;
        rootLayout = new BorderPane();

        // Initially load the login view.
        switchToLogin();
        Scene scene = new Scene(rootLayout);

        // Return the scene with the root layout.
        // scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/path/to/login.css")).toExternalForm());
        primaryStage.setScene(scene); // Set the scene on the primary stage
        primaryStage.sizeToScene(); // Size the stage to match the scene
        primaryStage.show(); // Show the primary stage

        return scene;    }

    public void switchToSignUp() {
        try {
            FXMLLoader signUpLoader = new FXMLLoader(getClass().getResource("signup-student.fxml"));
            Node signUpView = signUpLoader.load();
            StudentSignUpController signUpController = signUpLoader.getController();
            signUpController.setGuiHandler(this);
            rootLayout.setCenter(signUpView);
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToLogin() {
        try {
            FXMLLoader signInLoader = new FXMLLoader(getClass().getResource("login-student.fxml"));
            Node signInView = signInLoader.load();
            StudentLoginController signInController = signInLoader.getController();
            signInController.setGuiHandler(this);
            rootLayout.setCenter(signInView);
            hideToolBar();
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hideToolBar() {
        rootLayout.setTop(null); // This will remove the toolbar from the top region
    }





    public void switchToMainApp() {
        setupToolBar();
        switchToTimetable(); // Or whichever is the default view of your app
    }
    private void setupToolBar() {
        ToolBar toolBar = new ToolBar();

        Button viewTimetableBtn = new Button("View Timetable");
        Button viewFreeRoomsBtn = new Button("View Free Rooms");
        Button logoutButton = new Button("Logout");
       // Button viewTeacherAllocationBtn = new Button("Teacher Allocation");

        viewTimetableBtn.setMaxWidth(Double.MAX_VALUE);
        viewFreeRoomsBtn.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);


        viewTimetableBtn.setOnAction(event -> switchToTimetable());
        viewFreeRoomsBtn.setOnAction(event -> switchToFreeRooms());
        logoutButton.setOnAction(event -> switchToLogin());
//        viewTeacherAllocationBtn.setOnAction(event -> switchToTeacherAllocation());

        toolBar.getItems().addAll(viewTimetableBtn, viewFreeRoomsBtn, logoutButton);
//        toolBar.getItems().add(viewTeacherAllocationBtn);

        // Style the toolbar (optional but recommended)
        toolBar.setStyle("-fx-background-color: #2f4f4f;"); // Dark slate gray color
        viewTimetableBtn.setStyle("-fx-text-fill: white; -fx-background-color: #696969;"); // Dim gray
        viewFreeRoomsBtn.setStyle("-fx-text-fill: white; -fx-background-color: #696969;");
        logoutButton.setStyle("-fx-text-fill: white; -fx-background-color: #696969;");// Dim gray

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

    public BorderPane getRootLayout() {
        return rootLayout;
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
