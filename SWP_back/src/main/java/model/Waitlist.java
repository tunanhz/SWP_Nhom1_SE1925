public class Waitlist {
    private int waitlistId;
    private int patientId;
    private int doctorId;
    private int roomId;
    private String registeredAt;
    private String estimatedTime;
    private String visittype;
    private String status;

    public Waitlist() {
    }

    public Waitlist(int waitlistId, int patientId, int doctorId, int roomId, String registeredAt,
                   String estimatedTime, String visittype, String status) {
        this.waitlistId = waitlistId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.roomId = roomId;
        this.registeredAt = registeredAt;
        this.estimatedTime = estimatedTime;
        this.visittype = visittype;
        this.status = status;
    }

    public int getWaitlistId() {
        return waitlistId;
    }

    public void setWaitlistId(int waitlistId) {
        this.waitlistId = waitlistId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
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

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getVisittype() {
        return visittype;
    }

    public void setVisittype(String visittype) {
        this.visittype = visittype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}