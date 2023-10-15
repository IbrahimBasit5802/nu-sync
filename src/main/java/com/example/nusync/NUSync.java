package com.example.nusync;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class NUSync extends Application {

    @Override
    public void start(Stage stage) {
        GUIHandler guiHandler = new GUIHandler();
        Scene scene = guiHandler.initGUI(stage);
        stage.setTitle("NuSync");
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/assets/logo.png")).toString());
        stage.getIcons().add(image);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        launch(args);
    }

}
