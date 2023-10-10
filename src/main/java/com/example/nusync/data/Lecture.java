package com.example.nusync.data;

public class Lecture {
    private String courseName;
    private String timeslot;
    private String batch;
    private String department;
    private String room;
    private String section;
    private String day;

    // Getter and setter for courseName
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    // Getter and setter for timeslot
    public String getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(String timeslot) {
        this.timeslot = timeslot;
    }

    // Getter and setter for batch
    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    // Getter and setter for department
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // Getter and setter for room
    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    // Getter and setter for section
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    // Getter and setter for day
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Lecture{" +
                "courseName='" + courseName + '\'' +
                ", timeslot='" + timeslot + '\'' +
                ", batch='" + batch + '\'' +
                ", department='" + department + '\'' +
                ", room='" + room + '\'' +
                ", section='" + section + '\'' +
                ", day='" + day + '\'' +
                '}';
    }

    // You can also add other methods, constructors, and toString if needed.
}
