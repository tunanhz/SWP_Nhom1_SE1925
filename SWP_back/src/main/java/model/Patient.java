/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import dal.AppointmentDAO;

/**
 *
 * @author DTanh
 */
public class Patient {
    private int patientId;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String phone;
    private String address;
    private String email;

    private Appointment appointment;

    public Patient() {
    }

    public Patient(int patientId, String fullName, String dateOfBirth, String gender, String phone, String address, String email) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.email = email;
    }

    public int getId() {
        return patientId;
    }

    public void setId(int patientId) {
        this.patientId = patientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public Appointment getAppointment() {
        return appointment;
    }

    AppointmentDAO ad = new AppointmentDAO();
    public void includeAppointment() {
        this.appointment = ad.getAppointmentByPatientId(patientId);
    }

}

