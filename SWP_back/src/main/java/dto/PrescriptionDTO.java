package dto;

import java.util.Date;

public class PrescriptionDTO {
    private int prescriptionId;
    private int medicineRecordId;
    private int doctorId;
    private Date prescriptionDate;
    private String status;

    public PrescriptionDTO() {
    }


    public PrescriptionDTO(int prescriptionId, int medicineRecordId, int doctorId,
                           Date prescriptionDate, String status) {
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

    public Date getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(Date prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String doctorDepartment;
    private String doctorPhone;


    public PrescriptionDTO(int prescriptionId, Date prescriptionDate, String status, String patientName, String patientPhone, String doctorName, String doctorDepartment, String doctorPhone) {
        this.prescriptionId = prescriptionId;
        this.prescriptionDate = prescriptionDate;
        this.status = status;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.doctorName = doctorName;
        this.doctorDepartment = doctorDepartment;
        this.doctorPhone = doctorPhone;
    }

    private String medicineName;
    private int medicineQuantity;
    private String medicineDosage;
    private Double medicinePrice;


    public PrescriptionDTO(int prescriptionId, Date prescriptionDate, String status, String patientName, String doctorName, String medicineName, int medicineQuantity, String medicineDosage, Double medicinePrice) {
        this.prescriptionId = prescriptionId;
        this.prescriptionDate = prescriptionDate;
        this.status = status;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.medicineName = medicineName;
        this.medicineQuantity = medicineQuantity;
        this.medicineDosage = medicineDosage;
        this.medicinePrice = medicinePrice;
    }

}
