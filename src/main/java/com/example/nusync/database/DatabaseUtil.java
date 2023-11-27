package com.example.nusync.database;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.nusync.AuthenticationException;
import com.example.nusync.UserNotFoundException;
import com.example.nusync.data.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    // Update the JDBC URL to point to your MySQL server.
    private static final String URL = "jdbc:mysql://localhost:3306/nusync?user=root&password=";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Ensure the MySQL driver is loaded.
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection connect() {
        // Updated connection method for MySQL.
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public boolean isTableEmpty(String tableName) {
        String query = "SELECT COUNT(*) as count FROM " + tableName;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            // If count is 0, the table is empty
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    public static void initializeFeedbackTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS feedbacks ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "student_name VARCHAR(255) NOT NULL,"
                + "email VARCHAR(255) NOT NULL,"
                + "feedback TEXT NOT NULL,"
                + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean insertFeedback(Feedback feedback) {
        String insertSQL = "INSERT INTO feedbacks (student_name, email, feedback) VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, feedback.getName());
            pstmt.setString(2, feedback.getEmail());
            pstmt.setString(3, feedback.getFeedback());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error inserting feedback: " + e.getMessage());
            return false;
        }
    }
    public List<TeacherAllocation> getAllTeacherAllocations() {
        List<TeacherAllocation> allocations = new ArrayList<>();
        String sql = "SELECT * FROM teacherAllocations";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allocations.add(new TeacherAllocation(
                        rs.getString("course"),
                        rs.getString("section"),
                        rs.getString("teacherName"),
                        rs.getString("department"),
                        rs.getInt("semester")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching teacher allocations: " + e.getMessage());
        }
        return allocations;
    }

    public List<TeacherAllocation> searchTeacherAllocations(String searchText) {
        List<TeacherAllocation> allocations = new ArrayList<>();
        String sql = "SELECT * FROM teacherAllocations WHERE teacherName LIKE ? OR course LIKE ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchText + "%");
            pstmt.setString(2, "%" + searchText + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    allocations.add(new TeacherAllocation(
                            rs.getString("course"),
                            rs.getString("section"),
                            rs.getString("teacherName"),
                            rs.getString("department"),
                            rs.getInt("semester")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching teacher allocations: " + e.getMessage());
        }
        return allocations;
    }



    public boolean createStudent(String fullName, String email, String password, String section, String batch, String department) {
        // Check if email already exists
        String checkQuery = "SELECT email FROM students WHERE email = ?";
        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, email);
            ResultSet resultSet = checkStmt.executeQuery();
            if (resultSet.next()) {
                return false; // Email already exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Insert new student record
        String insertQuery = "INSERT INTO students (full_name, email, password, section, batch, department) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, fullName);
            insertStmt.setString(2, email);
            insertStmt.setString(3, BCrypt.withDefaults().hashToString(12, password.toCharArray()));
            insertStmt.setString(4, section);
            insertStmt.setString(5, batch);
            insertStmt.setString(6, department);
            insertStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Admin authenticateAdmin(String email, String password) throws AuthenticationException, UserNotFoundException {
        String query = "SELECT * FROM admin WHERE email = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password); // Directly using the password without hashing
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new UserNotFoundException("Admin not found for email: " + email);
            }

            // If the query is successful, it means the email and password matched
            return new Admin(
                    rs.getString("email"),
                    rs.getString("password") // Password is directly used without hashing
            );
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("An error occurred while attempting to authenticate.");
        }
    }


    public Student authenticateStudent(String email, String password) throws AuthenticationException, UserNotFoundException {
        String query = "SELECT * FROM students WHERE email = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new UserNotFoundException("User not found for email: " + email);
            }

            String storedHash = rs.getString("password");
            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);
            if (!result.verified) {
                throw new AuthenticationException("Password does not match for email: " + email);
            }

            return new Student(
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("section"),
                    rs.getString("batch"),
                    rs.getString("department")

            );
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("An error occurred while attempting to authenticate.");
        }
    }


    public static void initialize() {
        // MySQL uses AUTO_INCREMENT instead of AUTOINCREMENT
        String createTableSQL = "CREATE TABLE IF NOT EXISTS lectures ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "courseName VARCHAR(255) NOT NULL,"
                + "timeslot VARCHAR(255) NOT NULL,"
                + "batch VARCHAR(255),"
                + "department VARCHAR(255),"
                + "room VARCHAR(255),"
                + "section VARCHAR(255),"
                + "day VARCHAR(255),"
                + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void initializeFreeRoomsTable() {
        // Adjust syntax for MySQL.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS freeRooms ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "roomName VARCHAR(255) NOT NULL,"
                + "startTime VARCHAR(255) NOT NULL,"
                + "endTime VARCHAR(255) NOT NULL,"
                + "day VARCHAR(255) NOT NULL,"
                + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void initializeStudentsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS students ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "full_name VARCHAR(255) NOT NULL,"
                + "email VARCHAR(255) NOT NULL UNIQUE,"
                + "password VARCHAR(255) NOT NULL,"
                + "section VARCHAR(50),"
                + "batch VARCHAR(50),"
                + "department VARCHAR(50)"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void clearTable(String tableName) {
        String sql = "DELETE FROM " + tableName;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error clearing table " + tableName + ": " + e.getMessage());
        }
    }

    public boolean isDataStale(String tableName) {
        String query = "SELECT MAX(last_updated) FROM " + tableName;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                Timestamp lastUpdated = rs.getTimestamp(1);
                if (lastUpdated != null) {
                    return lastUpdated.toInstant().isBefore(Instant.now().minus(Duration.ofDays(1)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // Assume stale if any exceptions occur
    }





    public void saveFreeRooms(List<FreeRoom> freeRooms) {
        String insertSQL = "INSERT INTO freeRooms(roomName, startTime, endTime, day) VALUES(?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            for (FreeRoom freeRoom : freeRooms) {
                pstmt.setString(1, freeRoom.getRoomName());
                pstmt.setString(2, freeRoom.getStartTime());
                pstmt.setString(3, freeRoom.getEndTime());
                pstmt.setString(4, freeRoom.getDay());
                pstmt.addBatch();
            }

            pstmt.executeBatch();

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

    public List<Lecture> queryLectures(String section, String batch, String department) {
        List<Lecture> lectures = new ArrayList<>();
        String query = "SELECT * FROM lectures WHERE "
                + "section LIKE ? AND "
                + "batch = ? AND "
                + "department = ?";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "%" + section + "%");
            statement.setString(2, batch);
            statement.setString(3, department);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String courseName = resultSet.getString("courseName");
                String timeslot = resultSet.getString("timeslot");
                String room = resultSet.getString("room");
                String day = resultSet.getString(("day"));
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

    public List<FreeRoom> loadFreeRooms() {
        List<FreeRoom> rooms = new ArrayList<>();
        String query = "SELECT roomName, startTime, endTime, day FROM freeRooms";
        try (Connection conn = connect();
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(query)) {
            while (result.next()) {
                FreeRoom room = new FreeRoom(result.getString("roomName"), result.getString("startTime"), result.getString("endTime"), result.getString("day"));
                rooms.add(room);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rooms;
    }

    // In DatabaseUtil.java



    public static void initializeTeacherAllocationTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS teacherAllocations ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "teacherName VARCHAR(255) NOT NULL,"
                + "course VARCHAR(255) NOT NULL,"
                + "department VARCHAR(255),"
                + "section VARCHAR(255) NOT NULL,"
                + "semester INT,"
                + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveTeacherAllocations(List<TeacherAllocation> allocations) {
        String insertSQL = "INSERT INTO teacherAllocations(teacherName, course, department, section, semester) VALUES(?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            for (TeacherAllocation allocation : allocations) {
                pstmt.setString(1, allocation.getTeacherName());
                pstmt.setString(2, allocation.getCourse());
                pstmt.setString(3, allocation.getDepartment());
                pstmt.setString(4, allocation.getSection());
                pstmt.setInt(5, allocation.getSemester());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public List<Lecture> queryLecturesByInstructorAndDay(String instructor, String day, String courseName) {
        List<Lecture> lectures = new ArrayList<>();
        String query = "SELECT l.* FROM lectures l " +
                "JOIN teacherAllocations ta ON l.courseName = ta.course " +
                "WHERE ta.teacherName = ? AND l.day = ?";

        // If a course name is provided, add it to the query.
        if (courseName != null && !courseName.isEmpty()) {
            query += " AND l.courseName = ?";
        }

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, instructor);
            stmt.setString(2, day);

            if (courseName != null && !courseName.isEmpty()) {
                stmt.setString(3, courseName);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Lecture lecture = new Lecture();
                //... set properties of lecture from ResultSet
                lectures.add(lecture);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return lectures;
    }



}

