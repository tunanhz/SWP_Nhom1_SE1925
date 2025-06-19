package model;

import dal.AppointmentDAO;

public class Patient {
    private int id;
    private String fullName;
    private String dob;
    private String gender;
    private String phone;
    private String address;
    private String status;

    private Appointment appointment;


    public Patient() {
    }

    public Patient(int id, String fullName, String dob, String gender, String phone, String address, String status) {
        this.id = id;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
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

    public Appointment getAppointment() {
        return appointment;
    }

    public void includeAppointment() {
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        this.appointment = appointmentDAO.getAppointmentByPatientId(id);
    }
}