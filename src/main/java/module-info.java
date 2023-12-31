module com.example.nusync {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires com.google.api.services.sheets;
    requires com.google.api.client;
    requires google.api.client;
    requires google.http.client.jackson2;
    requires java.sql;
    requires bcrypt;

    //requires eu.hansolo.tilesfx;

    opens com.example.nusync to javafx.fxml, org.controlsfx.controls;
    exports com.example.nusync;
    exports com.example.nusync.controllers;
    opens com.example.nusync.controllers to javafx.fxml;
    exports com.example.nusync.data;
    exports com.example.nusync.exceptions;
    opens com.example.nusync.exceptions to javafx.fxml, org.controlsfx.controls;


}