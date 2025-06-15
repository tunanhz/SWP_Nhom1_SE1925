package model;

import dal.DoctorDAO;

import java.sql.Date;
import java.sql.Timestamp;

public class AppointmentDTO {
    private int accountPatientId;
    private int patientId;
    private Integer doctorId; // Nullable
    private int appointmentId;
    private Integer receptionistId; // Nullable
    private String fullName;
    private Date dob; // For patient's date of birth
    private String gender;
    private String phone;
    private String address;
    private String email;
    private String accountStatus;
    private String appointmentDateTime; // For both date and time
    private String shift;
    private String appointmentStatus;
    private String note;

    private Doctor doctor;

    // Constructor
    public AppointmentDTO(int accountPatientId, int patientId, Integer doctorId, int appointmentId,
                          Integer receptionistId, String fullName, Date dob, String gender,
                          String phone, String address, String email, String accountStatus,
                          String appointmentDateTime, String shift, String appointmentStatus, String note) {
        this.accountPatientId = accountPatientId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.receptionistId = receptionistId;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.accountStatus = accountStatus;
        this.appointmentDateTime = appointmentDateTime;
        this.shift = shift;
        this.appointmentStatus = appointmentStatus;
        this.note = note;
    }

    // Default constructor
    public AppointmentDTO() {
    }

    // Getters and Setters
    public int getAccountPatientId() {
        return accountPatientId;
    }

    public void setAccountPatientId(int accountPatientId) {
        this.accountPatientId = accountPatientId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getReceptionistId() {
        return receptionistId;
    }

    public void setReceptionistId(Integer receptionistId) {
        this.receptionistId = receptionistId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(String appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFormattedAppointmentDateTime() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(appointmentDateTime);
    }

    public Doctor getDoctor() {
        return doctor;
    }


    public void includeDoctor() {
        if (doctorId != null) {
            DoctorDAO dao = new DoctorDAO();
            this.doctor = dao.getDoctorById(this.doctorId);
        }
    }

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "AppointmentDTO{" +
                "accountPatientId=" + accountPatientId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", appointmentId=" + appointmentId +
                ", receptionistId=" + receptionistId +
                ", fullName='" + fullName + '\'' +
                ", dob=" + dob +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", appointmentDateTime=" + appointmentDateTime +
                ", shift='" + shift + '\'' +
                ", appointmentStatus='" + appointmentStatus + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}