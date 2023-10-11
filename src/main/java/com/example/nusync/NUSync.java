package com.example.nusync;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NUSync extends Application {

    @Override
    public void start(Stage stage) {
        GUIHandler guiHandler = new GUIHandler();
        Scene scene = guiHandler.initGUI(stage);
        stage.setTitle("NuSync Application");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        launch(args);
    }
}
