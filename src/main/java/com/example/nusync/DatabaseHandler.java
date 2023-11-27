package com.example.nusync;

import com.example.nusync.config.Config;
import com.example.nusync.data.FreeRoom;
import com.example.nusync.data.Lecture;
import com.example.nusync.data.TeacherAllocation;
import com.example.nusync.database.DatabaseUtil;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseHandler {



    public CompletableFuture<Void> initializeDatabaseAsync() {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtil dbUtil = new DatabaseUtil();


            // Initialize tables if they don't exist.
            DatabaseUtil.initialize();
            DatabaseUtil.initializeFreeRoomsTable();
            DatabaseUtil.initializeTeacherAllocationTable();
            DatabaseUtil.initializeStudentsTable();
            DatabaseUtil.initializeFeedbackTable();

            SheetsAPIFetcher fetcher = new SheetsAPIFetcher();
            String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

            // Check if lectures table is empty and fetch data if it is
            if (dbUtil.isTableEmpty("lectures") || dbUtil.isDataStale("lectures") || dbUtil.isTableEmpty("freeRooms") || dbUtil.isDataStale("freeRooms")) {
                dbUtil.clearTable("lectures");
                dbUtil.clearTable("freeRooms");
                for (String day : weekdays) {
                    System.out.println("Fetching data for: " + day);
                    try {
                        Sheet sheetData = fetcher.fetchDataAndFormat(day, Config.COMPUTING_TIMETABLE_SPREADSHEET_ID);
                        List<GridData> gridDataList = sheetData.getData();
                        List<List<Object>> values = fetcher.fetchSheetData(day, Config.COMPUTING_TIMETABLE_SPREADSHEET_ID);

                        List<Lecture> lectures = processData(values, gridDataList, day);
                        lectures.sort(Comparator.comparing(Lecture::getTimeslot));

                        List<FreeRoom> freeRooms = computeFreeRooms(lectures, day);



                        // Save the data to the database
                        dbUtil.saveLectures(lectures);
                        dbUtil.saveFreeRooms(freeRooms);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("-----------------------");
                }
            }

            // Check if teacherAllocations table is empty and fetch data if it is
            if (dbUtil.isTableEmpty("teacherAllocations") || dbUtil.isDataStale("teacherAllocations")) {
                dbUtil.clearTable("teacherAllocations");
                String[] courseAllocationSheet = {"Theory-Computing", "Labs-Computing", "Sciences-Humanities", "MG"};
                System.out.println("Fetching Teacher Course Allocations...");

                for (String value : courseAllocationSheet) {
                    System.out.println("Fetching Teacher Allocation Data for: " + value);
                    try {
                        List<List<Object>> values = fetcher.fetchSheetData(value, Config.TEACHER_ALLOCATION_SPREADSHEET_ID);

                        List<TeacherAllocation> tas = processTA(values);
                        // Save the teacher allocations to the database
                        dbUtil.saveTeacherAllocations(tas);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    public static List<TeacherAllocation> processTA(List<List<Object>> values) {
        List<TeacherAllocation> teacherAllocations = new ArrayList<>();
        String currentCourseName = "";

        for (int i = 4; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() > 5) {
                String courseName = "";
                String sectionCode = "";
                String instructor = "";
                String department = "";
                int semester = 0;
                String section = "";

                if (row.get(2) != null && !row.get(2).toString().trim().isEmpty()) {
                    currentCourseName = row.get(2).toString().trim();
                }

                courseName = currentCourseName;
                System.out.println(courseName);


                    sectionCode = row.get(4) != null ? row.get(4).toString().trim() : "";
                    String[] parts = sectionCode.split("-");

                    if (parts[0].startsWith("B")) {
                        department = parts[0].substring(1); // Skip 'B' and take the rest as department
                    } else {
                        department = parts[0]; // Take everything before '-' as department
                    }
                    if (parts.length > 1) {
                        String secPart = parts[1];
                        if (Character.isDigit(secPart.charAt(0))) {
                            semester = Character.getNumericValue(secPart.charAt(0)); // First character is the semester number
                            section = secPart.substring(1); // Rest is the section
                        } else {
                            // Handle cases where the section part does not start with a number
                            // Here, assume the entire section part is the section code
                            section = secPart;
                        }

                        // Format section for merged classes
                        if (!section.contains("/") && section.length() > 1) {
                            section = section.replaceAll("(\\D)(\\D)", "$1/$2"); // Add slash between characters
                        }
                    }


                    instructor = row.get(5).toString().trim();


                if (!section.isEmpty() && !instructor.isEmpty()) {
                    TeacherAllocation allocation = new TeacherAllocation(courseName, section, instructor, department, semester);

                    teacherAllocations.add(allocation);
                }
            }
        }

        return teacherAllocations;
    }

    private static Lecture processRepeatOrElectiveCourseCell(String cellValue, String batchOrType, String room, String day, Pattern timeslotPattern) {
        Lecture lecture = new Lecture();
        lecture.setRoom(room);
        lecture.setDay(day);
        lecture.setBatch(batchOrType); // This could be "Repeat" or "19-Electives"

        // Check if there is a timeslot within the cell
        Matcher timeslotMatcher = timeslotPattern.matcher(cellValue);
        if (timeslotMatcher.find()) {
            lecture.setTimeslot(timeslotMatcher.group());
            cellValue = cellValue.replace(timeslotMatcher.group(), "").trim(); // Remove the timeslot from the cell value
        }

        // Split the remaining cell value based on parentheses
        String[] parts = cellValue.split("[()]");
        if (parts.length > 0) {
            lecture.setCourseName(parts[0].trim()); // The course name is before the parentheses

            if (parts.length > 1) {
                // Inside the parentheses, we could have a department, section, or both
                String insideParens = parts[1].trim();
                String[] insideParts = insideParens.split("[,-]");
                if (insideParts.length == 1) {
                    // Only department or only section is given
                    String firstPart = insideParts[0].trim();
                    if (Character.isDigit(firstPart.charAt(0))) {
                        // If it starts with a digit, it's a batch number
                        lecture.setBatch(firstPart);
                    } else {
                        // Otherwise, it's a department code or a section
                        if (firstPart.length() == 1 || firstPart.contains("/")) {
                            lecture.setSection(firstPart);
                        } else {
                            lecture.setDepartment(firstPart);
                        }
                    }
                } else if (insideParts.length == 2) {
                    // We have both department and section
                    lecture.setDepartment(insideParts[0].trim());
                    lecture.setSection(insideParts[1].trim());
                } else if (insideParts.length == 3) {
                    // We have department, section, and batch
                    lecture.setDepartment(insideParts[0].trim());
                    lecture.setSection(insideParts[1].trim());
                    lecture.setBatch(insideParts[2].trim());
                }
            }
        }
        return lecture;
    }

        public static List<Lecture> processData(List<List<Object>> values, List<GridData> gridDataList, String day) {
        final Pattern TIMESLOT_PATTERN = Pattern.compile("\\b\\d{2}:\\d{2}-\\d{2}:\\d{2}\\b");

        List<Lecture> lectures = new ArrayList<>();
        Map<String, String> colors = getColors(values, gridDataList);

        int timeslot_row_index = 4;


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
                String cellBackgroundColor = getCellBackgroundColor(rowData, j);


                if (!cellValue.isEmpty()) {
                    String batchOrType = colors.get(cellBackgroundColor); // This could be "Repeat", "19-Electives", or a batch number

                    // Check if this is a repeat or elective course cell
                    if ("Repeat".equals(batchOrType) || "19-Electives".equals(batchOrType)) {
                        // Handle the cell value processing for repeat and elective courses
                        Lecture lecture = processRepeatOrElectiveCourseCell(cellValue, batchOrType, room, day, TIMESLOT_PATTERN);
                        if (lecture != null) {
                            lectures.add(lecture);
                        }
                    }
                    else {
                        String timeslot;
                        if (cellValue.contains(")")) {
                            String courseInfo = cellValue.split("\\)")[0] + ")";
                            String restOfCell = cellValue.replace(courseInfo, "").trim();

                            if (!restOfCell.isEmpty() && !restOfCell.equals("Resch") && !restOfCell.equals("ReSch") && !restOfCell.equals("Cancelled")) {
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
        }

        return lectures;
    }

    private static Map<String, String> getColors(List<List<Object>> values, List<GridData> gridDataList) {
        Map<String, String> colors = new HashMap<>(); // To store color-to-Batch mapping

        // Processing colors for batches
        for (int i = 0; i < 4; i++) {
            List<Object> row_vals = values.get(i);
            RowData rowData = gridDataList.get(0).getRowData().get(i);
            for (int j = 4; j < 19; j++) {
                if (j == 18 && (i == 2 || i == 3)) {
                    continue;
                }
                String bg = null;
                String cell = row_vals.get(j).toString().trim();
                if (!cell.isEmpty()) {
                    String batch = null;
                    if (cell.contains("(")) {
                        if (cell.contains("Electives")) {
                            batch = "19-Electives";
                        }
                        else {
                            String[] parts = cell.split("[()]");
                            if (parts.length > 1) {
                                batch = parts[1].substring(parts[1].length() - 2); // Get the last two characters
                            }
                        }

                    }

                    else {
                        batch = "Repeat";
                    }

                    var bgColor = rowData.getValues().get(j).getEffectiveFormat().getBackgroundColor();
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
        return colors;
    }


    public List<FreeRoom> computeFreeRooms(List<Lecture> lectures, String day) {
        final LocalTime START_TIME = LocalTime.of(8, 0); // Rooms are available from 8 AM
        final LocalTime END_TIME = LocalTime.of(20, 0); // Rooms are available until 8 PM
        Map<String, TreeMap<LocalTime, Lecture>> roomSchedules = new HashMap<>();

        for (Lecture lecture : lectures) {
            LocalTime startTime = convertStringToTime(lecture.getTimeslot().split("-")[0]);
            if (startTime != null) { // Only add to the map if the start time is valid
                roomSchedules.computeIfAbsent(lecture.getRoom(), k -> new TreeMap<>()).put(startTime, lecture);
            }
        }

        List<FreeRoom> freeRoomsList = new ArrayList<>();

        for (Map.Entry<String, TreeMap<LocalTime, Lecture>> entry : roomSchedules.entrySet()) {
            String room = entry.getKey();
            TreeMap<LocalTime, Lecture> schedule = entry.getValue();
            LocalTime currentTime = START_TIME;

            for (Map.Entry<LocalTime, Lecture> scheduleEntry : schedule.entrySet()) {
                LocalTime lectureStart = scheduleEntry.getKey();
                if (currentTime.isBefore(lectureStart)) {
                    freeRoomsList.add(new FreeRoom(room, timeToString(currentTime), timeToString(lectureStart), day));
                }
                LocalTime lectureEnd = convertStringToTime(scheduleEntry.getValue().getTimeslot().split("-")[1]);
                if (lectureEnd != null) { // Update current time only if the end time is valid
                    currentTime = lectureEnd;
                }
            }

            if (currentTime.isBefore(END_TIME)) {
                freeRoomsList.add(new FreeRoom(room, timeToString(currentTime), timeToString(END_TIME), day));
            }
        }

        return freeRoomsList;
    }

// The convertStringToTime and timeToString methods remain unchanged.


    private LocalTime convertStringToTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            System.err.println("Time string is null or empty");
            return null;
        }

        // Handle known keywords and formats
        if (timeString.equalsIgnoreCase("Cancelled")) {
            System.err.println("Lecture is cancelled: " + timeString);
            return null;
        }

        // Handle rescheduled times with "ReSch" at the beginning or end of the string
        String processedTime = timeString.replaceFirst("(?i)ReSch", "").trim();
        processedTime = processedTime.replaceFirst("(\\d{1,2}:\\d{2}-\\d{1,2}:\\d{2})$", "").trim();

        // Now try to parse the processed time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        try {
            return LocalTime.parse(processedTime, timeFormatter);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid time format: " + processedTime);
            return null;
        }
    }


    private String timeToString(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }




    private static String getCellBackgroundColor(RowData rowData, int j) {
        String cellBackgroundColor = "#FFFFFF"; // Default color

        if (rowData.getValues() != null && rowData.getValues().get(j) != null && rowData.getValues().get(j).getEffectiveFormat() != null) {
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
        }
        return cellBackgroundColor;
    }


}

