package com.example.nusync;

import com.example.nusync.controllers.*;
import com.example.nusync.data.Admin;
import com.example.nusync.data.Student;
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


    private Student current_student;

    public Scene initGUI(Stage stage) {
        this.primaryStage = stage;
        rootLayout = new BorderPane();

        // Initially load the login view.
        switchToStartView();
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

    public void setCurrentStudent(Student currentStudent) {
        this.current_student = currentStudent;
    }



    public void switchToMainApp(Student student) {
        setCurrentStudent(student); // Store the authenticated/created student

        setupToolBar();
        switchToTimetable(); // Or whichever is the default view of your app

    }
    private void setupToolBar() {
        ToolBar toolBar = new ToolBar();

        Button viewTimetableBtn = new Button("View Timetable");
        Button viewFreeRoomsBtn = new Button("View Free Rooms");
        Button manageProfileBtn = new Button("Manage Profile");
        Button viewTeacherAllocationBtn = new Button("View Teacher Allocation");
        Button submitFeedbackBtn = new Button("Submit Feedback");

        Button logoutButton = new Button("Logout");
       // Button viewTeacherAllocationBtn = new Button("Teacher Allocation");

        viewTimetableBtn.setMaxWidth(Double.MAX_VALUE);
        viewFreeRoomsBtn.setMaxWidth(Double.MAX_VALUE);
        manageProfileBtn.setMaxWidth(Double.MAX_VALUE);
        viewTeacherAllocationBtn.setMaxWidth(Double.MAX_VALUE);
        submitFeedbackBtn.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);


        viewTimetableBtn.setOnAction(event -> switchToTimetable());
        viewFreeRoomsBtn.setOnAction(event -> switchToFreeRooms());
        manageProfileBtn.setOnAction(event -> switchToManageProfile());
        viewTeacherAllocationBtn.setOnAction(event -> switchToTeacherAllocationView());
        submitFeedbackBtn.setOnAction(event -> switchTosSubmitFeedback());

        logoutButton.setOnAction(event -> switchToStartView());
//        viewTeacherAllocationBtn.setOnAction(event -> switchToTeacherAllocation());

        toolBar.getItems().addAll(viewTimetableBtn, viewFreeRoomsBtn, manageProfileBtn, viewTeacherAllocationBtn, submitFeedbackBtn ,logoutButton);
//        toolBar.getItems().add(viewTeacherAllocationBtn);

        // Style the toolbar (optional but recommended)
        toolBar.setStyle("-fx-background-color: #2f4f4f;"); // Dark slate gray color


        rootLayout.setTop(toolBar);
    }

    private void switchToTimetable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("timetable-view.fxml"));
            // Set the controller before loading the FXML
            TimeTableController timetableController = new TimeTableController(current_student);
            loader.setController(timetableController);
            Node timetableView = loader.load();
            rootLayout.setCenter(timetableView);
            primaryStage.sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchTosSubmitFeedback() {
        try {
            FXMLLoader forgotLoader = new FXMLLoader(getClass().getResource("submit-feedback.fxml"));
            SubmitFeedbackController forgotController = new SubmitFeedbackController(current_student);
            forgotLoader.setController(forgotController);
            Node forgotView = forgotLoader.load();

            rootLayout.setCenter(forgotView);
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void switchToAdminLogin() {

        try {
            FXMLLoader forgotLoader = new FXMLLoader(getClass().getResource("login-admin.fxml"));
            Node forgotView = forgotLoader.load();
            AdminLoginController forgotController = forgotLoader.getController();
            forgotController.setGuiHandler(this);
            rootLayout.setCenter(forgotView);
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Logic to switch to the admin login view
    }

    public void switchToStartView() {
        hideToolBar();
        try {
            FXMLLoader forgotLoader = new FXMLLoader(getClass().getResource("start-view.fxml"));
            Node forgotView = forgotLoader.load();
            StartViewController forgotController = forgotLoader.getController();
            forgotController.setGuiHandler(this);
            rootLayout.setCenter(forgotView);
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToAdminMainApp(Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-main-view.fxml"));
            Node adminMainView = loader.load();
            AdminMainViewController adminMainController = loader.getController();
            adminMainController.setGuiHandler(this);
            rootLayout.setCenter(adminMainView);
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void switchToManageProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manage-profile.fxml"));
            ManageProfileController profileController = new ManageProfileController(current_student);
            loader.setController(profileController);
            Node manageProfileView = loader.load();
            rootLayout.setCenter(manageProfileView);
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToForgotPassword() {
        try {
            FXMLLoader forgotLoader = new FXMLLoader(getClass().getResource("forgot-password.fxml"));
            Node forgotView = forgotLoader.load();
            ForgotPasswordController forgotController = forgotLoader.getController();
            forgotController.setGuiHandler(this);
            rootLayout.setCenter(forgotView);
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
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
    private void switchToTeacherAllocationView() {
        // The logic for switching to the Free Rooms view.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TeacherAllocationView.fxml"));
            Node adminMainView = loader.load();
            TeacherAllocationController adminMainController = loader.getController();
            adminMainController.setGuiHandler(this);
            rootLayout.setCenter(adminMainView);
            primaryStage.sizeToScene(); // Resize the stage to fit the scene's new size
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
