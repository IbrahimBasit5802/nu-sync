package com.example.nusync.database;

import com.example.nusync.data.Lecture;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    private static final String URL = "jdbc:sqlite:lectures.db";
    private static final String DATABASE_PATH = "lectures.db";


    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public boolean doesDatabaseExist() {
        File databaseFile = new File(DATABASE_PATH);
        return databaseFile.exists();
    }

    public static void initialize() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS lectures ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "courseName TEXT NOT NULL,"
                + "timeslot TEXT NOT NULL,"
                + "batch TEXT,"
                + "department TEXT,"
                + "room TEXT,"
                + "section TEXT,"
                + "day TEXT"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveLectures(List<Lecture> lectures) {
        String insertSQL = "INSERT INTO lectures(courseName, timeslot, batch, department, room, section, day) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            for (Lecture lecture : lectures) {
                pstmt.setString(1, lecture.getCourseName());
                pstmt.setString(2, lecture.getTimeslot());
                pstmt.setString(3, lecture.getBatch());
                pstmt.setString(4, lecture.getDepartment());
                pstmt.setString(5, lecture.getRoom());
                pstmt.setString(6, lecture.getSection());
                pstmt.setString(7, lecture.getDay());
                pstmt.addBatch();
            }

            pstmt.executeBatch();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Lecture> queryLectures(String section, String day, String batch, String department) {
        List<Lecture> lectures = new ArrayList<>();
        String query = "SELECT * FROM lectures WHERE "
                + "section LIKE ? AND "
                + "day = ? AND "
                + "batch = ? AND "
                + "department = ?";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "%" + section + "%");
            statement.setString(2, day);
            statement.setString(3, batch);
            statement.setString(4, department);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String courseName = resultSet.getString("courseName");
                String timeslot = resultSet.getString("timeslot");
                String room = resultSet.getString("room");
                //... Extract other fields as needed

                Lecture lecture = new Lecture();
                lecture.setCourseName(courseName);
                lecture.setTimeslot(timeslot);
                lecture.setRoom(room);
                lecture.setBatch(batch);
                lecture.setDay(day);
                lecture.setSection(section);
                //... Set other fields as needed

                lectures.add(lecture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lectures;
    }

    public List<Lecture> loadLectures() {
        List<Lecture> lectures = new ArrayList<>();
        String selectSQL = "SELECT courseName, timeslot, batch, department, room, section, day FROM lectures";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                Lecture lecture = new Lecture();
                lecture.setCourseName(rs.getString("courseName"));
                lecture.setTimeslot(rs.getString("timeslot"));
                lecture.setBatch(rs.getString("batch"));
                lecture.setDepartment(rs.getString("department"));
                lecture.setRoom(rs.getString("room"));
                lecture.setSection(rs.getString("section"));
                lecture.setDay(rs.getString("day"));

                lectures.add(lecture);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return lectures;
    }
}

