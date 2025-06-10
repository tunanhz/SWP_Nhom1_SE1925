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
    private int id;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String phone;
    private String address;
    private String email;

    private Appointment appointment;

    public Patient() {
    }

    public Patient(int id, String fullName, String dateOfBirth, String gender, String phone, String address, String email) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.email = email;
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

    public Appointment getAppointment() {
        return appointment;
    }

    public void includeAppointment() {
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        this.appointment = appointmentDAO.getAppointmentByPatientId(id);
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

