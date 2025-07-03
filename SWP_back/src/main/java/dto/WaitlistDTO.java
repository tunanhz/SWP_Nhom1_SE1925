package dto;

import java.util.Date;

public class WaitlistDTO {
    private int waitlistId;
    private String patientName;
    private String doctorName;
    private String roomName;
    private Date registeredAt;
    private Date estimatedTime;
    private String visitType;
    private String status;

    public WaitlistDTO() {
    }

    public WaitlistDTO(int waitlistId, String patientName, String doctorName, String roomName,
                       Date registeredAt, Date estimatedTime, String visitType, String status) {
        this.waitlistId = waitlistId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.roomName = roomName;
        this.registeredAt = registeredAt;
        this.estimatedTime = estimatedTime;
        this.visitType = visitType;
        this.status = status;
    }

    public int getWaitlistId() {
        return waitlistId;
    }
    public void setWaitlistId(int waitlistId) {
        this.waitlistId = waitlistId;
    }

    public String getPatientName() {
        return patientName;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Date getRegisteredAt() {
        return registeredAt;
    }
    public void setRegisteredAt(Date registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Date getEstimatedTime() {
        return estimatedTime;
    }
    public void setEstimatedTime(Date estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getVisitType() {
        return visitType;
    }
    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}