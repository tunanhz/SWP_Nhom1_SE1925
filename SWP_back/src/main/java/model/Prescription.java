package model;

import java.util.Date;

public class Prescription {
    int prescriptionID;
    Date prescriptionDate;
    String prescriptionStatus;
    String patientName;
    String patientPhone;
    String DoctorName;
    String DoctorDepartment;
    String DoctorPhone;

    public Prescription() {
    }

    public Prescription(int prescriptionID, Date prescriptionDate, String prescriptionStatus, String patientName, String patientPhone, String doctorName, String doctorDepartment, String doctorPhone) {
        this.prescriptionID = prescriptionID;
        this.prescriptionDate = prescriptionDate;
        this.prescriptionStatus = prescriptionStatus;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        DoctorName = doctorName;
        DoctorDepartment = doctorDepartment;
        DoctorPhone = doctorPhone;
    }

    public int getPrescriptionID() {
        return prescriptionID;
    }

    public void setPrescriptionID(int prescriptionID) {
        this.prescriptionID = prescriptionID;
    }

    public Date getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(Date prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getPrescriptionStatus() {
        return prescriptionStatus;
    }

    public void setPrescriptionStatus(String prescriptionStatus) {
        this.prescriptionStatus = prescriptionStatus;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDoctorName() {
        return DoctorName;
    }

    public void setDoctorName(String doctorName) {
        DoctorName = doctorName;
    }

    public String getDoctorDepartment() {
        return DoctorDepartment;
    }

    public void setDoctorDepartment(String doctorDepartment) {
        DoctorDepartment = doctorDepartment;
    }

    public String getDoctorPhone() {
        return DoctorPhone;
    }

    public void setDoctorPhone(String doctorPhone) {
        DoctorPhone = doctorPhone;
    }
}
