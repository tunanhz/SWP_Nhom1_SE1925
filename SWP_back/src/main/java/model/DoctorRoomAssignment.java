package model;
public class DoctorRoomAssignment {
    private int assignmentId;
    private int doctorId;
    private int roomId;
    private String assignmentDate;
    private String shift;

    public DoctorRoomAssignment() {
    }

    public DoctorRoomAssignment(int assignmentId, int doctorId, int roomId, String assignmentDate, String shift) {
        this.assignmentId = assignmentId;
        this.doctorId = doctorId;
        this.roomId = roomId;
        this.assignmentDate = assignmentDate;
        this.shift = shift;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(String assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }
}