package dto;

public class DoctorScheduleDTO {
    private int scheduleId;
    private int doctorId;
    private String doctorName;
    private String doctorDepartment;
    private String workingDate;
    private String shift;
    private String roomName;
    private String roomDepartment;
    private String availability;
    private String note;

    // Getters and Setters
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDoctorDepartment() { return doctorDepartment; }
    public void setDoctorDepartment(String doctorDepartment) { this.doctorDepartment = doctorDepartment; }
    public String getWorkingDate() { return workingDate; }
    public void setWorkingDate(String workingDate) { this.workingDate = workingDate; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getRoomDepartment() { return roomDepartment; }
    public void setRoomDepartment(String roomDepartment) { this.roomDepartment = roomDepartment; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isAvailable() { return "Available".equals(availability); }
    public void setAvailable(boolean available) { this.availability = available ? "Available" : "Not Available"; }
}
