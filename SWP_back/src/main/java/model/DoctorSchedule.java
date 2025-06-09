public class DoctorSchedule {
    private int scheduleId;
    private int doctorId;
    private String workingDate;
    private String shift;
    private int roomId;
    private boolean isAvailable;
    private String note;

    public DoctorSchedule() {
    }

    public DoctorSchedule(int scheduleId, int doctorId, String workingDate, String shift,
                         int roomId, boolean isAvailable, String note) {
        this.scheduleId = scheduleId;
        this.doctorId = doctorId;
        this.workingDate = workingDate;
        this.shift = shift;
        this.roomId = roomId;
        this.isAvailable = isAvailable;
        this.note = note;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getWorkingDate() {
        return workingDate;
    }

    public void setWorkingDate(String workingDate) {
        this.workingDate = workingDate;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}