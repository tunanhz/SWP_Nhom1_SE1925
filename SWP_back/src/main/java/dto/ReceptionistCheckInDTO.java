package dto;

import java.util.Date;

public class ReceptionistCheckInDTO {
    private int appointmentId;
    private String patientName;
    private String doctorName;
    private Date appointmentDatetime;
    private String shift;
    private String status;
    private String note;

    public ReceptionistCheckInDTO() {
    }

    public ReceptionistCheckInDTO(int appointmentId, String patientName, String doctorName, Date appointmentDatetime,
                                  String shift, String status, String note) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.doctorName = doctorName;
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

    public String getDoctorName() {
        return doctorName;
    }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Date getAppointmentDatetime() {
        return appointmentDatetime;
    }
    public void setAppointmentDatetime(Date appointmentDatetime) {
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
}