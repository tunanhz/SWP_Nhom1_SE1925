package dto;

import dal.DoctorDAO;
import dal.PatientDAO;
import model.Doctor;
import model.Patient;

public class AppointmentReportDTO {

    private int appointmentId;
    private int patientId;
    private int doctorId;
    private String patientName;
    private String appointmentDateTime;
    private String shift;
    private String cancellationReason;
    private String appointmentStatus;
    private String doctorName;
    private String isNoShow;

    private Patient patient;
    private Doctor doctor;

    public AppointmentReportDTO(int appointmentId, int patientId, int doctorId, String patientName,
                                String appointmentDateTime, String shift, String cancellationReason,
                                String appointmentStatus, String doctorName, String isNoShow) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.appointmentDateTime = appointmentDateTime;
        this.shift = shift;
        this.cancellationReason = cancellationReason;
        this.appointmentStatus = appointmentStatus;
        this.doctorName = doctorName;
        this.isNoShow = isNoShow;
    }

    // Getters
    public int getAppointmentId() {
        return appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public String getShift() {
        return shift;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getIsNoShow() {
        return isNoShow;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setAppointmentDateTime(String appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setIsNoShow(String isNoShow) {
        this.isNoShow = isNoShow;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public void includeDoctor() {
        DoctorDAO dao = new DoctorDAO();
        this.doctor = dao.getDoctorById(this.doctorId);
    }

    public void includePatient() {
        PatientDAO dao = new PatientDAO();
        this.patient = dao.getPatientByPatientId(this.patientId);

    }
}
