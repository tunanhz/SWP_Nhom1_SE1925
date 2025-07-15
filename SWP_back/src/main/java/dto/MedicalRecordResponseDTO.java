package dto;

import java.time.LocalDate;

public class MedicalRecordResponseDTO {
    private int recordId;
    private int patientId;
    private String patientName;
    private LocalDate patientDob;
    private String patientGender;
    private String patientPhone;
    private String patientAddress;
    private LocalDate date;
    private String type;
    private String status;

    public MedicalRecordResponseDTO() {
    }

    public MedicalRecordResponseDTO(int recordId, int patientId, String patientName,
                                    LocalDate patientDob, String patientGender, String patientPhone,
                                    String patientAddress, LocalDate date, String type, String status) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientDob = patientDob;
        this.patientGender = patientGender;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.date = date;
        this.type = type;
        this.status = status;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getPatientDob() {
        return patientDob;
    }

    public void setPatientDob(LocalDate patientDob) {
        this.patientDob = patientDob;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MedicalRecordResponseDTO{" +
                "recordId=" + recordId +
                ", patientId=" + patientId +
                ", patientName='" + patientName + '\'' +
                ", patientDob=" + patientDob +
                ", patientGender='" + patientGender + '\'' +
                ", patientPhone='" + patientPhone + '\'' +
                ", patientAddress='" + patientAddress + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
