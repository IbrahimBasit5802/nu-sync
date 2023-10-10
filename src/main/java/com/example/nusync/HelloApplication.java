package com.example.nusync;

import com.example.nusync.data.Lecture;
import com.example.nusync.database.DatabaseUtil;
import com.google.api.services.sheets.v4.model.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.mongodb.client.model.Filters.eq;

public class HelloApplication extends Application {

    private static final String YOUR_API_KEY = "AIzaSyAIUw5fOxesfapuLhn8r11lI6_TXKXuwvY";
    private static final String YOUR_SPREADSHEET_ID = "1knS7NRf3WjqFOnd-b5NTx1rvWNqvNK5jjecY0fkhcXM";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("ViewTimetable");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        SheetsAPIFetcher fetcher = new SheetsAPIFetcher(YOUR_API_KEY, YOUR_SPREADSHEET_ID);
        String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for (String day : weekdays) {
            System.out.println("Fetching data for: " + day);
            try {
                Sheet sheetData = fetcher.fetchDataAndFormat(day);
                List<GridData> gridDataList = sheetData.getData();
                List<List<Object>> values = fetcher.fetchSheetData(day);

                List<Lecture> lectures = processData(values, gridDataList, day);

                DatabaseUtil dbUtil = new DatabaseUtil();

// To save lectures:
                dbUtil.initialize();

                dbUtil.saveLectures(lectures);


                // Save lectures to database or whatever you want to do
                // For example: saveLecturesToDB(lectures);

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("-----------------------");
        }

        launch(args);
    }

    public static List<Lecture> processData(List<List<Object>> values, List<GridData> gridDataList, String day) {
        List<Lecture> lectures = new ArrayList<>();
        Map<String, String> colors = new HashMap<>(); // To store color-to-Batch mapping
        int timeslot_row_index = 4;

        // Processing colors for batches
        for (int i = 0; i < 4; i++) {
            List<Object> row_vals = values.get(i);
            RowData rowData = gridDataList.get(0).getRowData().get(i);
            for (int j = 4; j < 17; j++) {
                String bg = null;
                String cell = row_vals.get(j).toString().trim();
                if (!cell.isEmpty()) {
                    String batch = "";
                    if (cell.contains("(")) {
                        String[] parts = cell.split("[()]");
                        if (parts.length > 1) {
                            batch = parts[1].substring(parts[1].length() - 2); // Get the last two characters
                        }
                    }

                    com.google.api.services.sheets.v4.model.Color bgColor = rowData.getValues().get(j).getEffectiveFormat().getBackgroundColor();
                    if (bgColor != null) {
                        float redFloat = (float) 0;
                        float greenFloat = (float) 0;
                        float blueFloat = (float) 0;
                        if (bgColor.getRed() != null) {
                            redFloat = bgColor.getRed();
                        }
                        if (bgColor.getGreen() != null) {
                            greenFloat = bgColor.getGreen();
                        }
                        if (bgColor.getBlue() != null) {
                            blueFloat = bgColor.getBlue();
                        }
                        int red = (int) (redFloat * 255);
                        int green = (int) (greenFloat * 255);
                        int blue = (int) (blueFloat * 255);

                        bg = String.format("#%02x%02x%02x", red, green, blue);
                    }

                    if (batch != null && bg != null && !batch.isEmpty() && !bg.isEmpty()) {
                        colors.put(bg, batch);

                    }
                }
            }
        }

        // Processing lectures
        for (int i = 5; i < values.size(); i++) {
            List<Object> row_vals = values.get(i);
            RowData rowData = gridDataList.get(0).getRowData().get(i);

            String room = row_vals.get(0).toString();

            if ("Lab".equals(room)) {
                timeslot_row_index = i;
                continue;
            }

            for (int j = 0; j < row_vals.size(); j++) {
                String cellValue = row_vals.get(j).toString().trim();

                if (!cellValue.isEmpty()) {
                    String timeslot;
                    if (cellValue.contains(")")) {
                        String courseInfo = cellValue.split("\\)")[0] + ")";
                        String restOfCell = cellValue.replace(courseInfo, "").trim();

                        if (!restOfCell.isEmpty() && !restOfCell.equals("Resch")) {
                            timeslot = restOfCell;
                        } else {
                            timeslot = values.get(timeslot_row_index).get(j).toString();
                        }

                        String[] parts = courseInfo.split("[()]");
                        if (parts.length >= 2) {
                            String courseName = parts[0].trim();
                            String[] courseParts = parts[1].trim().split("-");

                            String department = "";
                            String section = "";
                            if (courseParts.length >= 2) {
                                department = courseParts[0].trim();
                                section = courseParts[1].trim();
                            }

                            String cellBackgroundColor = "#FFFFFF"; // Default color
                            com.google.api.services.sheets.v4.model.Color bgColor = rowData.getValues().get(j).getEffectiveFormat().getBackgroundColor();
                            if (bgColor != null) {
                                float redFloat = (float) 0;
                                float greenFloat = (float) 0;
                                float blueFloat = (float) 0;
                                if (bgColor.getRed() != null) {
                                    redFloat = bgColor.getRed();
                                }
                                if (bgColor.getGreen() != null) {
                                    greenFloat = bgColor.getGreen();
                                }
                                if (bgColor.getBlue() != null) {
                                    blueFloat = bgColor.getBlue();
                                }
                                int red = (int) (redFloat * 255);
                                int green = (int) (greenFloat * 255);
                                int blue = (int) (blueFloat * 255);

                                cellBackgroundColor = String.format("#%02x%02x%02x", red, green, blue);
                            }
                            String batch = colors.get(cellBackgroundColor);

                            if (batch != null) {
                                Lecture newLecture = new Lecture();
                                newLecture.setCourseName(courseName);
                                newLecture.setTimeslot(timeslot);
                                newLecture.setBatch(batch);
                                newLecture.setDepartment(department);
                                newLecture.setRoom(room);
                                newLecture.setSection(section);
                                newLecture.setDay(day);

                                lectures.add(newLecture);
                            }
                        }
                    }
                }
            }
        }

        return lectures;
    }



    public static void connectToMongoDB() {
        String uri = "mongodb+srv://nusyncc:UHKgwaPhtQZuCcaV@cluster0.toqveo3.mongodb.net/?retryWrites=true&w=majority";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("sample_airbnb");
            MongoCollection<Document> collection = database.getCollection("listingAndReviews");

            Document doc = collection.find(eq("_id","10009999")).first();
            if (doc != null) {
                System.out.println(doc.toJson());
            } else {
                System.out.println("Document not found");
            }

        } catch (Exception e) {
            System.err.println("Error interacting with MongoDB: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
