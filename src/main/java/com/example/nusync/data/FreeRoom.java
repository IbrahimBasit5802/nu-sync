package com.example.nusync.data;

public class FreeRoom {
    private String roomName;
    private String startTime;
    private String endTime;
    private String day;

    public FreeRoom(String roomName, String startTime, String endTime, String day) {
        this.roomName = roomName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
    }

    // Getters
    public String getRoomName() {
        return roomName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDay() {
        return day;
    }

    // Setters
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setDay(String day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Room: " + roomName + ", Day: " + day + ", From: " + startTime + " To: " + endTime;
    }
}
