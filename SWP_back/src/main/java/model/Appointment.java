package model;

public class Appointment {
    private int appointmentID;
    private int doctorID;
    private int patientID;
    private int receptionistID;
    private String appointmentDatetime;
    private String shift;
    private String status;
    private String note;

    public Appointment() {
    }

    public Appointment(int appointmentID, int doctorID, int patientID, int receptionistID, String appointmentDatetime, String shift, String status, String note) {
        this.appointmentID = appointmentID;
        this.doctorID = doctorID;
        this.patientID = patientID;
        this.receptionistID = receptionistID;
        this.appointmentDatetime = appointmentDatetime;
        this.shift = shift;
        this.status = status;
        this.note = note;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(int doctorID) {
        this.doctorID = doctorID;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public int getReceptionistID() {
        return receptionistID;
    }

    public void setReceptionistID(int receptionistID) {
        this.receptionistID = receptionistID;
    }

    public String getAppointmentDatetime() {
        return appointmentDatetime;
    }

    public void setAppointmentDatetime(String appointmentDatetime) {
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
