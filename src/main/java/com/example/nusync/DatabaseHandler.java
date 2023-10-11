package com.example.nusync;

import com.example.nusync.config.Config;
import com.example.nusync.data.Lecture;
import com.example.nusync.database.DatabaseUtil;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DatabaseHandler {


    public CompletableFuture<Void> initializeDatabaseAsync() {
        return CompletableFuture.runAsync(() -> {
            // Place the current initializeDatabase() code here
            DatabaseUtil dbUtil = new DatabaseUtil();

            if (!dbUtil.doesDatabaseExist()) {
                // If the database doesn't exist, fetch data and create the database.
                SheetsAPIFetcher fetcher = new SheetsAPIFetcher(Config.API_KEY, Config.COMPUTING_TIMETABLE_SPREADSHEET_ID);
                String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

                for (String day : weekdays) {
                    System.out.println("Fetching data for: " + day);
                    try {
                        Sheet sheetData = fetcher.fetchDataAndFormat(day);
                        List<GridData> gridDataList = sheetData.getData();
                        List<List<Object>> values = fetcher.fetchSheetData(day);

                        List<Lecture> lectures = processData(values, gridDataList, day);
                        DatabaseUtil.initialize();
                        dbUtil.saveLectures(lectures);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("-----------------------");
                }
            }
        });
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
                    String batch = null;
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
            List<Object> row_values = values.get(i);
            RowData rowData = gridDataList.get(0).getRowData().get(i);

            String room = row_values.get(0).toString();

            if ("Lab".equals(room)) {
                timeslot_row_index = i;
                continue;
            }

            for (int j = 0; j < row_values.size(); j++) {
                String cellValue = row_values.get(j).toString().trim();

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

                            String cellBackgroundColor = getCellBackgroundColor(rowData, j);
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

    private static String getCellBackgroundColor(RowData rowData, int j) {
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
        return cellBackgroundColor;
    }
}
