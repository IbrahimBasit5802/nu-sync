package com.example.nusync.data;

public class TeacherAllocation {
    private String teacherName;
    private String course;
    private String section;

    private int semester;

    private String department;

    public TeacherAllocation(String courseName, String section, String instructor) {
        this.course = courseName;
        this.section = section;
        this.teacherName = instructor;
    }

    // Getters and setters
    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSection() {
        return section;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDepartment() {

        return department;
    }

    public int getSemester() {

        return semester;
    }
}
