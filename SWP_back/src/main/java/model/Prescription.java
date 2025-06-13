package model;

public class Prescription {
    private int prescriptionId;
    private int medicineRecordId;
    private int doctorId;
    private String prescriptionDate;
    private String status;

    public Prescription() {
    }

    public Prescription(int prescriptionId, int medicineRecordId, int doctorId,
                        String prescriptionDate, String status) {
        this.prescriptionId = prescriptionId;
        this.medicineRecordId = medicineRecordId;
        this.doctorId = doctorId;
        this.prescriptionDate = prescriptionDate;
        this.status = status;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public int getMedicineRecordId() {
        return medicineRecordId;
    }

    public void setMedicineRecordId(int medicineRecordId) {
        this.medicineRecordId = medicineRecordId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(String prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}