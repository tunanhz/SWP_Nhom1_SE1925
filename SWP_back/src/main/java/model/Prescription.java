package model;

import java.util.Date;

public class Prescription {
    int prescriptionID;
    Date prescriptionDate;
    String patientName;
    String patientPhone;
    String doctorName;

    public Prescription() {
    }

    public Prescription(int prescriptionID, Date prescriptionDate, String patientName, String patientPhone, String doctorName) {
        this.doctorName = doctorName;
        this.prescriptionID = prescriptionID;
        this.prescriptionDate = prescriptionDate;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
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
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
}
