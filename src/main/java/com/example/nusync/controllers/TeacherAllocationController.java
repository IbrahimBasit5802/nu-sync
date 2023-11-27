package com.example.nusync.controllers;

import com.example.nusync.GUIHandler;
import com.example.nusync.data.Lecture;
import com.example.nusync.data.TeacherAllocation;
import com.example.nusync.database.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

public class TeacherAllocationController {
    @FXML private ListView<String> allocationListView;
    @FXML private TextField searchField;
    private DatabaseUtil dbUtil = new DatabaseUtil();
    private GUIHandler guiHandler;

    @FXML
    public void initialize() {
        // Initialize the table with data
        loadAllAllocations();
    }

    private void loadAllAllocations() {
        List<TeacherAllocation> allocs = dbUtil.getAllTeacherAllocations();
        allocationListView.getItems().clear();
        for (TeacherAllocation a : allocs) {
            allocationListView.getItems().add(a.getTeacherName() + " - " + a.getCourse() + " - " + a.getSection() + " - " + a.getDepartment() + " - " + a.getSemester());
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText();
        if (!searchText.isEmpty()) {
            // Based on selection, perform search by instructor or course
            List<TeacherAllocation> allocs = dbUtil.searchTeacherAllocations(searchText);
            allocationListView.getItems().clear();

            for (TeacherAllocation a : allocs) {
                allocationListView.getItems().add(a.getTeacherName() + " - " + a.getCourse() + " - " + a.getSection());
            }
        }
    }

    public void setGuiHandler(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }
}
