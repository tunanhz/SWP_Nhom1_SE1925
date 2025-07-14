package dto;

import java.time.LocalDateTime;

public class AppointmentSearchDTO {
    private int appointmentId;
    private String patientName;
    private LocalDateTime appointmentDatetime;
    private String shift;
    private String status;
    private String note;

    public AppointmentSearchDTO() {
    }

    public AppointmentSearchDTO(int appointmentId, String patientName, LocalDateTime appointmentDatetime,
                                String shift, String status, String note) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.appointmentDatetime = appointmentDatetime;
        this.shift = shift;
        this.status = status;
        this.note = note;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDateTime getAppointmentDatetime() {
        return appointmentDatetime;
    }

    public void setAppointmentDatetime(LocalDateTime appointmentDatetime) {
        this.appointmentDatetime = appointmentDatetime;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "AppointmentSearchDTO{" +
                "appointmentId=" + appointmentId +
                ", patientName='" + patientName + '\'' +
                ", appointmentDatetime=" + appointmentDatetime +
                ", shift='" + shift + '\'' +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
